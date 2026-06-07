package com.fruitmkt.test;

import com.fruitmkt.dao.DeliveryDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.entity.Delivery;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.service.DeliveryService;
import com.fruitmkt.config.AppConfig;
import java.util.List;

/**
 * TestDelivery — Test case chuẩn kiểm thử nghiệp vụ role Shipper (Delivery Staff)
 */
public class TestDelivery {

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU ROLE SHIPPER (DELIVERY STAFF) ===");
        
        DeliveryDAO deliveryDAO = new DeliveryDAO();
        OrderDAO orderDAO = new OrderDAO();
        DeliveryService deliveryService = new DeliveryService();
        
        int testStaffId = 2; // ID của Delivery Nguyen trong seed data
        
        try {
            // 1. Kiểm thử lấy danh sách đơn hàng được phân công
            System.out.println("\n[TEST 1] Lay danh sach don hang cua Shipper ID: " + testStaffId);
            List<Delivery> deliveries = deliveryDAO.findByStaffId(testStaffId);
            System.out.println("-> So luong don hang da phan cong: " + deliveries.size());
            
            if (deliveries.isEmpty()) {
                System.out.println("-> [WARN] Khong co don hang nao duoc phan cong cho Shipper ID " + testStaffId + " de kiem thu tiep.");
                System.out.println("=== KET THUC KIEM THU VAI TRO SHIPPER ===");
                return;
            }
            
            // Chon mot don hang dang o trang thai ASSIGNED de kiem thu
            Delivery targetDelivery = null;
            for (Delivery d : deliveries) {
                List<Order> orders = orderDAO.findById(d.getOrderId());
                String orderStatus = orders.isEmpty() ? "UNKNOWN" : orders.get(0).getStatus();
                System.out.println(String.format("   * Don hang ID: %d | Trang thai giao: %s | Trang thai don: %s", 
                    d.getOrderId(), d.getStatus(), orderStatus));
                if (AppConfig.DELIVERY_ASSIGNED.equals(d.getStatus())) {
                    targetDelivery = d;
                }
            }
            
            if (targetDelivery == null) {
                targetDelivery = deliveries.get(0);
                System.out.println("\n-> [INFO] Chon don hang ID " + targetDelivery.getOrderId() + " de chay tiep luong cap nhat.");
            } else {
                System.out.println("\n-> [INFO] Tim thay don hang trang thai ASSIGNED: ID " + targetDelivery.getOrderId());
            }

            int deliveryId = targetDelivery.getDeliveryId();
            
            // 2. Kiem thu cap nhat trang thai sang PICKED_UP (Da lay hang)
            System.out.println("\n[TEST 2] Cap nhat sang trang thai: PICKED_UP");
            deliveryService.updateStatusAndProof(testStaffId, deliveryId, AppConfig.DELIVERY_PICKED_UP, null, null);
            Delivery step2 = deliveryDAO.findById(deliveryId);
            System.out.println("-> Trang thai sau cap nhat: " + step2.getStatus());
            assert AppConfig.DELIVERY_PICKED_UP.equals(step2.getStatus()) : "Loi: Trang thai khong khop!";

            // 3. Kiem thu cap nhat trang thai sang IN_TRANSIT (Dang giao)
            System.out.println("\n[TEST 3] Cap nhat sang trang thai: IN_TRANSIT");
            deliveryService.updateStatusAndProof(testStaffId, deliveryId, AppConfig.DELIVERY_IN_TRANSIT, null, null);
            Delivery step3 = deliveryDAO.findById(deliveryId);
            System.out.println("-> Trang thai sau cap nhat: " + step3.getStatus());
            assert AppConfig.DELIVERY_IN_TRANSIT.equals(step3.getStatus()) : "Loi: Trang thai khong khop!";

            // 4. Kiem thu cap nhat trang thai sang DELIVERED (Giao thanh cong)
            System.out.println("\n[TEST 4] Cap nhat sang trang thai: DELIVERED");
            deliveryService.markAsDelivered(testStaffId, deliveryId, "uploads/proof-test.png");
            Delivery step4 = deliveryDAO.findById(deliveryId);
            List<Order> step4Orders = orderDAO.findById(step4.getOrderId());
            String step4OrderStatus = step4Orders.isEmpty() ? "UNKNOWN" : step4Orders.get(0).getStatus();
            System.out.println("-> Trang thai giao sau cap nhat: " + step4.getStatus());
            System.out.println("-> Trang thai don hang tuong ung trong DB: " + step4OrderStatus);
            
            assert AppConfig.DELIVERY_DELIVERED.equals(step4.getStatus()) : "Loi: Trang thai giao khong phai DELIVERED!";
            assert AppConfig.ORDER_DELIVERED.equals(step4OrderStatus) : "Loi: Trang thai don hang chua tu dong sync sang DELIVERED!";

            System.out.println("\n-> [SUCCESS] 100% test cases cho vai tro Shipper da qua tai successfully!");
            
        } catch (Exception e) {
            System.out.println("\n-> [FAILED] Xay ra loi trong qua trinh kiem thu!");
            e.printStackTrace();
        }
        
        System.out.println("\n=== KET THUC KIEM THU ROLE SHIPPER (DELIVERY STAFF) ===");
    }
}
