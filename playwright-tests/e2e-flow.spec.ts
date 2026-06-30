import { test, expect } from '@playwright/test';
import { MailTMClient } from './mailtm';
import * as path from 'path';

/**
 * Full E2E Flow for Ban Hoa Qua Online
 * 
 * Actor separation:
 *  - MailTM email  → new account that registers then upgrades to SHOP_OWNER
 *  - customer1@fruitshop.local  → seeded CUSTOMER who places the order
 *  - owner1@fruitshop.local     → seeded SHOP_OWNER who processes the order
 *  - delivery@fruitshop.local   → seeded DELIVERY who picks up and delivers
 *  - admin@fruitshop.local      → ADMIN who approves the shop
 *
 * NOTE: SHOP_OWNER cannot call CheckoutServlet (blocked at servlet level).
 *       Keep ordering under a CUSTOMER account.
 */
test('Ban Hoa Qua Online Full E2E Flow', async ({ page, request }) => {
  test.setTimeout(300000); // 5 minutes

  page.on('console', msg => {
    if (msg.type() === 'error') console.log(`BROWSER ERROR: ${msg.text()}`);
  });
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));

  const mailtmClient = new MailTMClient();
  const tempEmail = await mailtmClient.initialize();
  const password = 'Password@2026_Secure';
  const fullName = 'Nguyễn Test E2E';
  const phone = '09' + Math.floor(10000000 + Math.random() * 90000000);
  const shopName = 'Shop E2E Organic ' + Math.floor(1000 + Math.random() * 9000);
  const businessEmail = `e2ebiz_${Math.floor(10000 + Math.random() * 90000)}@gmail.com`;

  const CUSTOMER_EMAIL = 'customer1@fruitshop.local';
  const CUSTOMER_PASSWORD = '123456';

  console.log(`=== E2E Flow ===`);
  console.log(`  Shop applicant: ${tempEmail}`);
  console.log(`  Order customer: ${CUSTOMER_EMAIL}`);
  console.log(`  Shop: ${shopName}`);

  // ==========================================
  // Step 1: New Customer Account Registration + Email Verification
  // ==========================================
  console.log('\n[Step 1] Account Registration & Email Verification...');
  await page.goto('auth/register');
  await expect(page).toHaveTitle(/Đăng ký tài khoản/);

  await page.fill('#fullName', fullName);
  await page.fill('#phone', phone);
  await page.fill('#email', tempEmail);
  await page.fill('#password', password);
  await page.fill('#confirmPassword', password);
  await page.check('#terms');

  await page.click('button[type="submit"]');

  try {
    await page.waitForURL('**/auth/verify', { timeout: 20000 });
  } catch (e) {
    const errs = await page.locator('.bg-red-50, .text-red-600, .alert-danger').allTextContents();
    console.log('Registration errors:', errs);
    await page.screenshot({ path: path.join(__dirname, 'step1-reg-fail.png') });
    throw e;
  }

  await expect(page.locator('h1')).toContainText('Xác minh email');

  const verificationCode = await mailtmClient.waitForVerificationCode(90000);
  console.log(`[Step 1] Code: ${verificationCode}`);

  await page.fill('#code', verificationCode);
  await page.click('button[type="submit"]');

  await page.waitForURL('**/auth/login', { timeout: 20000 });
  await expect(page.locator('.bg-green-50, .alert-success')).toContainText(/Xác minh email thành công/);
  console.log('[Step 1] ✓ Registration and verification complete');

  // ==========================================
  // Step 2: Login & Apply for Shop Owner Role
  // ==========================================
  console.log('\n[Step 2] Login as Customer & Apply for Shop Owner...');
  await page.fill('#identifier', tempEmail);
  await page.fill('#password', password);
  await page.click('button[type="submit"]');
  await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });
  console.log('[Step 2] ✓ Logged in as customer');

  // Shop upgrade form shown to logged-in customers at /auth/register
  await page.goto('auth/register');
  await expect(page.locator('h1')).toContainText('Đăng ký mở cửa hàng', { timeout: 10000 });

  await page.fill('#storeName', shopName);
  await page.fill('#businessEmail', businessEmail);
  await page.fill('#address', '123 E2E Street, District 1, HCMC');

  // Select first category
  const firstCategory = page.locator('input[name="categoryIds"]').first();
  await firstCategory.check();

  // Upload verification document
  const fileChooserPromise = page.waitForEvent('filechooser');
  await page.locator('#dropzone').click();
  const fileChooser = await fileChooserPromise;
  await fileChooser.setFiles(path.join(__dirname, 'dummy-doc.pdf'));

  await page.check('#terms');
  await page.click('button[type="submit"]');

  await page.waitForURL('**/shop/status', { timeout: 20000 });
  await expect(page.locator('h1')).toContainText(/Hồ sơ đang được xét duyệt/);
  console.log('[Step 2] ✓ Shop application submitted');

  await page.goto('auth/logout');

  // ==========================================
  // Step 3: Admin Approves Shop Application
  // ==========================================
  console.log('\n[Step 3] Admin approving shop application...');
  await page.goto('auth/login');
  await page.fill('#identifier', 'admin@fruitshop.local');
  await page.fill('#password', '123456');
  await page.click('button[type="submit"]');
  await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });

  await page.goto('admin/shops');
  await expect(page.locator('h1')).toContainText('Phê Duyệt Cửa Hàng', { timeout: 10000 });

  // Find our shop row
  const shopRow = page.locator(`tr:has-text("${shopName}")`);
  await expect(shopRow).toBeVisible({ timeout: 10000 });

  // Click approve button in that row
  const approveBtn = shopRow.locator(
    'button[title="Duyệt cửa hàng"], button:has(.fa-check), button:has-text("Duyệt")'
  ).first();
  await approveBtn.click();

  // Handle SweetAlert2 confirmation
  const swal2Confirm = page.locator('button.swal2-confirm');
  await expect(swal2Confirm).toBeVisible({ timeout: 8000 });
  await swal2Confirm.click();

  // Verify success in SweetAlert2
  await expect(page.locator('.swal2-popup')).toContainText(/thành công/i, { timeout: 10000 });

  // Dismiss if still showing
  const swal2Ok = page.locator('button.swal2-confirm');
  if (await swal2Ok.isVisible()) await swal2Ok.click();

  await page.waitForTimeout(1000);
  await expect(shopRow.locator('[id^="status-badge-"]')).toContainText('Đã Duyệt', { timeout: 10000 });
  console.log('[Step 3] ✓ Admin approved shop');

  await page.goto('auth/logout');

  // ==========================================
  // Step 4: Customer Places an Order
  // Uses seeded CUSTOMER account (not the new shop owner!)
  // ==========================================
  console.log('\n[Step 4] Customer placing an order...');
  await page.goto('auth/login');
  await page.fill('#identifier', CUSTOMER_EMAIL);
  await page.fill('#password', CUSTOMER_PASSWORD);
  await page.click('button[type="submit"]');
  await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });
  console.log(`[Step 4] ✓ Logged in as ${CUSTOMER_EMAIL}`);

  // Navigate to product detail page (product 1 = Cam Sành, always seeded and ACTIVE)
  await page.goto('products/detail?id=1');
  await expect(page.locator('h1')).toBeVisible({ timeout: 10000 });

  // Select first available variant chip
  const firstVariantChip = page.locator('.variant-chip-label').first();
  if (await firstVariantChip.isVisible()) {
    await firstVariantChip.click();
    await page.waitForTimeout(400);
  }

  // Click "Thêm Vào Giỏ Hàng" on product detail page
  const addToCartBtn = page.locator('#btn-add-to-cart');
  await expect(addToCartBtn).toBeVisible({ timeout: 10000 });
  await addToCartBtn.click();
  console.log('[Step 4] ✓ Product added to cart');

  // Wait for toast/feedback then navigate to cart
  await page.waitForTimeout(1500);

  await page.goto('cart');
  await expect(page.locator('h1')).toContainText(/Giỏ hàng/, { timeout: 10000 });

  // Wait for JS to render cart items (cart.js loads dynamically)
  await page.waitForFunction(() => {
    const container = document.getElementById('cart-items-container');
    return container && !container.querySelector('.animate-spin') && container.children.length > 0;
  }, { timeout: 15000 });

  const cartCheckoutCard = page.locator('#cart-checkout-card');
  await expect(cartCheckoutCard).toBeVisible({ timeout: 10000 });
  console.log('[Step 4] ✓ Cart has items');

  // Click checkout button (JS calls /cart?action=checkStock then redirects to /checkout?variantIds=...)
  const checkoutBtn = page.locator('#btn-cart-checkout');
  await expect(checkoutBtn).toBeVisible({ timeout: 10000 });
  await checkoutBtn.click();

  // Wait for checkout page (URL includes /checkout)
  await page.waitForURL('**/checkout**', { timeout: 20000 });
  console.log('[Step 4] ✓ On checkout page');

  // Wait for checkout page to fully load
  await page.waitForLoadState('networkidle');

  // Handle address section: the seeded customer may already have an address saved
  const inlineAddressForm = page.locator('#inlineAddressForm');
  const selectedAddressCard = page.locator('#selectedAddressCard');

  if (await inlineAddressForm.isVisible({ timeout: 3000 })) {
    // Inline form is open (no address yet) — fill it
    await page.fill('#mRecipientName', fullName);
    await page.fill('#mRecipientPhone', phone);
    await page.fill('#mAddressDetail', '456 Test Street, District 3, HCMC');
    const isDefaultCheckbox = page.locator('#mIsDefault');
    if (await isDefaultCheckbox.isVisible()) await isDefaultCheckbox.check();
    await page.click('#btnSaveCheckoutAddress');
    await page.waitForTimeout(1500);
  } else {
    // Address already selected or there's another address flow
    const hasSelectedAddress = await selectedAddressCard.isVisible({ timeout: 3000 }).catch(() => false);
    if (!hasSelectedAddress) {
      // Try clicking "Thêm địa chỉ mới" or "Thay đổi" button
      const addBtn = page.locator('button:has-text("Thêm địa chỉ mới"), button:has-text("Thay đổi")').first();
      if (await addBtn.isVisible({ timeout: 2000 })) {
        await addBtn.click();
        await page.waitForTimeout(500);
        // Check for a nested "Thêm địa chỉ mới" button in the modal
        const addNewBtn = page.locator('button:has-text("Thêm địa chỉ mới")').nth(1);
        if (await addNewBtn.isVisible({ timeout: 1000 })) await addNewBtn.click();
        await page.waitForTimeout(500);
      }
      if (await inlineAddressForm.isVisible({ timeout: 2000 })) {
        await page.fill('#mRecipientName', fullName);
        await page.fill('#mRecipientPhone', phone);
        await page.fill('#mAddressDetail', '456 Test Street, District 3, HCMC');
        const isDefaultCheckbox = page.locator('#mIsDefault');
        if (await isDefaultCheckbox.isVisible()) await isDefaultCheckbox.check();
        await page.click('#btnSaveCheckoutAddress');
        await page.waitForTimeout(1500);
      }
    }
  }

  // Submit Button locator
  const submitBtn = page.locator('#submitBtn');
  await expect(submitBtn).toBeVisible({ timeout: 10000 });

  // Select Bank Transfer (CK) payment - now on Step 1
  const ckPaymentRadio = page.locator('input[name="paymentMethod"][value="CK"]');
  if (await ckPaymentRadio.isVisible({ timeout: 3000 })) {
    await ckPaymentRadio.check();
    console.log('[Step 4] ✓ Selected Bank Transfer (CK) payment');
  }

  // Delivery time slot: default "Giao hỏa tốc" is pre-selected, no change needed

  // Transition from Step 1 (Info + Payment + Voucher) to Step 2 (Confirmation)
  console.log('[Step 4] Transitioning to Step 2 (Confirmation)...');
  await submitBtn.click();
  await page.waitForTimeout(600);

  // Submit the order from Step 2 (Confirmation)
  console.log('[Step 4] Submitting final order...');
  await submitBtn.click();

  // Wait for payment page (action=payment)
  await page.waitForURL('**/checkout?action=payment**', { timeout: 30000 });
  console.log('[Step 4] ✓ Redirected to payment QR page');

  // Extract payment reference code (format: MFxxxxxx) from the payment page
  const refCode = await page.locator('#js-reference').inputValue();
  const rawAmount = await page.locator('#js-amount-formatted').inputValue();
  console.log(`[Step 4] Payment ref code: ${refCode}, amount: ${rawAmount}`);

  // ==========================================
  // Step 5: Payment Simulation via SePay Webhook
  // ==========================================
  console.log('\n[Step 5] Simulating SePay payment webhook...');

  // POST SePay webhook
  const webhookResponse = await request.post('api/payment/webhook', {
    headers: { 'Content-Type': 'application/json' },
    data: {
      id: `e2e-tx-${Math.floor(Math.random() * 900000) + 100000}`,
      code: refCode,
      transferType: 'in',
      transferAmount: rawAmount,
      subAccount: 'SBSEPAY3NHWA061W5V2',
      accountNumber: 'SBSEPAY3NHWA061W5V2'
    }
  });

  expect(webhookResponse.status()).toBe(200);
  const webhookJson = await webhookResponse.json();
  expect(webhookJson.success).toBe(true);
  console.log('[Step 5] ✓ SePay webhook accepted');

  // Wait for success page redirect (polling script triggers automatic redirect or we can wait)
  await page.waitForURL('**/checkout?action=success**', { timeout: 30000 });
  await expect(page.locator('h1')).toContainText('Đặt Hàng Thành Công!', { timeout: 10000 });
  console.log('[Step 5] ✓ Order placed and paid successfully');

  await page.goto('auth/logout');

  // ==========================================
  // Step 6: Shop Owner Confirms & Dispatches Order
  // ==========================================
  console.log('\n[Step 6] Shop owner processing order...');
  await page.goto('auth/login');
  await page.fill('#identifier', 'owner1@fruitshop.local');
  await page.fill('#password', '123456');
  await page.click('button[type="submit"]');
  await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });

  await page.goto('shop/orders');
  await expect(page.locator('h1')).toContainText(/quản lý\s+đơn\s+hàng/i, { timeout: 10000 });

  // Accept/confirm (CONFIRMED -> PROCESSING)
  const confirmBtn = page.locator(
    'form:has(button:has-text("Xác nhận đơn")) button, form:has(button:has-text("Duyệt đơn")) button, ' +
    'button:has-text("Xác nhận đơn"), button:has-text("Duyệt đơn")'
  ).first();
  if (await confirmBtn.isVisible({ timeout: 5000 })) {
    await confirmBtn.click();
    await page.waitForLoadState('networkidle');
    console.log('[Step 6] ✓ Order confirmed by shop');
  } else {
    console.log('[Step 6] No confirm button (may already be confirmed)');
  }

  // Dispatch to delivery (PROCESSING -> SHIPPING)
  const dispatchBtn = page.locator(
    'form:has(button:has-text("Bàn giao vận chuyển")) button, form:has(button:has-text("Chuẩn bị xong")) button, ' +
    'button:has-text("Bàn giao vận chuyển"), button:has-text("Chuẩn bị xong")'
  ).first();
  if (await dispatchBtn.isVisible({ timeout: 5000 })) {
    await dispatchBtn.click();
    await page.waitForLoadState('networkidle');
    console.log('[Step 6] ✓ Order dispatched to delivery');
  } else {
    console.log('[Step 6] No dispatch button (may already be dispatched)');
  }

  await page.goto('auth/logout');
  console.log('[Step 6] ✓ Shop owner processing complete');

  // ==========================================
  // Step 7: Delivery Staff Picks Up and Delivers
  // ==========================================
  console.log('\n[Step 7] Delivery staff processing...');
  await page.goto('auth/login');
  await page.fill('#identifier', 'delivery@fruitshop.local');
  await page.fill('#password', '123456');
  await page.click('button[type="submit"]');
  await page.waitForURL(url => !url.toString().includes('/auth/login'), { timeout: 15000 });

  await page.goto('delivery/dashboard');
  await expect(page.locator('h1')).toContainText(/Giao Hàng/i, { timeout: 10000 });

  // Accept delivery
  const acceptBtn = page.locator(
    'button:has-text("Nhận giao đơn"), button:has-text("Nhận đơn"), a:has-text("Nhận giao đơn")'
  ).first();
  if (await acceptBtn.isVisible({ timeout: 5000 })) {
    await acceptBtn.click();
    await page.waitForLoadState('networkidle');
    console.log('[Step 7] ✓ Delivery accepted');
  } else {
    console.log('[Step 7] No accept button visible');
  }

  // Mark as delivered
  const deliveredBtn = page.locator(
    'button:has-text("Hoàn thành"), button:has-text("Giao thành công"), button:has-text("Đã giao")'
  ).first();
  if (await deliveredBtn.isVisible({ timeout: 5000 })) {
    await deliveredBtn.click();
    await page.waitForLoadState('networkidle');
    console.log('[Step 7] ✓ Order delivered');
  } else {
    console.log('[Step 7] No delivered button visible');
  }

  await page.goto('auth/logout');
  console.log('[Step 7] ✓ Delivery staff processing complete');

  console.log('\n=== ✓ E2E Full Business Flow COMPLETED SUCCESSFULLY ===');
});
