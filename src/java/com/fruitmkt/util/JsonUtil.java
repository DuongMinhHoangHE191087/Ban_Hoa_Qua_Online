package com.fruitmkt.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JsonUtil — Tiện ích JSON dùng Jackson cho webhook và AJAX response.
 * 
 * Đặt jackson-databind-*.jar vào WEB-INF/lib/
 * Download: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
 *
 * @author fruitmkt-team
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        module.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        MAPPER.registerModule(module);
    }

    /** Serialize object thành JSON string */
    public static String toJson(Object obj) throws Exception {
        return MAPPER.writeValueAsString(obj);
    }

    /** Deserialize JSON string thành object */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return MAPPER.readValue(json, clazz);
    }

    /** Ghi JSON response cho AJAX/webhook endpoint */
    public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        try {
            resp.getWriter().write(MAPPER.writeValueAsString(data));
        } catch (Exception e) {
            throw new IOException("Lỗi ghi dữ liệu JSON: " + e.getMessage(), e);
        }
    }

    private JsonUtil() {}
}
