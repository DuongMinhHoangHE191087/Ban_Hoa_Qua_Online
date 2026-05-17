package com.fruitmkt.util;

/**
 * PaginationUtil — /** Tính toán offset và tổng trang cho phân trang. */
 * @author fruitmkt-team
 */
public final class PaginationUtil {

        /**
         * Tính offset (dùng trong SQL OFFSET ? ROWS)
         * @param page     Trang hiện tại (1-based)
         * @param pageSize Số record/trang
         */
        public static int getOffset(int page, int pageSize) {
            return Math.max(0, (page - 1) * pageSize);
        }
        /**
         * Tính tổng số trang
         * @param totalItems Tổng số record
         * @param pageSize   Số record/trang
         */
        public static int getTotalPages(long totalItems, int pageSize) {
            return (int) Math.ceil((double) totalItems / pageSize);
        }
        /** Parse page param từ request, mặc định trang 1 */
        public static int parsePage(String param) {
            try { int p = Integer.parseInt(param); return p > 0 ? p : 1; }
            catch (Exception e) { return 1; }
        }

    private PaginationUtil() {}
}
