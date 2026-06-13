package com.fruitmkt.dto;

import com.fruitmkt.util.PaginationUtil;

/**
 * PageMeta — Metadata phân trang đặt vào ApiResponse.meta.
 *
 * Dùng kèm: ApiResponse.ok(items, new PageMeta(page, size, total, pages))
 *
 * @param page       trang hiện tại (1-based)
 * @param pageSize   số record mỗi trang
 * @param totalCount tổng số record
 * @param totalPages tổng số trang
 * @author fruitmkt-team
 */
public record PageMeta(int page, int pageSize, long totalCount, int totalPages) {

    /** Factory tính sẵn totalPages từ totalCount + pageSize (dùng PaginationUtil). */
    public static PageMeta of(int page, int pageSize, long totalCount) {
        return new PageMeta(page, pageSize, totalCount,
                PaginationUtil.getTotalPages(totalCount, pageSize));
    }
}
