package com.fruitmkt.util;

/**
 * PaginationHelper — Shared utilities for pagination across DAOs.
 *
 * Consolidates offset calculation, SQL fragment generation, and parameter binding
 * to reduce repetition across all paginated DAO methods.
 *
 * Usage:
 *   int offset = PaginationHelper.calculateOffset(page, pageSize);
 *   String sqlFragment = " ORDER BY id DESC " + PaginationHelper.OFFSET_FETCH_SQL;
 *   ps.setInt(paramIndex++, offset);
 *   ps.setInt(paramIndex++, pageSize);
 *
 * @author fruitmkt-team
 */
public final class PaginationHelper {

    private PaginationHelper() {
        // Utility class — no instances
    }

    /**
     * SQL Server pagination fragment: " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
     * Use after ORDER BY clause.
     *
     * Requires two parameters: offset (int), pageSize (int)
     */
    public static final String OFFSET_FETCH_SQL = " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    /**
     * Calculate offset (0-based row number) from 1-based page and page size.
     *
     * @param page     1-based page number
     * @param pageSize rows per page
     * @return offset (0-based), clamped to >= 0
     */
    public static int calculateOffset(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return Math.max(offset, 0);
    }

    /**
     * Set pagination parameters (offset, pageSize) into a PreparedStatement.
     *
     * @param ps         the PreparedStatement to bind to
     * @param paramIndex 1-based parameter index (incremented after binding)
     * @param page       1-based page number
     * @param pageSize   rows per page
     * @return the next available parameter index (paramIndex + 2)
     * @throws java.sql.SQLException if statement binding fails
     */
    public static int bindOffsetFetch(java.sql.PreparedStatement ps, int paramIndex, int page, int pageSize) throws java.sql.SQLException {
        int offset = calculateOffset(page, pageSize);
        ps.setInt(paramIndex, offset);
        ps.setInt(paramIndex + 1, pageSize);
        return paramIndex + 2;
    }
}
