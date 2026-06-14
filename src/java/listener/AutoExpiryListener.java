package listener;

import service.catalog.InventoryService;
import util.LoggerUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * AutoExpiryListener — Chạy daily background job tự động quét và trừ tồn kho các lô hàng hết hạn.
 *
 * Tần suất: Chạy sau 15 giây kể từ khi khởi động Tomcat, sau đó lặp lại mỗi 24 giờ.
 *
 * @author fruitmkt-team
 */
@WebListener
public class AutoExpiryListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(AutoExpiryListener.class.getName());

    private ScheduledExecutorService scheduler;
    private final InventoryService inventoryService = new InventoryService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LoggerUtil.info(log, "[AutoExpiryListener] Khởi tạo daily background job cho tự động trừ hàng hết hạn...");
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "AutoExpiry-Thread");
            t.setDaemon(true);
            return t;
        });

        // Chạy job hết hạn sau khi startup 15 giây, định kỳ mỗi 24 giờ
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                LoggerUtil.info(log, "[AutoExpiryListener] Bắt đầu tiến trình kiểm tra sản phẩm hết hạn tự động...");
                int processed = inventoryService.processExpiredBatches();
                if (processed > 0) {
                    LoggerUtil.info(log, "[AutoExpiryListener] Hoàn thành kiểm tra. Đã xử lý trừ kho " + processed + " lô hàng hết hạn.");
                } else {
                    LoggerUtil.info(log, "[AutoExpiryListener] Hoàn thành kiểm tra. Không có lô hàng nào hết hạn mới.");
                }
            } catch (Exception e) {
                LoggerUtil.error(log, "[AutoExpiryListener] Lỗi khi chạy job tự động trừ hàng hết hạn", e);
            }
        }, 15, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerUtil.info(log, "[AutoExpiryListener] Đang dừng daily background job cho tự động trừ hàng hết hạn...");
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LoggerUtil.warn(log, "[AutoExpiryListener] Hết thời gian chờ tắt scheduler.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
