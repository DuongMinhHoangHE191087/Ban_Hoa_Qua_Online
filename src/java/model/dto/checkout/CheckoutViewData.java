package model.dto.checkout;
import model.dto.product.CartSummaryDTO;

import model.entity.auth.UserAddress;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO du lieu can hien thi tren trang checkout.
 */
// Touched for IDE re-indexing
public class CheckoutViewData {

    private CartSummaryDTO cartSummary;
    private List<UserAddress> userAddresses;
    private Integer shopOwnerId;
    private int shopCount;
    private BigDecimal directSaleAmount;
    private CheckoutQuoteDTO quote;
    private List<CheckoutShopSummaryDTO> shopSummaries = new ArrayList<>();

    public CartSummaryDTO getCartSummary() {
        return cartSummary;
    }

    public void setCartSummary(CartSummaryDTO cartSummary) {
        this.cartSummary = cartSummary;
    }

    public List<UserAddress> getUserAddresses() {
        return userAddresses;
    }

    public void setUserAddresses(List<UserAddress> userAddresses) {
        this.userAddresses = userAddresses;
    }

    public Integer getShopOwnerId() {
        return shopOwnerId;
    }

    public void setShopOwnerId(Integer shopOwnerId) {
        this.shopOwnerId = shopOwnerId;
    }

    public int getShopCount() {
        return shopCount;
    }

    public void setShopCount(int shopCount) {
        this.shopCount = shopCount;
    }

    public BigDecimal getDirectSaleAmount() {
        return directSaleAmount;
    }

    public void setDirectSaleAmount(BigDecimal directSaleAmount) {
        this.directSaleAmount = directSaleAmount;
    }

    public CheckoutQuoteDTO getQuote() {
        return quote;
    }

    public void setQuote(CheckoutQuoteDTO quote) {
        this.quote = quote;
    }

    public List<CheckoutShopSummaryDTO> getShopSummaries() {
        return shopSummaries;
    }

    public void setShopSummaries(List<CheckoutShopSummaryDTO> shopSummaries) {
        this.shopSummaries = shopSummaries;
    }
}
