package com.fruitmkt.util;

/**
 * PaginationUtil — Tính toán offset và tổng trang cho phân trang.
 *
 * @author fruitmkt-team
 */
public final class PaginationUtil {

    private static final int MAX_PAGE_SIZE = com.fruitmkt.config.AppConfig.MAX_PAGE_SIZE;
    private static final int DEFAULT_PAGE_SIZE = com.fruitmkt.config.AppConfig.DEFAULT_PAGE_SIZE;

    /**
     * Tính offset (dùng trong SQL OFFSET ? ROWS)
     * @param page     Trang hiện tại (1-based)
     * @param pageSize Số record/trang
     * @return offset để nhảy dòng
     */
    public static int getOffset(int page, int pageSize) {
        int validatedPage = validatePage(page);
        int validatedPageSize = validatePageSize(pageSize);
        return Math.max(0, (validatedPage - 1) * validatedPageSize);
    }

    /**
     * Tính tổng số trang
     * @param totalItems Tổng số record
     * @param pageSize   Số record/trang
     * @return tổng số trang
     */
    public static int getTotalPages(long totalItems, int pageSize) {
        int validatedPageSize = validatePageSize(pageSize);
        if (validatedPageSize <= 0) return 0;
        return (int) Math.ceil((double) totalItems / validatedPageSize);
    }

    /** Parse page param từ request, mặc định trang 1 và giới hạn tối thiểu 1 */
    public static int parsePage(String param) {
        try {
            if (param == null || param.trim().isEmpty()) {
                return 1;
            }
            int p = Integer.parseInt(param.trim());
            return validatePage(p);
        } catch (Exception e) {
            return 1;
        }
    }

    /** Parse pageSize param từ request, mặc định 10 và giới hạn tối đa 100 */
    public static int parsePageSize(String param) {
        try {
            if (param == null || param.trim().isEmpty()) {
                return DEFAULT_PAGE_SIZE;
            }
            int s = Integer.parseInt(param.trim());
            return validatePageSize(s);
        } catch (Exception e) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    /** Validate số trang */
    public static int validatePage(int page) {
        return Math.max(1, page);
    }

    /** Validate kích thước trang (giới hạn tối thiểu 1, tối đa 100) */
    public static int validatePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private PaginationUtil() {}
}
