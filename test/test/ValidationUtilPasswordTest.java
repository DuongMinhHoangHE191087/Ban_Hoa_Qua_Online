package test;

import org.junit.Test;
import static org.junit.Assert.*;
import util.ValidationUtil;

/**
 * ValidationUtilPasswordTest — Bộ kiểm thử JUnit 4 cho ValidationUtil.isValidPassword().
 *
 * Áp dụng kỹ thuật:
 *   - Black-Box: Equivalence Partitioning (EP) & Boundary Value Analysis (BVA)
 *   - White-Box: Statement Coverage & Decision Coverage
 *
 * SWT301 — Phần 2: Unit Test
 * @author fruitmkt-team
 */
public class ValidationUtilPasswordTest {

    // =========================================================================
    //  BLACK-BOX TESTING — Equivalence Partitioning (EP)
    // =========================================================================

    /**
     * EP-01: Lớp tương đương hợp lệ — mật khẩu 8 ký tự (min boundary).
     * Input: "Abcd1234" (8 chars) → Expected: true
     */
    @Test
    public void testEP_ValidPassword_8Chars() {
        assertTrue("EP Valid: 8 chars should be valid",
                ValidationUtil.isValidPassword("Abcd1234"));
    }

    /**
     * EP-02: Lớp tương đương hợp lệ — mật khẩu 20 ký tự (typical valid).
     * Input: "MySecurePassword2026" (20 chars) → Expected: true
     */
    @Test
    public void testEP_ValidPassword_20Chars() {
        assertTrue("EP Valid: 20 chars should be valid",
                ValidationUtil.isValidPassword("MySecurePassword2026"));
    }

    /**
     * EP-03: Lớp tương đương không hợp lệ — null input.
     * Input: null → Expected: false
     */
    @Test
    public void testEP_NullPassword() {
        assertFalse("EP Invalid: null should be invalid",
                ValidationUtil.isValidPassword(null));
    }

    /**
     * EP-04: Lớp tương đương không hợp lệ — chuỗi rỗng.
     * Input: "" (0 chars) → Expected: false
     */
    @Test
    public void testEP_EmptyString() {
        assertFalse("EP Invalid: empty string should be invalid",
                ValidationUtil.isValidPassword(""));
    }

    /**
     * EP-05: Lớp tương đương không hợp lệ — quá ngắn (< 8).
     * Input: "Ab1" (3 chars) → Expected: false
     */
    @Test
    public void testEP_TooShort() {
        assertFalse("EP Invalid: 3 chars should be invalid",
                ValidationUtil.isValidPassword("Ab1"));
    }

    /**
     * EP-06: Lớp tương đương không hợp lệ — quá dài (> 64).
     * Input: 65 ký tự → Expected: false
     */
    @Test
    public void testEP_TooLong() {
        assertFalse("EP Invalid: 65 chars should be invalid",
                ValidationUtil.isValidPassword("A".repeat(65)));
    }

    /**
     * EP-07: Lớp tương đương hợp lệ — đúng 64 ký tự (max boundary).
     * Input: 64 ký tự → Expected: true
     */
    @Test
    public void testEP_MaxLength_64Chars() {
        assertTrue("EP Valid: exactly 64 chars should be valid",
                ValidationUtil.isValidPassword("A".repeat(64)));
    }

    // =========================================================================
    //  BLACK-BOX TESTING — Boundary Value Analysis (BVA)
    // =========================================================================

    /**
     * BVA-01: Dưới biên dưới — 7 ký tự (min - 1).
     * Input: 7 chars → Expected: false
     */
    @Test
    public void testBVA_BelowMin_7Chars() {
        assertFalse("BVA: 7 chars (below min) should be invalid",
                ValidationUtil.isValidPassword("A".repeat(7)));
    }

    /**
     * BVA-02: Tại biên dưới — 8 ký tự (min).
     * Input: 8 chars → Expected: true
     */
    @Test
    public void testBVA_AtMin_8Chars() {
        assertTrue("BVA: 8 chars (at min) should be valid",
                ValidationUtil.isValidPassword("A".repeat(8)));
    }

    /**
     * BVA-03: Trên biên dưới — 9 ký tự (min + 1).
     * Input: 9 chars → Expected: true
     */
    @Test
    public void testBVA_AboveMin_9Chars() {
        assertTrue("BVA: 9 chars (above min) should be valid",
                ValidationUtil.isValidPassword("A".repeat(9)));
    }

    /**
     * BVA-04: Dưới biên trên — 63 ký tự (max - 1).
     * Input: 63 chars → Expected: true
     */
    @Test
    public void testBVA_BelowMax_63Chars() {
        assertTrue("BVA: 63 chars (below max) should be valid",
                ValidationUtil.isValidPassword("A".repeat(63)));
    }

    /**
     * BVA-05: Tại biên trên — 64 ký tự (max).
     * Input: 64 chars → Expected: true
     */
    @Test
    public void testBVA_AtMax_64Chars() {
        assertTrue("BVA: 64 chars (at max) should be valid",
                ValidationUtil.isValidPassword("A".repeat(64)));
    }

    /**
     * BVA-06: Trên biên trên — 65 ký tự (max + 1).
     * Input: 65 chars → Expected: false
     */
    @Test
    public void testBVA_AboveMax_65Chars() {
        assertFalse("BVA: 65 chars (above max) should be invalid",
                ValidationUtil.isValidPassword("A".repeat(65)));
    }

    // =========================================================================
    //  WHITE-BOX TESTING — Statement Coverage
    // =========================================================================
    //
    //  Source code under test (ValidationUtil.java, Lines 75-84):
    //
    //    public static boolean isValidPassword(String pwd) {           // S1
    //        return isValidPassword(pwd, PASSWORD_MIN_LEN, PASSWORD_MAX_LEN); // S2
    //    }
    //    public static boolean isValidPassword(String pwd, int minLen, int maxLen) {
    //        if (pwd == null) return false;       // S3: Decision D1 (pwd == null?)
    //        int len = pwd.length();              // S4
    //        return len >= minLen && len <= maxLen; // S5: Decision D2 & D3
    //    }
    //
    //  => Cần tối thiểu 2 test cases để đạt 100% Statement Coverage.

    /**
     * WB-SC-01: Nhánh null → covers S1, S2, S3 (true branch → return false).
     * Input: null → Expected: false
     * Statements covered: S1, S2, S3
     */
    @Test
    public void testWB_StatementCoverage_NullBranch() {
        assertFalse("WB Statement Coverage: null path covers S1→S2→S3",
                ValidationUtil.isValidPassword(null));
    }

    /**
     * WB-SC-02: Nhánh valid → covers S1, S2, S3 (false), S4, S5.
     * Input: "12345678" (8 chars) → Expected: true
     * Statements covered: S1, S2, S3, S4, S5
     * => Kết hợp với WB-SC-01 → 100% Statement Coverage
     */
    @Test
    public void testWB_StatementCoverage_ValidPath() {
        assertTrue("WB Statement Coverage: valid path covers S1→S2→S3→S4→S5",
                ValidationUtil.isValidPassword("12345678"));
    }

    // =========================================================================
    //  WHITE-BOX TESTING — Decision Coverage
    // =========================================================================
    //
    //  3 Decisions:
    //    D1: pwd == null?     (True → return false, False → continue)
    //    D2: len >= minLen?   (True → check D3, False → return false)
    //    D3: len <= maxLen?   (True → return true, False → return false)
    //
    //  => Cần 4 test cases để đạt 100% Decision Coverage (mỗi branch T/F).

    /**
     * WB-DC-01: D1 = True (pwd is null).
     * Input: null → Expected: false
     * Decisions: D1=True
     */
    @Test
    public void testWB_DecisionCoverage_D1_True() {
        assertFalse("WB Decision Coverage: D1=True (null input)",
                ValidationUtil.isValidPassword(null));
    }

    /**
     * WB-DC-02: D1 = False, D2 = False (password too short).
     * Input: "1234" (4 chars, < 8) → Expected: false
     * Decisions: D1=False, D2=False
     */
    @Test
    public void testWB_DecisionCoverage_D2_False() {
        assertFalse("WB Decision Coverage: D1=False, D2=False (too short)",
                ValidationUtil.isValidPassword("1234"));
    }

    /**
     * WB-DC-03: D1 = False, D2 = True, D3 = True (valid password).
     * Input: "12345678" (8 chars) → Expected: true
     * Decisions: D1=False, D2=True, D3=True
     */
    @Test
    public void testWB_DecisionCoverage_D2D3_True() {
        assertTrue("WB Decision Coverage: D1=False, D2=True, D3=True (valid)",
                ValidationUtil.isValidPassword("12345678"));
    }

    /**
     * WB-DC-04: D1 = False, D2 = True, D3 = False (password too long).
     * Input: 65 chars → Expected: false
     * Decisions: D1=False, D2=True, D3=False
     */
    @Test
    public void testWB_DecisionCoverage_D3_False() {
        assertFalse("WB Decision Coverage: D1=False, D2=True, D3=False (too long)",
                ValidationUtil.isValidPassword("A".repeat(65)));
    }

    // =========================================================================
    //  BỔ SUNG: Test với isValidPassword(pwd, minLen, maxLen) overload
    // =========================================================================

    /**
     * EP-EXT-01: Overloaded method — custom min/max (4, 10).
     */
    @Test
    public void testOverload_CustomBounds_Valid() {
        assertTrue("Overload: 6 chars with min=4, max=10 should be valid",
                ValidationUtil.isValidPassword("abcdef", 4, 10));
    }

    /**
     * EP-EXT-02: Overloaded method — below custom min.
     */
    @Test
    public void testOverload_CustomBounds_BelowMin() {
        assertFalse("Overload: 2 chars with min=4, max=10 should be invalid",
                ValidationUtil.isValidPassword("ab", 4, 10));
    }

    /**
     * EP-EXT-03: Overloaded method — above custom max.
     */
    @Test
    public void testOverload_CustomBounds_AboveMax() {
        assertFalse("Overload: 12 chars with min=4, max=10 should be invalid",
                ValidationUtil.isValidPassword("abcdefghijkl", 4, 10));
    }
}
