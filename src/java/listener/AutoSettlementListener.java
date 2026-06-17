package listener;

import service.shop.SettlementService;
import util.LoggerUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * AutoSettlementListener — Chạy daily background job tự động quyết toán các đơn hàng hoàn thành.
 *
 * Tần suất: Chạy sau 10 giây kể từ khi khởi động Tomcat, sau đó lặp lại mỗi 24 giờ.
 *
 * @author fruitmkt-team
 */
@WebListener
public class AutoSettlementListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(AutoSettlementListener.class.getName());

    private ScheduledExecutorService scheduler;
    private final SettlementService settlementService = new SettlementService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LoggerUtil.info(log, "[AutoSettlementListener] Khởi tạo daily background job cho quyết toán tự động...");
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "AutoSettlement-Thread");
            t.setDaemon(true);
            return t;
        });

        // Chạy job quyết toán sau khi startup 10 giây, định kỳ mỗi 24 giờ
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                LoggerUtil.info(log, "[AutoSettlementListener] Bắt đầu tiến trình quyết toán đơn hàng tự động...");
                int processed = settlementService.runAutoSettlement();
                LoggerUtil.info(log, "[AutoSettlementListener] Hoàn thành tiến trình quyết toán. Đã tạo " + processed + " kỳ đối soát mới.");
            } catch (Exception e) {
                LoggerUtil.error(log, "[AutoSettlementListener] Lỗi khi chạy job quyết toán tự động", e);
            }
        }, 10, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerUtil.info(log, "[AutoSettlementListener] Đang dừng daily background job cho quyết toán tự động...");
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LoggerUtil.warn(log, "[AutoSettlementListener] Hết thời gian chờ tắt scheduler.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
