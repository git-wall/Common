package org.app.common.utils;

import lombok.Getter;

import java.util.List;
public class PaginationUtils {

    public static <T> PaginatedResponse<T> paginate(List<T> items, int page, int size) {
        int totalItems = items.size();
        int start = Math.max(0, page * size);
        int end = Math.min(totalItems, start + size);

        List<T> paginatedItems = items.subList(start, end);
        return new PaginatedResponse<>(paginatedItems, page, size, totalItems);
    }

    @Getter
    public static class PaginatedResponse<T> {
        private final List<T> items;
        private final int page;
        private final int size;
        private final int totalItems;

        public PaginatedResponse(List<T> items, int page, int size, int totalItems) {
            this.items = items;
            this.page = page;
            this.size = size;
            this.totalItems = totalItems;
        }
    }
}