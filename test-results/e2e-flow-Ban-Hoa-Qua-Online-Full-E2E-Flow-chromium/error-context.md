# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e-flow.spec.ts >> Ban Hoa Qua Online Full E2E Flow
- Location: playwright-tests\e2e-flow.spec.ts:18:5

# Error details

```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:8080/Ban_Hoa_Qua_Online/auth/register
Call log:
  - navigating to "http://localhost:8080/Ban_Hoa_Qua_Online/auth/register", waiting until "load"

```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | import { MailTMClient } from './mailtm';
  3   | import * as path from 'path';
  4   | 
  5   | /**
  6   |  * Full E2E Flow for Ban Hoa Qua Online
  7   |  * 
  8   |  * Actor separation:
  9   |  *  - MailTM email  → new account that registers then upgrades to SHOP_OWNER
  10  |  *  - customer1@fruitshop.local  → seeded CUSTOMER who places the order
  11  |  *  - owner1@fruitshop.local     → seeded SHOP_OWNER who processes the order
  12  |  *  - delivery@fruitshop.local   → seeded DELIVERY who picks up and delivers
  13  |  *  - admin@fruitshop.local      → ADMIN who approves the shop
  14  |  *
  15  |  * NOTE: SHOP_OWNER cannot call CheckoutServlet (blocked at servlet level).
  16  |  *       Keep ordering under a CUSTOMER account.
  17  |  */
  18  | test('Ban Hoa Qua Online Full E2E Flow', async ({ page, request }) => {
  19  |   test.setTimeout(300000); // 5 minutes
  20  | 
  21  |   page.on('console', msg => {
  22  |     if (msg.type() === 'error') console.log(`BROWSER ERROR: ${msg.text()}`);
  23  |   });
  24  |   page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  25  | 
  26  |   const mailtmClient = new MailTMClient();
  27  |   const tempEmail = await mailtmClient.initialize();
  28  |   const password = 'Password@2026_Secure';
  29  |   const fullName = 'Nguyễn Test E2E';
  30  |   const phone = '09' + Math.floor(10000000 + Math.random() * 90000000);
  31  |   const shopName = 'Shop E2E Organic ' + Math.floor(1000 + Math.random() * 9000);
  32  |   const businessEmail = `e2ebiz_${Math.floor(10000 + Math.random() * 90000)}@gmail.com`;
  33  | 
  34  |   const CUSTOMER_EMAIL = 'customer1@fruitshop.local';
  35  |   const CUSTOMER_PASSWORD = '123456';
  36  | 
  37  |   console.log(`=== E2E Flow ===`);
  38  |   console.log(`  Shop applicant: ${tempEmail}`);
  39  |   console.log(`  Order customer: ${CUSTOMER_EMAIL}`);
  40  |   console.log(`  Shop: ${shopName}`);
  41  | 
  42  |   // ==========================================
  43  |   // Step 1: New Customer Account Registration + Email Verification
  44  |   // ==========================================
  45  |   console.log('\n[Step 1] Account Registration & Email Verification...');
> 46  |   await page.goto('auth/register');
      |              ^ Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:8080/Ban_Hoa_Qua_Online/auth/register
  47  |   await expect(page).toHaveTitle(/Đăng ký tài khoản/);
  48  | 
  49  |   await page.fill('#fullName', fullName);
  50  |   await page.fill('#phone', phone);
  51  |   await page.fill('#email', tempEmail);
  52  |   await page.fill('#password', password);
  53  |   await page.fill('#confirmPassword', password);
  54  |   await page.check('#terms');
  55  | 
  56  |   await page.click('button[type="submit"]');
  57  | 
  58  |   try {
  59  |     await page.waitForURL('**/auth/verify', { timeout: 20000 });
  60  |   } catch (e) {
  61  |     const errs = await page.locator('.bg-red-50, .text-red-600, .alert-danger').allTextContents();
  62  |     console.log('Registration errors:', errs);
  63  |     await page.screenshot({ path: path.join(__dirname, 'step1-reg-fail.png') });
  64  |     throw e;
  65  |   }
  66  | 
  67  |   await expect(page.locator('h1')).toContainText('Xác minh email');
  68  | 
  69  |   const verificationCode = await mailtmClient.waitForVerificationCode(90000);
  70  |   console.log(`[Step 1] Code: ${verificationCode}`);
  71  | 
  72  |   await page.fill('#code', verificationCode);
  73  |   await page.click('button[type="submit"]');
  74  | 
  75  |   await page.waitForURL('**/auth/login', { timeout: 20000 });
  76  |   await expect(page.locator('.bg-green-50, .alert-success')).toContainText(/Xác minh email thành công/);
  77  |   console.log('[Step 1] ✓ Registration and verification complete');
  78  | 
  79  |   // ==========================================
  80  |   // Step 2: Login & Apply for Shop Owner Role
  81  |   // ==========================================
  82  |   console.log('\n[Step 2] Login as Customer & Apply for Shop Owner...');
  83  |   await page.fill('#identifier', tempEmail);
  84  |   await page.fill('#password', password);
  85  |   await page.click('button[type="submit"]');
  86  |   await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });
  87  |   console.log('[Step 2] ✓ Logged in as customer');
  88  | 
  89  |   // Shop upgrade form shown to logged-in customers at /auth/register
  90  |   await page.goto('auth/register');
  91  |   await expect(page.locator('h1')).toContainText('Đăng ký mở cửa hàng', { timeout: 10000 });
  92  | 
  93  |   await page.fill('#storeName', shopName);
  94  |   await page.fill('#businessEmail', businessEmail);
  95  |   await page.fill('#address', '123 E2E Street, District 1, HCMC');
  96  | 
  97  |   // Select first category
  98  |   const firstCategory = page.locator('input[name="categoryIds"]').first();
  99  |   await firstCategory.check();
  100 | 
  101 |   // Upload verification document
  102 |   const fileChooserPromise = page.waitForEvent('filechooser');
  103 |   await page.locator('#dropzone').click();
  104 |   const fileChooser = await fileChooserPromise;
  105 |   await fileChooser.setFiles(path.join(__dirname, 'dummy-doc.pdf'));
  106 | 
  107 |   await page.check('#terms');
  108 |   await page.click('button[type="submit"]');
  109 | 
  110 |   await page.waitForURL('**/shop/status', { timeout: 20000 });
  111 |   await expect(page.locator('h1')).toContainText(/Hồ sơ đang được xét duyệt/);
  112 |   console.log('[Step 2] ✓ Shop application submitted');
  113 | 
  114 |   await page.goto('auth/logout');
  115 | 
  116 |   // ==========================================
  117 |   // Step 3: Admin Approves Shop Application
  118 |   // ==========================================
  119 |   console.log('\n[Step 3] Admin approving shop application...');
  120 |   await page.goto('auth/login');
  121 |   await page.fill('#identifier', 'admin@fruitshop.local');
  122 |   await page.fill('#password', '123456');
  123 |   await page.click('button[type="submit"]');
  124 |   await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });
  125 | 
  126 |   await page.goto('admin/shops');
  127 |   await expect(page.locator('h1')).toContainText('Phê Duyệt Cửa Hàng', { timeout: 10000 });
  128 | 
  129 |   // Find our shop row
  130 |   const shopRow = page.locator(`tr:has-text("${shopName}")`);
  131 |   await expect(shopRow).toBeVisible({ timeout: 10000 });
  132 | 
  133 |   // Click approve button in that row
  134 |   const approveBtn = shopRow.locator(
  135 |     'button[title="Duyệt cửa hàng"], button:has(.fa-check), button:has-text("Duyệt")'
  136 |   ).first();
  137 |   await approveBtn.click();
  138 | 
  139 |   // Handle SweetAlert2 confirmation
  140 |   const swal2Confirm = page.locator('button.swal2-confirm');
  141 |   await expect(swal2Confirm).toBeVisible({ timeout: 8000 });
  142 |   await swal2Confirm.click();
  143 | 
  144 |   // Verify success in SweetAlert2
  145 |   await expect(page.locator('.swal2-popup')).toContainText(/thành công/i, { timeout: 10000 });
  146 | 
```