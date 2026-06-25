package model.entity.order;

/**
 * OrderItem — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class OrderItem {

    private int orderItemId;
    private int orderId;
    private Integer variantId;
    private String productNameSnapshot;
    private String variantLabelSnapshot;
    private int quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal subtotal;
    private String packagingLabelSnapshot;
    private java.math.BigDecimal packagingPriceSnapshot;
    private String imagePath;

    public OrderItem() {}

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getVariantLabelSnapshot() { return variantLabelSnapshot; }
    public void setVariantLabelSnapshot(String variantLabelSnapshot) { this.variantLabelSnapshot = variantLabelSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public java.math.BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public java.math.BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(java.math.BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getPackagingLabelSnapshot() { return packagingLabelSnapshot; }
    public void setPackagingLabelSnapshot(String packagingLabelSnapshot) { this.packagingLabelSnapshot = packagingLabelSnapshot; }

    public java.math.BigDecimal getPackagingPriceSnapshot() { return packagingPriceSnapshot; }
    public void setPackagingPriceSnapshot(java.math.BigDecimal packagingPriceSnapshot) { this.packagingPriceSnapshot = packagingPriceSnapshot; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

}
