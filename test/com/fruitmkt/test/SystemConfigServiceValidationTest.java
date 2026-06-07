package com.fruitmkt.test;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.service.SystemConfigService;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SystemConfigServiceValidationTest {

    private final SystemConfigService service = new SystemConfigService();

    @Test
    public void rejectFeeAboveHundredPercent() {
        assertIllegalArgument(() ->
                service.updateConfig(AppConfig.CONFIG_PLATFORM_FEE_RATE, "150", LocalDateTime.now(), 1, "test"),
                "Tỷ lệ phí nền tảng không được vượt quá 100%.");
    }

    @Test
    public void rejectInvalidLogoUrl() {
        assertIllegalArgument(() ->
                service.updateConfig("WEBSITE_LOGO_URL", "ftp://bad/logo.png", LocalDateTime.now(), 1, "test"),
                "Đường dẫn logo phải là URL hợp lệ hoặc đường dẫn tương đối bắt đầu bằng '/'.");
    }

    @Test
    public void rejectPastEffectiveDate() {
        assertIllegalArgument(() ->
                service.updateConfig(AppConfig.CONFIG_FREEZE_DAYS, "15", LocalDateTime.now().minusDays(1), 1, "test"),
                "Effective date cannot be in the past");
    }

    @Test
    public void rejectInvalidChangedBy() {
        assertIllegalArgument(() ->
                service.updateConfig(AppConfig.CONFIG_ACCEPT_TIMEOUT_MIN, "30", LocalDateTime.now(), 0, "test"),
                "Người cập nhật không hợp lệ.");
    }

    private void assertIllegalArgument(ThrowingRunnable runnable, String expectedMessage) {
        try {
            runnable.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals(normalizeMessage(expectedMessage), normalizeMessage(ex.getMessage()));
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        String normalized = message.trim();
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
