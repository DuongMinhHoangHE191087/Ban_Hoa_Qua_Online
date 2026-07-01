package listener;

import service.shop.PaymentService;
import util.LoggerUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * PaymentReconcileListener — Background job quét các payment completed nhưng tree đơn bị lệch
 * để tự động đồng bộ lại parent/child order status.
 */
@WebListener
public class PaymentReconcileListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(PaymentReconcileListener.class.getName());
    private static final int INITIAL_DELAY_MINUTES = 3;
    private static final int SCHEDULE_INTERVAL_MINUTES = 5;

    private final PaymentService paymentService = new PaymentService();
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LoggerUtil.info(log, "[PaymentReconcileListener] Khởi tạo job reconcile payment tự động...");
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "PaymentReconcile-Thread");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int repaired = paymentService.reconcileCompletedPayments();
                if (repaired > 0) {
                    LoggerUtil.info(log, "[PaymentReconcileListener] Đã reconcile thành công " + repaired + " payment tree.");
                } else {
                    LoggerUtil.info(log, "[PaymentReconcileListener] Không có payment tree nào cần reconcile.");
                }
            } catch (Exception e) {
                LoggerUtil.error(log, "[PaymentReconcileListener] Lỗi khi chạy job reconcile payment", e);
            }
        }, INITIAL_DELAY_MINUTES, SCHEDULE_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerUtil.info(log, "[PaymentReconcileListener] Đang dừng job reconcile payment...");
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LoggerUtil.warn(log, "[PaymentReconcileListener] Hết thời gian chờ tắt scheduler.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
