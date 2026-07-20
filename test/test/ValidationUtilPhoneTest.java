package test;

import org.junit.Test;
import static org.junit.Assert.*;
import util.ValidationUtil;

/**
 * ValidationUtilPhoneTest — Bộ kiểm thử JUnit 4 cho ValidationUtil.isValidPhone().
 *
 * Áp dụng kỹ thuật:
 *   - Black-Box: Equivalence Partitioning (EP)
 *   - Black-Box: Boundary Value Analysis (BVA)
 *
 * Regex pattern: ^(0|\+84)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-9])[0-9]{7}$
 * Valid format: 10 digits starting with 03x, 05x, 07x, 08x, 09x
 *
 * SWT301 — Phần 2: Unit Test (bổ sung)
 * @author fruitmkt-team
 */
public class ValidationUtilPhoneTest {

    // =========================================================================
    //  EQUIVALENCE PARTITIONING (EP)
    // =========================================================================

    /**
     * EP-01: Hợp lệ — số đầu 09x (Viettel).
     */
    @Test
    public void testEP_Valid_09x() {
        assertTrue("EP Valid: 09x prefix (Viettel)",
                ValidationUtil.isValidPhone("0987654321"));
    }

    /**
     * EP-02: Hợp lệ — số đầu 03x (Viettel mới).
     */
    @Test
    public void testEP_Valid_03x() {
        assertTrue("EP Valid: 03x prefix",
                ValidationUtil.isValidPhone("0321234567"));
    }

    /**
     * EP-03: Hợp lệ — số đầu 05x (Vietnamobile).
     */
    @Test
    public void testEP_Valid_05x() {
        assertTrue("EP Valid: 05x prefix",
                ValidationUtil.isValidPhone("0561234567"));
    }

    /**
     * EP-04: Hợp lệ — số đầu 07x (Mobifone mới).
     */
    @Test
    public void testEP_Valid_07x() {
        assertTrue("EP Valid: 07x prefix",
                ValidationUtil.isValidPhone("0701234567"));
    }

    /**
     * EP-05: Hợp lệ — số đầu 08x (Vinaphone mới).
     */
    @Test
    public void testEP_Valid_08x() {
        assertTrue("EP Valid: 08x prefix",
                ValidationUtil.isValidPhone("0812345678"));
    }

    /**
     * EP-06: Hợp lệ — số bắt đầu bằng +84 (quốc tế).
     */
    @Test
    public void testEP_Valid_Plus84() {
        assertTrue("EP Valid: +84 prefix (international)",
                ValidationUtil.isValidPhone("+84987654321"));
    }

    /**
     * EP-07: Không hợp lệ — null.
     */
    @Test
    public void testEP_NullPhone() {
        assertFalse("EP Invalid: null phone",
                ValidationUtil.isValidPhone(null));
    }

    /**
     * EP-08: Không hợp lệ — chuỗi rỗng.
     */
    @Test
    public void testEP_EmptyPhone() {
        assertFalse("EP Invalid: empty string",
                ValidationUtil.isValidPhone(""));
    }

    /**
     * EP-09: Không hợp lệ — chứa chữ cái.
     */
    @Test
    public void testEP_LettersInPhone() {
        assertFalse("EP Invalid: letters in phone number",
                ValidationUtil.isValidPhone("098abc4321"));
    }

    /**
     * EP-10: Không hợp lệ — đầu số 01x (đã bị xóa khỏi VN).
     */
    @Test
    public void testEP_Invalid_01x() {
        assertFalse("EP Invalid: 01x prefix (obsolete)",
                ValidationUtil.isValidPhone("0123456789"));
    }

    /**
     * EP-11: Không hợp lệ — đầu số 02x (số cố định).
     */
    @Test
    public void testEP_Invalid_02x() {
        assertFalse("EP Invalid: 02x prefix (landline)",
                ValidationUtil.isValidPhone("0212345678"));
    }

    // =========================================================================
    //  BOUNDARY VALUE ANALYSIS (BVA) — Digit count boundaries
    // =========================================================================

    /**
     * BVA-01: Đúng 10 chữ số (valid length).
     */
    @Test
    public void testBVA_Exactly10Digits() {
        assertTrue("BVA: exactly 10 digits should be valid",
                ValidationUtil.isValidPhone("0987654321"));
    }

    /**
     * BVA-02: 9 chữ số — quá ngắn.
     */
    @Test
    public void testBVA_9Digits_TooShort() {
        assertFalse("BVA: 9 digits (too short) should be invalid",
                ValidationUtil.isValidPhone("098765432"));
    }

    /**
     * BVA-03: 11 chữ số — quá dài.
     */
    @Test
    public void testBVA_11Digits_TooLong() {
        assertFalse("BVA: 11 digits (too long) should be invalid",
                ValidationUtil.isValidPhone("09876543210"));
    }

    /**
     * BVA-04: Chỉ có 1 chữ số.
     */
    @Test
    public void testBVA_SingleDigit() {
        assertFalse("BVA: single digit should be invalid",
                ValidationUtil.isValidPhone("0"));
    }

    // =========================================================================
    //  BỔ SUNG: normalizePhone() helper tests
    // =========================================================================

    /**
     * Normalize: +84 prefix → chuẩn hóa thành 0xxx.
     */
    @Test
    public void testNormalize_Plus84Prefix() {
        assertEquals("Normalize +84 to 0",
                "0987654321", ValidationUtil.normalizePhone("+84987654321"));
    }

    /**
     * Normalize: 84 prefix (no plus) → chuẩn hóa thành 0xxx.
     */
    @Test
    public void testNormalize_84Prefix() {
        assertEquals("Normalize 84 to 0",
                "0987654321", ValidationUtil.normalizePhone("84987654321"));
    }

    /**
     * Normalize: null → null.
     */
    @Test
    public void testNormalize_Null() {
        assertNull("Normalize null should return null",
                ValidationUtil.normalizePhone(null));
    }

    /**
     * Normalize: phone with spaces/dashes → stripped.
     */
    @Test
    public void testNormalize_WithSpacesAndDashes() {
        assertEquals("Normalize phone with formatting chars",
                "0987654321", ValidationUtil.normalizePhone("098-765-4321"));
    }
}
