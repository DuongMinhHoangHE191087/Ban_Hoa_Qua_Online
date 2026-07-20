import { test, expect, type Page } from '@playwright/test';

/**
 * Security demo — Admin access control (AuthN + AuthZ)
 *
 * Ánh xạ code thật của app:
 *  - AuthFilter  : chưa login → redirect /auth/login?redirect=...
 *  - RoleFilter  : /admin/* chỉ cho role ADMIN; role khác → HTTP 403
 *  - LoginServlet: admin login thành công → /admin/dashboard
 *  - Seed data   : admin@fruitshop.local / 123456
 *
 * Đây là security regression (kiểm soát truy cập), KHÔNG phải DAST scanner.
 */

const ADMIN_EMAIL = 'admin@fruitshop.local';
const ADMIN_PASSWORD = '123456';
const CUSTOMER_EMAIL = 'customer1@fruitshop.local';
const CUSTOMER_PASSWORD = '123456';

/** Form login thật: #identifier, #password, hidden _csrf (browser tự submit) */
async function loginAs(page: Page, identifier: string, password: string): Promise<void> {
  await page.goto('auth/login');
  await expect(page).toHaveTitle(/Đăng nhập/i);
  await page.fill('#identifier', identifier);
  await page.fill('#password', password);
  await page.click('button[type="submit"]');
}

test.describe('Bảo mật truy cập Admin (AuthN / AuthZ)', () => {
  test.describe.configure({ mode: 'serial' });

  // --------------------------------------------------------------------------
  // Case 1 — Chưa login: AuthFilter chặn /admin/*
  // --------------------------------------------------------------------------
  test('Chưa đăng nhập → bị chặn khỏi /admin/dashboard (redirect login)', async ({ page }) => {
    // Guest context: không cookie/session
    await page.goto('admin/dashboard');

    // AuthFilter: sendRedirect(contextPath + "/auth/login?redirect=" + encodedRedirect)
    await expect(page).toHaveURL(/\/auth\/login/i, { timeout: 15000 });

    // Phải kèm redirect để sau login quay lại trang admin ban đầu
    const url = new URL(page.url());
    expect(url.searchParams.get('redirect')).toBeTruthy();
    expect(decodeURIComponent(url.searchParams.get('redirect') || '')).toMatch(/\/admin\/dashboard/i);

    // Đang ở form login, chưa phải trang admin
    await expect(page.locator('#identifier')).toBeVisible();
    await expect(page.locator('#password')).toBeVisible();
    await expect(page.locator('h1')).toContainText(/Đăng nhập/i);

    // Không lộ nội dung admin
    await expect(page.locator('h1')).not.toContainText(/Tổng Quan Hệ Thống/i);
  });

  test('Chưa đăng nhập → bị chặn khỏi /admin/shops', async ({ page }) => {
    await page.goto('admin/shops');
    await expect(page).toHaveURL(/\/auth\/login/i, { timeout: 15000 });
    const url = new URL(page.url());
    expect(decodeURIComponent(url.searchParams.get('redirect') || '')).toMatch(/\/admin\/shops/i);
  });

  // --------------------------------------------------------------------------
  // Case 2 — Login ADMIN thành công: không bị chặn, vào được dashboard
  // --------------------------------------------------------------------------
  test('Login ADMIN thành công → vào được /admin/dashboard', async ({ page }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);

    // ShopStatusRedirectUtil.redirectToRoleHome → /admin/dashboard
    await expect(page).toHaveURL(/\/admin\/dashboard/i, { timeout: 20000 });
    await expect(page).toHaveTitle(/Tổng quan Admin/i);
    await expect(page.locator('h1')).toContainText(/Tổng Quan Hệ Thống/i, { timeout: 10000 });

    // Session còn hiệu lực: điều hướng tiếp trang admin khác không bị đá về login
    await page.goto('admin/shops');
    await expect(page).toHaveURL(/\/admin\/shops/i, { timeout: 15000 });
    await expect(page).not.toHaveURL(/\/auth\/login/i);
    await expect(page.locator('h1')).toContainText(/Phê Duyệt Cửa Hàng|Cửa Hàng|Shop/i, {
      timeout: 10000,
    });
  });

  // --------------------------------------------------------------------------
  // Case 3 — Guest mở admin → redirect login kèm ?redirect= → login admin → vào đúng đích
  // (mô phỏng flow thực tế: user gõ URL admin trước, rồi mới login)
  // --------------------------------------------------------------------------
  test('Flow redirect: guest vào admin → login admin → được đưa vào trang đích', async ({ page }) => {
    await page.goto('admin/dashboard');
    await expect(page).toHaveURL(/\/auth\/login/i, { timeout: 15000 });

    // Login ngay trên trang đã có hidden input redirect (login.jsp)
    await page.fill('#identifier', ADMIN_EMAIL);
    await page.fill('#password', ADMIN_PASSWORD);
    await page.click('button[type="submit"]');

    // LoginServlet: redirectTarget an toàn (bắt đầu bằng /) → sendRedirect(cleanTarget)
    await expect(page).toHaveURL(/\/admin\/dashboard/i, { timeout: 20000 });
    await expect(page.locator('h1')).toContainText(/Tổng Quan Hệ Thống/i, { timeout: 10000 });
  });

  // --------------------------------------------------------------------------
  // Case 4 — Login CUSTOMER: AuthN OK nhưng AuthZ (RoleFilter) chặn /admin/*
  // --------------------------------------------------------------------------
  test('Login CUSTOMER → vẫn bị chặn /admin/* (403 RoleFilter)', async ({ page }) => {
    await loginAs(page, CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

    // Customer thường về trang chủ (không phải admin)
    await page.waitForURL((url) => !url.toString().includes('/auth/login'), { timeout: 20000 });
    await expect(page).not.toHaveURL(/\/admin\//i);

    // Cố vào admin → RoleFilter: sendError(403) vì !isAdmin
    const response = await page.goto('admin/dashboard', { waitUntil: 'domcontentloaded' });
    const status = response?.status() ?? 0;
    const finalUrl = page.url();
    const bodyText = await page.locator('body').innerText().catch(() => '');

    const blockedByRole =
      status === 403 ||
      /403|Forbidden|không có quyền|Access Denied|HTTP Status 403/i.test(
        `${finalUrl}\n${bodyText}`
      );

    expect(
      blockedByRole,
      `Customer không được vào admin. status=${status} url=${finalUrl}`
    ).toBeTruthy();

    // Không được render dashboard admin thật
    await expect(page.locator('h1')).not.toContainText(/Tổng Quan Hệ Thống/i);
  });
});
