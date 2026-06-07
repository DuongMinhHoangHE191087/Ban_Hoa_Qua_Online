package com.fruitmkt.model.entity;

import java.math.BigDecimal;

/**
 * ProductPackagingOption — Thực thể đại diện cho các lựa chọn đóng gói đi kèm sản phẩm.
 * Ánh xạ bảng product_packaging_options.
 *
 * @author fruitmkt-team
 */
public class ProductPackagingOption {
    private int packagingId;
    private int productId;
    private String label;
    private BigDecimal priceAdd;
    private boolean isActive;

    public ProductPackagingOption() {}

    public int getPackagingId() { return packagingId; }
    public void setPackagingId(int packagingId) { this.packagingId = packagingId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getPriceAdd() { return priceAdd; }
    public void setPriceAdd(BigDecimal priceAdd) { this.priceAdd = priceAdd; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
}
