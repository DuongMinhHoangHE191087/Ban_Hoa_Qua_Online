package com.fruitmkt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ApiResponse<T> — Phong bì JSON chuẩn cho mọi API endpoint.
 *
 * DEPRECATED: Use {@link com.fruitmkt.model.response.ApiResponse} instead.
 * This class is kept for compatibility; all new code should use the standardized
 * location in com.fruitmkt.model.response.
 *
 * Mọi endpoint nên trả về cùng cấu trúc để frontend chỉ cần MỘT đường
 * parse/xử-lý-lỗi thay vì xử lý riêng từng endpoint.
 *
 * Cấu trúc JSON:
 *   { "success": true,  "data": {...}, "meta": {...} }   // thành công
 *   { "success": false, "error": "..." }                  // thất bại
 *
 * Các field null bị loại bỏ khi serialize (@JsonInclude NON_NULL) để
 * giữ payload gọn — phản hồi thành công không có "error", và ngược lại.
 *
 * Bất biến (record). Serialize qua JsonUtil (Jackson hỗ trợ record từ 2.12+).
 *
 * @param <T> kiểu của payload dữ liệu
 * @author fruitmkt-team
 * @deprecated Use {@link com.fruitmkt.model.response.ApiResponse}
 */
@Deprecated(since = "1.0", forRemoval = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String error, Object meta) {

    /** Thành công, chỉ có dữ liệu. */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** Thành công, kèm metadata (vd: phân trang PageMeta). */
    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    /** Thất bại với thông điệp lỗi (dùng cho 4xx/5xx). */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    /**
     * Thất bại kèm mã trạng thái HTTP nhúng trong meta để client/log truy vết.
     * Lưu ý: KHÔNG tự set HTTP status của response — caller phải gọi
     * resp.setStatus(statusCode) riêng. Đây chỉ là dữ liệu mang theo.
     */
    public static <T> ApiResponse<T> fail(int statusCode, String message) {
        return new ApiResponse<>(false, null, message, java.util.Map.of("statusCode", statusCode));
    }
}
