package org.app.database.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;

public class BaseJooqRepository<T> {
    protected final DSLContext dsl;
    protected final Table<?> table;
    protected final Class<T> dtoClass;

    public BaseJooqRepository(DSLContext dsl, String tableName, Class<T> dtoClass) {
        this.dsl = dsl;
        this.table = DSL.table(DSL.name(tableName));
        this.dtoClass = dtoClass;
    }

    // Lấy 1 bản ghi theo ID
    public T findById(String idColumn, Object idValue) {
        try (var select = dsl.selectFrom(table)) {
            return select
                .where(DSL.field(DSL.name(idColumn)).eq(idValue))
                .fetchOneInto(dtoClass);
        }
    }

    // Lấy tất cả bản ghi
    public List<T> findAll() {
        try (var select = dsl.selectFrom(table)) {
            return select.fetchInto(dtoClass);
        }
    }

    // Thêm mới dữ liệu (Truyền vào POJO)
    public int insert(T dto) {
        try (var sql = dsl.insertInto(table).set(dsl.newRecord(table, dto))) {
            return sql.execute();
        }
    }

    // Cap nhat du lieu truyen vao POJO
    public int update(T dto) {
        try (var sql = dsl.update(table).set(dsl.newRecord(table, dto))) {
            return sql.execute();
        }
    }

    // Xóa dữ liệu
    public int deleteById(String idColumn, Object idValue) {
        try (var delete = dsl.deleteFrom(table)) {
            return delete
                .where(DSL.field(DSL.name(idColumn)).eq(idValue))
                .execute();
        }
    }

    /**
     * Tìm kiếm với Filter động và Phân trang (Offset-based)
     * <pre>{@code
     * // 1. Tạo điều kiện động (giống như "chạy chay" nhưng cực mạnh)
     *     Condition filter = DSL.noCondition();
     *     filter = filter.and(DSL.field("status").eq("ACTIVE"));
     *     filter = filter.and(DSL.field("age").greaterThan(18));
     *
     * // 2. Phân trang kiểu Offset (Page 1: limit 10, offset 0)
     *     List<UserDTO> page1 = repo.findWithPagination(
     *         filter,
     *         10, 0,
     *         DSL.field("created_at").desc() // Sắp xếp giảm dần
     *     );
     * }</pre>
     */
    public List<T> findWithPagination(Condition condition, int limit, int offset, SortField<?>... sortFields) {
        try (var select = dsl.selectFrom(table)) {
            return select
                .where(condition)
                .orderBy(sortFields)
                .limit(limit)
                .offset(offset)
                .fetchInto(dtoClass);
        }
    }

    /**
     * Phân trang kiểu Cursor (Keyset Pagination) - Tốt cho Performance với data lớn
     * <pre>{@code
     *  // Phân trang kiểu Cursor (Lấy 10 thằng tiếp theo sau ID số 100)
     *  // Cách này nhanh hơn OFFSET rất nhiều khi table có hàng triệu record
     *  List<UserDTO> nextItems = repo.findWithCursor("id", 100L, 10);
     * }</pre>
     * @param lastId Giá trị ID của bản ghi cuối cùng của trang trước
     */
    public List<T> findWithCursor(String idColumn, Object lastId, int limit) {
        Field<Object> idField = DSL.field(DSL.name(idColumn));

        try (var select = dsl.selectFrom(table)) {
            return select
                .where(lastId == null ? DSL.noCondition() : idField.gt(lastId)) // Cursor logic
                .orderBy(idField.asc())
                .limit(limit)
                .fetchInto(dtoClass);
        }
    }
}
