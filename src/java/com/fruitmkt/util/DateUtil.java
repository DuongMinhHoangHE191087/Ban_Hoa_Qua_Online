package com.fruitmkt.util;

/**
 * DateUtil — /** Tiện ích format và parse ngày tháng cho JSP và DB. */
 * @author fruitmkt-team
 */
public final class DateUtil {

        /** Format LocalDateTime sang 'dd/MM/yyyy HH:mm' */
        public static String format(java.time.LocalDateTime dt) { throw new UnsupportedOperationException(); }
        /** Format LocalDate sang 'dd/MM/yyyy' */
        public static String format(java.time.LocalDate d) { throw new UnsupportedOperationException(); }
        /** Parse String 'dd/MM/yyyy' sang LocalDate */
        public static java.time.LocalDate parseDate(String s) { throw new UnsupportedOperationException(); }
        /** Lấy LocalDateTime từ java.sql.Timestamp (dùng trong mapRow của DAO) */
        public static java.time.LocalDateTime fromTimestamp(java.sql.Timestamp ts) {
            return ts == null ? null : ts.toLocalDateTime();
        }

    private DateUtil() {}
}
