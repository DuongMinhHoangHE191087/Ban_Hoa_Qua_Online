package model.dto.order;

import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Du lieu view cho trang danh sach don hang cua customer.
 */
public class OrderListViewDTO {

    private List<Order> orders = new ArrayList<>();
    private Map<Integer, Delivery> deliveryMap = new HashMap<>();
    private Map<Integer, PaymentTransaction> paymentTransactionMap = new HashMap<>();
    private int currentPage;
    private int totalPages;
    private String selectedStatus;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Map<Integer, Delivery> getDeliveryMap() {
        return deliveryMap;
    }

    public void setDeliveryMap(Map<Integer, Delivery> deliveryMap) {
        this.deliveryMap = deliveryMap;
    }

    public Map<Integer, PaymentTransaction> getPaymentTransactionMap() {
        return paymentTransactionMap;
    }

    public void setPaymentTransactionMap(Map<Integer, PaymentTransaction> paymentTransactionMap) {
        this.paymentTransactionMap = paymentTransactionMap;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(String selectedStatus) {
        this.selectedStatus = selectedStatus;
    }
}
