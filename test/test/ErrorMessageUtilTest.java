package test;

import exception.BusinessException;
import org.junit.Test;
import util.ErrorMessageUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErrorMessageUtilTest {

    @Test
    public void businessException_shouldReturnOriginalMessage_andBeUserError() {
        BusinessException ex = new BusinessException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng #12");

        assertEquals("Không tìm thấy đơn hàng #12", ErrorMessageUtil.getUserMessage(ex));
        assertTrue(ErrorMessageUtil.isUserError(ex));
    }

    @Test
    public void withReference_shouldAppendRequestIdSafely() {
        assertEquals("Lỗi hệ thống (Mã tham chiếu: abc123)",
                ErrorMessageUtil.withReference("Lỗi hệ thống", "abc123"));
    }

    @Test
    public void sanitizeForLog_shouldStripControlCharacters() {
        assertEquals("line1_line2_line3", ErrorMessageUtil.sanitizeForLog("line1\nline2\rline3"));
    }
}
