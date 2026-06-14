package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;

/**
 * DateUtil — Xử lý định dạng và chuyển đổi các kiểu dữ liệu ngày tháng.
 *
 * @author fruitmkt-team
 */
public final class DateUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Format LocalDateTime sang 'dd/MM/yyyy HH:mm' */
    public static String format(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FORMATTER);
    }

    /** Format LocalDate sang 'dd/MM/yyyy' */
    public static String format(LocalDate d) {
        return d == null ? "" : d.format(DATE_FORMATTER);
    }

    /** Parse String 'dd/MM/yyyy' hoặc 'yyyy-MM-dd' sang LocalDate */
    public static LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(s.trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /** Lấy LocalDateTime từ java.sql.Timestamp */
    public static LocalDateTime fromTimestamp(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private DateUtil() {}
}
