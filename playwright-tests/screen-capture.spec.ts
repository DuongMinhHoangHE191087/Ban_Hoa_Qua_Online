import { test, expect, type Page } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';

type Screen = {
  id: string;
  actor: 'guest' | 'customer' | 'shop' | 'delivery' | 'admin';
  route: string;
  name: string;
  requiresLogin?: boolean;
};

const screens: Screen[] = [
  { id: 'G01', actor: 'guest', route: 'home', name: 'home' },
  { id: 'G02', actor: 'guest', route: 'products', name: 'product-list' },
  { id: 'G03', actor: 'guest', route: 'products/detail?id=1', name: 'product-detail' },
  { id: 'G04', actor: 'guest', route: 'shop-view?id=1', name: 'shop-view' },
  { id: 'G05', actor: 'guest', route: 'cart', name: 'cart' },
  { id: 'G06', actor: 'guest', route: 'about', name: 'about' },
  { id: 'G07', actor: 'guest', route: 'contact', name: 'contact' },
  { id: 'A01', actor: 'guest', route: 'auth/login', name: 'login' },
  { id: 'A02', actor: 'guest', route: 'auth/register', name: 'register' },
  { id: 'A03', actor: 'guest', route: 'auth/forgot', name: 'forgot-password' },
  { id: 'A17', actor: 'guest', route: 'auth/verify', name: 'verify-email' },
  { id: 'A18', actor: 'guest', route: 'auth/reset-password', name: 'reset-password' },
  { id: 'C01', actor: 'customer', route: 'customer/dashboard', name: 'customer-dashboard', requiresLogin: true },
  { id: 'C02', actor: 'customer', route: 'checkout', name: 'checkout', requiresLogin: true },
  { id: 'C03', actor: 'customer', route: 'orders', name: 'orders', requiresLogin: true },
  { id: 'C04', actor: 'customer', route: 'orders/detail?id=1', name: 'order-detail', requiresLogin: true },
  { id: 'C05', actor: 'customer', route: 'notifications', name: 'notifications', requiresLogin: true },
  { id: 'C06', actor: 'customer', route: 'chat', name: 'customer-chat', requiresLogin: true },
  { id: 'C07', actor: 'customer', route: 'profile', name: 'customer-profile', requiresLogin: true },
  { id: 'C08', actor: 'customer', route: 'returns', name: 'return-request', requiresLogin: true },
  { id: 'C09', actor: 'customer', route: 'customer/order-reviews?orderId=1', name: 'order-reviews', requiresLogin: true },
  { id: 'C10', actor: 'customer', route: 'customer/shop-apply', name: 'shop-apply', requiresLogin: true },
  { id: 'C11', actor: 'customer', route: 'auth/change-password', name: 'change-password', requiresLogin: true },
  { id: 'C12', actor: 'customer', route: 'profile/order-detail?id=1', name: 'profile-order-detail', requiresLogin: true },
  { id: 'C13', actor: 'customer', route: 'profile/order-detail?action=invoice&id=1', name: 'invoice', requiresLogin: true },
  { id: 'C14', actor: 'customer', route: 'checkout?action=payment', name: 'order-payment', requiresLogin: true },
  { id: 'C15', actor: 'customer', route: 'checkout?action=success', name: 'order-success', requiresLogin: true },
  { id: 'C16', actor: 'customer', route: 'reviews?action=submit&orderId=1', name: 'review-submit', requiresLogin: true },
  { id: 'S01', actor: 'shop', route: 'shop/status', name: 'shop-status', requiresLogin: true },
  { id: 'S02', actor: 'shop', route: 'shop/dashboard', name: 'shop-dashboard', requiresLogin: true },
  { id: 'S03', actor: 'shop', route: 'shop/profile', name: 'shop-profile', requiresLogin: true },
  { id: 'S04', actor: 'shop', route: 'shop/products', name: 'shop-products', requiresLogin: true },
  { id: 'S05', actor: 'shop', route: 'shop/product-create', name: 'product-create', requiresLogin: true },
  { id: 'S06', actor: 'shop', route: 'shop/inventory', name: 'inventory', requiresLogin: true },
  { id: 'S07', actor: 'shop', route: 'shop/orders', name: 'shop-orders', requiresLogin: true },
  { id: 'S08', actor: 'shop', route: 'shop/promotions', name: 'promotions', requiresLogin: true },
  { id: 'S09', actor: 'shop', route: 'shop/settlement', name: 'settlement', requiresLogin: true },
  { id: 'S10', actor: 'shop', route: 'shop/reports', name: 'shop-reports', requiresLogin: true },
  { id: 'S11', actor: 'shop', route: 'shop/chat', name: 'shop-chat', requiresLogin: true },
  { id: 'S12', actor: 'shop', route: 'shop/settings', name: 'shop-settings', requiresLogin: true },
  { id: 'S13', actor: 'shop', route: 'returns', name: 'shop-return-requests', requiresLogin: true },
  { id: 'S14', actor: 'shop', route: 'shop/product-edit?id=1', name: 'product-edit', requiresLogin: true },
  { id: 'D01', actor: 'delivery', route: 'delivery/dashboard', name: 'delivery-dashboard', requiresLogin: true },
  { id: 'D02', actor: 'delivery', route: 'delivery/list', name: 'delivery-list', requiresLogin: true },
  { id: 'D03', actor: 'delivery', route: 'delivery/detail?id=1', name: 'delivery-detail', requiresLogin: true },
  { id: 'A04', actor: 'admin', route: 'admin/dashboard', name: 'admin-dashboard', requiresLogin: true },
  { id: 'A05', actor: 'admin', route: 'admin/users', name: 'admin-users', requiresLogin: true },
  { id: 'A06', actor: 'admin', route: 'admin/shops', name: 'admin-shops', requiresLogin: true },
  { id: 'A07', actor: 'admin', route: 'admin/products', name: 'admin-products', requiresLogin: true },
  { id: 'A08', actor: 'admin', route: 'admin/categories', name: 'admin-categories', requiresLogin: true },
  { id: 'A09', actor: 'admin', route: 'admin/orders', name: 'admin-orders', requiresLogin: true },
  { id: 'A10', actor: 'admin', route: 'admin/payments', name: 'admin-payments', requiresLogin: true },
  { id: 'A11', actor: 'admin', route: 'admin/settlements', name: 'admin-settlements', requiresLogin: true },
  { id: 'A12', actor: 'admin', route: 'admin/refunds', name: 'admin-refunds', requiresLogin: true },
  { id: 'A13', actor: 'admin', route: 'admin/reviews', name: 'admin-reviews', requiresLogin: true },
  { id: 'A14', actor: 'admin', route: 'admin/reports', name: 'admin-reports', requiresLogin: true },
  { id: 'A15', actor: 'admin', route: 'admin/config', name: 'admin-config', requiresLogin: true },
  { id: 'A16', actor: 'admin', route: 'admin/chat', name: 'admin-chat', requiresLogin: true },
  { id: 'A19', actor: 'admin', route: 'admin/users/view?id=1', name: 'admin-user-view', requiresLogin: true },
  { id: 'A20', actor: 'admin', route: 'admin/shops/manage?id=1', name: 'admin-shop-manage', requiresLogin: true },
  { id: 'A21', actor: 'admin', route: 'admin/notifications', name: 'admin-notifications', requiresLogin: true },
];

const credentials = {
  customer: [process.env.PW_CUSTOMER_EMAIL, process.env.PW_CUSTOMER_PASSWORD],
  shop: [process.env.PW_SHOP_EMAIL, process.env.PW_SHOP_PASSWORD],
  delivery: [process.env.PW_DELIVERY_EMAIL, process.env.PW_DELIVERY_PASSWORD],
  admin: [process.env.PW_ADMIN_EMAIL, process.env.PW_ADMIN_PASSWORD],
} as const;

async function login(page: Page, actor: Screen['actor']) {
  if (actor === 'guest') return;
  const [email, password] = credentials[actor];
  test.skip(!email || !password, `Thiếu PW_${actor.toUpperCase()}_EMAIL/PASSWORD`);
  await page.goto('auth/login');
  await page.fill('#identifier', email!);
  await page.fill('#password', password!);
  await page.click('button[type="submit"]');
  await expect(page).not.toHaveURL(/\/auth\/login/i, { timeout: 20_000 });
}

async function collectUiEvidence(page: Page, screenId: string) {
  return page.locator('input, select, textarea, button, a[role="button"]')
    .evaluateAll((elements, id) => {
      const keyFor = (element: Element) => {
        const input = element as HTMLInputElement;
        const onclick = element.getAttribute('onclick') || '';
        const handler = onclick.match(/(?:window\[['"]?([^'"\]]+)|([A-Za-z_$][\w$]*))\s*\(/);
        const form = element.closest('form');
        const label = element.closest('label')?.textContent?.trim()
          || element.getAttribute('aria-label') || element.getAttribute('title')
          || element.getAttribute('name') || element.textContent?.trim() || '';
        return [
          element.tagName.toLowerCase(), input.type || '', element.getAttribute('data-action') || '',
          handler?.[1] || handler?.[2] || '', form?.getAttribute('action') || '',
          form?.getAttribute('method') || '', element.getAttribute('name') || '',
          label.replace(/\s+/g, ' ').slice(0, 80).toLowerCase(),
        ].join('|');
      };
      const counts = new Map<string, number>();
      elements.forEach(element => counts.set(keyFor(element), (counts.get(keyFor(element)) || 0) + 1));
      const seen = new Set<string>();
      return elements.flatMap((element, index) => {
      const node = element as HTMLElement;
      const input = element as HTMLInputElement;
      const functionKey = keyFor(element);
      const duplicateCount = counts.get(functionKey) || 1;
      if (seen.has(functionKey)) return [];
      seen.add(functionKey);
      const label = element.closest('label')?.textContent?.trim()
        || element.getAttribute('aria-label')
        || element.getAttribute('name')
        || element.textContent?.trim()
        || '';
      return {
        evidenceId: `${id}-FUNC-${String(seen.size).padStart(2, '0')}-UI`,
        functionKey,
        duplicateCount,
        tag: element.tagName.toLowerCase(),
        type: input.type || undefined,
        name: element.getAttribute('name'),
        id: element.id || undefined,
        label: label.replace(/\s+/g, ' ').slice(0, 120),
        role: element.getAttribute('role'),
        visible: Boolean(node.offsetWidth || node.offsetHeight || node.getClientRects().length),
        disabled: input.disabled,
        screenScoped: !element.closest('nav, header, footer, #ai-chat-widget, .ai-chat'),
      };
      });
    }, screenId);
}

async function markUiEvidence(page: Page) {
  await page.evaluate(() => {
    document.querySelectorAll('[data-screen-evidence-marker]').forEach(marker => marker.remove());
    const elements = [...document.querySelectorAll('input, select, textarea, button, a[role="button"]')]
      .filter(element => {
        const node = element as HTMLElement;
        return Boolean(node.offsetWidth || node.offsetHeight || node.getClientRects().length);
      });
    const seen = new Set<string>();
    const keyFor = (element: Element) => {
      const input = element as HTMLInputElement;
      const onclick = element.getAttribute('onclick') || '';
      const handler = onclick.match(/(?:window\[['"]?([^'"\]]+)|([A-Za-z_$][\w$]*))\s*\(/);
      const form = element.closest('form');
      const label = element.closest('label')?.textContent?.trim() || element.getAttribute('aria-label') || element.getAttribute('title') || element.getAttribute('name') || element.textContent?.trim() || '';
      return [element.tagName.toLowerCase(), input.type || '', element.getAttribute('data-action') || '', handler?.[1] || handler?.[2] || '', form?.getAttribute('action') || '', form?.getAttribute('method') || '', element.getAttribute('name') || '', label.replace(/\s+/g, ' ').slice(0, 80).toLowerCase()].join('|');
    };
    elements.forEach((element, index) => {
      const functionKey = keyFor(element);
      if (seen.has(functionKey)) return;
      seen.add(functionKey);
      const node = element as HTMLElement;
      const rect = node.getBoundingClientRect();
      node.setAttribute('data-screen-evidence-id', String(seen.size));
      node.style.outline = '2px solid #dc2626';
      node.style.outlineOffset = '1px';
      const marker = document.createElement('span');
      marker.dataset.screenEvidenceMarker = 'true';
      marker.textContent = String(seen.size);
      Object.assign(marker.style, {
        position: 'fixed', left: `${Math.max(0, rect.left)}px`, top: `${Math.max(0, rect.top)}px`,
        zIndex: '2147483647', background: '#dc2626', color: '#fff', font: 'bold 11px Arial',
        lineHeight: '16px', minWidth: '16px', height: '16px', textAlign: 'center',
        borderRadius: '3px', pointerEvents: 'none',
      });
      document.body.appendChild(marker);
    });
  });
}

test.describe('Screen inventory screenshots', () => {
  test.describe.configure({ mode: 'serial' });

  for (const screen of screens) {
    test(`${screen.id} ${screen.name}`, async ({ page }) => {
      test.setTimeout(60_000);
      const responseStatuses: Array<{ url: string; status: number }> = [];
      const consoleErrors: string[] = [];
      page.on('response', response => {
        const url = new URL(response.url());
        responseStatuses.push({ url: `${url.pathname}${url.search ? '?…' : ''}`, status: response.status() });
      });
      page.on('console', message => {
        if (message.type() === 'error') consoleErrors.push(message.text());
      });
      await login(page, screen.actor);
      const response = await page.goto(screen.route, { waitUntil: 'domcontentloaded' });
      expect(response, `${screen.route} không trả response`).not.toBeNull();
      expect(response!.status(), `${screen.route} trả HTTP ${response!.status()}`).toBeLessThan(400);
      // Background polling/fonts/third-party assets may never become network-idle;
      // DOMContentLoaded plus a bounded settle window is sufficient for evidence.
      await page.waitForLoadState('networkidle', { timeout: 10_000 }).catch(() => undefined);
      // Some JSP layouts temporarily hide body while client-side styles initialize;
      // the document root is the stable render gate for screenshot evidence.
      await expect(page.locator('html')).toBeVisible();
      const uiEvidence = await collectUiEvidence(page, screen.id);
      const output = path.join(
        'artifacts',
        'screenshots',
        test.info().project.name,
        screen.actor,
        `${screen.id}-${screen.name}.png`
      );
      const annotatedOutput = output.replace(/\.png$/, '-annotated.png');
      fs.rmSync(output, { force: true });
      fs.rmSync(annotatedOutput, { force: true });
      try {
        await page.screenshot({ path: output, fullPage: true });
        await markUiEvidence(page);
        await page.screenshot({ path: annotatedOutput, fullPage: false });
      } catch (error) {
        if (!String(error).includes('larger than 32767')) throw error;
        const viewportOutput = output.replace(/\.png$/, '-viewport.png');
        await page.screenshot({ path: viewportOutput, fullPage: false });
        await markUiEvidence(page);
        await page.screenshot({ path: annotatedOutput, fullPage: false });
        console.warn(`[screen-capture] ${screen.id} dùng viewport fallback: fullPage vượt giới hạn Chromium`);
      }
      const evidenceDir = path.join('artifacts', 'screen-evidence', test.info().project.name, screen.actor);
      fs.mkdirSync(evidenceDir, { recursive: true });
      fs.writeFileSync(
        path.join(evidenceDir, `${screen.id}-${screen.name}.json`),
        JSON.stringify({
          id: screen.id,
          actor: screen.actor,
          route: screen.route,
          finalUrl: new URL(page.url()).pathname,
          httpStatus: response!.status(),
          responseStatuses,
          consoleErrors,
          uiEvidence,
          annotatedScreenshot: annotatedOutput,
          capturedAt: new Date().toISOString(),
        }, null, 2),
        'utf8'
      );
    });
  }
});
