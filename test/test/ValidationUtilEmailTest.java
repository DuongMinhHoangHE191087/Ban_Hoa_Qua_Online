package test;

import org.junit.Test;
import static org.junit.Assert.*;
import util.ValidationUtil;

/**
 * ValidationUtilEmailTest — Bộ kiểm thử JUnit 4 cho ValidationUtil.isValidEmail().
 *
 * Áp dụng kỹ thuật:
 *   - Black-Box: Equivalence Partitioning (EP)
 *   - Black-Box: Boundary Value Analysis (BVA) — theo độ dài và ký tự đặc biệt
 *
 * SWT301 — Phần 2: Unit Test (bổ sung)
 * @author fruitmkt-team
 */
public class ValidationUtilEmailTest {

    // =========================================================================
    //  EQUIVALENCE PARTITIONING (EP)
    // =========================================================================

    /**
     * EP-01: Lớp tương đương hợp lệ — email chuẩn.
     */
    @Test
    public void testEP_ValidEmail_Standard() {
        assertTrue("EP Valid: standard email",
                ValidationUtil.isValidEmail("user@example.com"));
    }

    /**
     * EP-02: Lớp tương đương hợp lệ — email với subdomain.
     */
    @Test
    public void testEP_ValidEmail_Subdomain() {
        assertTrue("EP Valid: email with subdomain",
                ValidationUtil.isValidEmail("user@mail.example.com"));
    }

    /**
     * EP-03: Lớp tương đương hợp lệ — email với dấu chấm trong local part.
     */
    @Test
    public void testEP_ValidEmail_DotInLocal() {
        assertTrue("EP Valid: dot in local part",
                ValidationUtil.isValidEmail("first.last@example.com"));
    }

    /**
     * EP-04: Lớp tương đương hợp lệ — email với dấu + (plus addressing).
     */
    @Test
    public void testEP_ValidEmail_PlusSign() {
        assertTrue("EP Valid: plus sign in local part",
                ValidationUtil.isValidEmail("user+tag@example.com"));
    }

    /**
     * EP-05: Lớp tương đương không hợp lệ — null input.
     */
    @Test
    public void testEP_NullEmail() {
        assertFalse("EP Invalid: null email",
                ValidationUtil.isValidEmail(null));
    }

    /**
     * EP-06: Lớp tương đương không hợp lệ — chuỗi rỗng.
     */
    @Test
    public void testEP_EmptyEmail() {
        assertFalse("EP Invalid: empty string",
                ValidationUtil.isValidEmail(""));
    }

    /**
     * EP-07: Lớp tương đương không hợp lệ — thiếu @.
     */
    @Test
    public void testEP_MissingAtSign() {
        assertFalse("EP Invalid: missing @ sign",
                ValidationUtil.isValidEmail("userexample.com"));
    }

    /**
     * EP-08: Lớp tương đương không hợp lệ — thiếu domain.
     */
    @Test
    public void testEP_MissingDomain() {
        assertFalse("EP Invalid: missing domain part",
                ValidationUtil.isValidEmail("user@"));
    }

    /**
     * EP-09: Lớp tương đương không hợp lệ — thiếu local part.
     */
    @Test
    public void testEP_MissingLocalPart() {
        assertFalse("EP Invalid: missing local part",
                ValidationUtil.isValidEmail("@example.com"));
    }

    /**
     * EP-10: Lớp tương đương không hợp lệ — dấu chấm đầu local part.
     * Theo regex pattern: ^[A-Za-z0-9] — phải bắt đầu bằng alphanumeric.
     */
    @Test
    public void testEP_LeadingDotLocalPart() {
        assertFalse("EP Invalid: leading dot in local part",
                ValidationUtil.isValidEmail(".user@example.com"));
    }

    /**
     * EP-11: Lớp tương đương không hợp lệ — thiếu TLD (top-level domain).
     */
    @Test
    public void testEP_MissingTLD() {
        assertFalse("EP Invalid: domain without TLD",
                ValidationUtil.isValidEmail("user@example"));
    }

    /**
     * EP-12: Lớp tương đương không hợp lệ — khoảng trắng trong email.
     */
    @Test
    public void testEP_SpacesInEmail() {
        assertFalse("EP Invalid: spaces in email",
                ValidationUtil.isValidEmail("user @example.com"));
    }

    // =========================================================================
    //  BOUNDARY VALUE ANALYSIS (BVA) — TLD length boundaries
    // =========================================================================

    /**
     * BVA-01: TLD tối thiểu 2 ký tự — "vn" → hợp lệ.
     */
    @Test
    public void testBVA_TLD_MinLength_2Chars() {
        assertTrue("BVA: TLD 2 chars (vn) should be valid",
                ValidationUtil.isValidEmail("user@example.vn"));
    }

    /**
     * BVA-02: TLD 1 ký tự — "x" → không hợp lệ (theo regex: [A-Za-z]{2,}).
     */
    @Test
    public void testBVA_TLD_BelowMin_1Char() {
        assertFalse("BVA: TLD 1 char should be invalid",
                ValidationUtil.isValidEmail("user@example.x"));
    }

    /**
     * BVA-03: TLD dài — "museum" (6 chars) → hợp lệ.
     */
    @Test
    public void testBVA_TLD_LongTLD() {
        assertTrue("BVA: TLD 6 chars (museum) should be valid",
                ValidationUtil.isValidEmail("user@example.museum"));
    }

    /**
     * BVA-04: Email rất ngắn nhưng hợp lệ — "a@b.co" (6 chars).
     */
    @Test
    public void testBVA_MinimalValidEmail() {
        assertTrue("BVA: minimal valid email 'a@b.co'",
                ValidationUtil.isValidEmail("a@b.co"));
    }
}
