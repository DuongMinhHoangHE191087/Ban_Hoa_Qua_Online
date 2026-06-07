package com.fruitmkt.listener;

import com.fruitmkt.service.SettlementService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AutoSettlementListener — Chạy daily background job tự động quyết toán các đơn hàng hoàn thành.
 *
 * Tần suất: Chạy sau 10 giây kể từ khi khởi động Tomcat, sau đó lặp lại mỗi 24 giờ.
 *
 * @author fruitmkt-team
 */
@WebListener
public class AutoSettlementListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private final SettlementService settlementService = new SettlementService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AutoSettlementListener] Khởi tạo daily background job cho quyết toán tự động...");
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "AutoSettlement-Thread");
            t.setDaemon(true);
            return t;
        });

        // Chạy job quyết toán sau khi startup 10 giây, định kỳ mỗi 24 giờ
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                System.out.println("[AutoSettlementListener] Bắt đầu tiến trình quyết toán đơn hàng tự động...");
                int processed = settlementService.runAutoSettlement();
                System.out.println("[AutoSettlementListener] Hoàn thành tiến trình quyết toán. Đã tạo " + processed + " kỳ đối soát mới.");
            } catch (Exception e) {
                System.err.println("[AutoSettlementListener] Lỗi khi chạy job quyết toán tự động: " + e.getMessage());
                e.printStackTrace();
            }
        }, 10, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[AutoSettlementListener] Đang dừng daily background job cho quyết toán tự động...");
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("[AutoSettlementListener] Hết thời gian chờ tắt scheduler.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
