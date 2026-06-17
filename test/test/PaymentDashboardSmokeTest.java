package test;

import service.shop.PaymentService;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Smoke test cho dashboard payment global.
 */
public class PaymentDashboardSmokeTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    public void loadAdminPaymentDashboardData() throws Exception {
        List<Map<String, Object>> payments = paymentService.getAdminPayments(null, null, null, 1, 5);
        int count = paymentService.countAdminPayments(null, null, null);

        assertNotNull(payments);
        assertTrue(count >= 0);
        assertTrue(payments.size() <= 5);
    }

    @Test
    public void filterAndPaginationShouldRespectPageSizeAndFilters() throws Exception {
        List<Map<String, Object>> allPayments = paymentService.getAdminPayments(null, null, null, 1, 10);
        int allCount = paymentService.countAdminPayments(null, null, null);

        assertNotNull(allPayments);
        assertTrue(allCount >= 0);
        assertTrue(allPayments.size() <= 10);

        if (!allPayments.isEmpty()) {
            Map<String, Object> sample = allPayments.get(0);
            String status = sample.get("paymentStatus") != null ? sample.get("paymentStatus").toString() : null;
            String method = sample.get("paymentMethod") != null ? sample.get("paymentMethod").toString() : null;

            List<Map<String, Object>> filtered = paymentService.getAdminPayments(status, method, null, 1, 1);
            int filteredCount = paymentService.countAdminPayments(status, method, null);

            assertNotNull(filtered);
            assertTrue(filtered.size() <= 1);
            assertTrue(filteredCount <= allCount);
            for (Map<String, Object> row : filtered) {
                if (status != null) {
                    assertEquals(status, row.get("paymentStatus").toString());
                }
                if (method != null) {
                    assertEquals(method, row.get("paymentMethod").toString());
                }
            }
        }
    }
}
