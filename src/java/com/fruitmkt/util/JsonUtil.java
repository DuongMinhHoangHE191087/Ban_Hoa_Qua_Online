package com.fruitmkt.util;

/**
 * JsonUtil — /** Tiện ích JSON dùng Jackson — cho webhook và AJAX response.
 * @author fruitmkt-team
 */
public final class JsonUtil {

        * Đặt jackson-databind-*.jar vào WEB-INF/lib/
        * Download: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind */
        private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
        /** Serialize object thành JSON string */
        public static String toJson(Object obj) throws Exception { return MAPPER.writeValueAsString(obj); }
        /** Deserialize JSON string thành object */
        public static <T> T fromJson(String json, Class<T> clazz) throws Exception { return MAPPER.readValue(json, clazz); }
        /** Ghi JSON response cho AJAX/webhook endpoint */
        public static void writeJson(jakarta.servlet.http.HttpServletResponse resp, Object data) throws java.io.IOException {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(MAPPER.writeValueAsString(data));
        }

    private JsonUtil() {}
}
