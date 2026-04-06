package org.app.database.mybatis;

import java.util.Collections;
import java.util.List;

/**
 * Result of a paged query — contains rows for the current page plus metadata.
 *
 * <pre>
 * PageResult&lt;User&gt; page = db.selectPage(stmt, USER_MAPPER, 1, 20);
 *
 * page.rows()        // List&lt;User&gt; for this page
 * page.total()       // total matching rows (from COUNT sub-query)
 * page.page()        // current page (1-based)
 * page.pageSize()    // rows per page
 * page.totalPages()  // total number of pages
 * page.hasNext()     // true if there is a next page
 * page.hasPrev()     // true if there is a previous page
 * </pre>
 *
 * @param <T> row type
 */
public final class PageResult<T> {

    private final List<T> rows;
    private final long    total;
    private final int     page;
    private final int     pageSize;

    public PageResult(List<T> rows, long total, int page, int pageSize) {
        this.rows     = Collections.unmodifiableList(rows);
        this.total    = total;
        this.page     = page;
        this.pageSize = pageSize;
    }

    /** Rows on this page. */
    public List<T> rows()       { return rows; }

    /** Total rows matching the query (across all pages). */
    public long total()         { return total; }

    /** Current page number (1-based). */
    public int page()           { return page; }

    /** Max rows per page. */
    public int pageSize()       { return pageSize; }

    /** Total number of pages. */
    public int totalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) total / pageSize);
    }

    public boolean hasNext()    { return page < totalPages(); }
    public boolean hasPrev()    { return page > 1; }
    public boolean isEmpty()    { return rows.isEmpty(); }
    public int size()           { return rows.size(); }

    /** Offset (0-based) for SQL OFFSET clause — derived from page + pageSize. */
    public int offset()         { return (page - 1) * pageSize; }

    @Override
    public String toString() {
        return String.format("PageResult{page=%d/%d, size=%d, total=%d}",
            page, totalPages(), rows.size(), total);
    }
}
