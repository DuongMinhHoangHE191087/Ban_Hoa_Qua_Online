<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="${product.name} - MetaFruit"/></jsp:include>

<!-- Get current month -->
<c:set var="currentMonth" value="<%= java.time.LocalDate.now().getMonthValue() %>" />
<c:set var="inSeason" value="true" />
<c:if test="${not empty product.seasonStart && not empty product.seasonEnd}">
    <c:choose>
        <c:when test="${product.seasonStart <= product.seasonEnd}">
            <c:set var="inSeason" value="${currentMonth >= product.seasonStart && currentMonth <= product.seasonEnd}" />
        </c:when>
        <c:otherwise>
            <c:set var="inSeason" value="${currentMonth >= product.seasonStart || currentMonth <= product.seasonEnd}" />
        </c:otherwise>
    </c:choose>
</c:if>

<div class="shop-container" style="max-width: 1000px; margin: 40px auto; padding: 0 15px; font-family: 'Plus Jakarta Sans', sans-serif;">
    
    <!-- PRODUCT DETAILED CARD -->
    <div style="background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03); border: 1px solid rgba(20, 83, 45, 0.08); display: grid; grid-template-columns: 1fr 1fr; gap: 40px; margin-bottom: 40px; position: relative;">
        
        <!-- Organic/Imported Label (II.9) -->
        <c:if test="${not empty product.labelType}">
            <span style="position: absolute; top: 20px; left: 20px; z-index: 10; padding: 6px 14px; border-radius: 20px; color: white; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;
                         background-color: ${'Organic'.equals(product.labelType) ? '#16a34a' : '#2563eb'};">
                <i class="fa-solid ${'Organic'.equals(product.labelType) ? 'fa-leaf' : 'fa-plane-arrival'}"></i>
                <c:out value="${product.labelType}"/>
            </span>
        </c:if>

        <!-- Product Image (with fallback) -->
        <div style="display: flex; align-items: center; justify-content: center; background-color: #f9fafb; border-radius: 16px; border: 1px solid #f3f4f6; height: 350px;">
            <img src="${pageContext.request.contextPath}/assets/img/logo-leaf.png" alt="Fruit" style="max-height: 200px; object-fit: contain;" />
        </div>

        <!-- Product Purchase Information -->
        <div style="display: flex; flex-direction: column;">
            
            <h1 style="color: #1f2937; font-size: 28px; font-weight: 800; margin: 0 0 10px 0;"><c:out value="${product.name}"/></h1>
            
            <div style="display: flex; align-items: center; gap: 15px; margin-bottom: 20px;">
                <ft:stars rating="${product.rating}" />
                <span style="color: #6b7280; font-size: 13px;">| <c:out value="${product.viewCount}"/> lượt xem</span>
                <span style="color: #6b7280; font-size: 13px;">| <c:out value="${product.soldQuantity}"/> đã bán</span>
            </div>

            <!-- SEASON AVAILABILITY BLOCK (II.10) -->
            <c:choose>
                <c:when test="${not inSeason}">
                    <div style="background-color: #fef2f2; border: 1px solid #fee2e2; border-radius: 12px; padding: 15px; margin-bottom: 20px; display: flex; align-items: flex-start; gap: 10px; color: #991b1b; font-size: 14px; font-weight: 500;">
                        <i class="fa-solid fa-triangle-exclamation" style="margin-top: 3px; font-size: 16px;"></i>
                        <div>
                            <strong>Hết mùa thu hoạch:</strong> Sản phẩm này hiện đã hết mùa thu hoạch (Mùa vụ: Tháng ${product.seasonStart} - Tháng ${product.seasonEnd}). Hệ thống đã tạm đóng chức năng đặt mua!
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:if test="${not empty product.seasonStart && not empty product.seasonEnd}">
                        <div style="background-color: #f0fdf4; border: 1px solid #dcfce7; border-radius: 12px; padding: 10px 15px; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; color: #166534; font-size: 13px; font-weight: 600;">
                            <i class="fa-solid fa-calendar-check"></i>
                            Đang trong mùa thu hoạch rộ (Mùa vụ: Tháng ${product.seasonStart} - Tháng ${product.seasonEnd})
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>

            <div style="border-bottom: 1.5px solid #f3f4f6; margin-bottom: 20px; padding-bottom: 20px;">
                <!-- DYNAMIC DISPLAY OF PRICE AND STRIKETHROUGH DISCOUNT (II.14, II.15) -->
                <div style="display: flex; align-items: baseline; gap: 12px;">
                    <span id="displayedPrice" style="color: #dc2626; font-size: 26px; font-weight: 800;"></span>
                    <span id="strikethroughPrice" style="color: #9ca3af; font-size: 16px; text-decoration: line-through; display: none;"></span>
                </div>
                <div id="stockBadge" style="margin-top: 8px; font-size: 13px; font-weight: 600; color: #4b5563;"></div>
            </div>

            <!-- DYNAMIC WEIGHT VARIANTS SWITCHER CHIPS (II.7) -->
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: 700; color: #374151; margin-bottom: 8px; font-size: 14px;">Chọn khối lượng (II.7)</label>
                <div id="variantChipsContainer" style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <c:forEach var="v" items="${variants}" varStatus="status">
                        <button type="button" class="variant-chip" data-id="${v.variantId}" data-price="${v.price.intValue()}" 
                                data-discount="${v.discountPrice != null ? v.discountPrice.intValue() : ''}" data-stock="${v.stockQuantity}"
                                style="padding: 8px 16px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 14px; font-weight: 600; background: white; cursor: pointer; color: #4b5563; transition: all 0.2s;">
                            <c:out value="${v.variantLabel}"/>
                        </button>
                    </c:forEach>
                </div>
            </div>

            <!-- PACKAGING OPTIONS SELECTOR (II.8) -->
            <div style="margin-bottom: 25px;">
                <label style="display: block; font-weight: 700; color: #374151; margin-bottom: 8px; font-size: 14px;">Tùy chọn đóng gói (II.8)</label>
                <div style="display: flex; gap: 10px;">
                    <label style="flex: 1; border: 1.5px solid #14532d; border-radius: 10px; padding: 10px; display: flex; flex-direction: column; cursor: pointer; font-size: 12px; color: #4b5563; background: #f0fdf4;">
                        <span style="font-weight: 700; font-size: 13px; color: #14532d;"><input type="radio" name="packaging" value="None" checked style="accent-color: #14532d; margin-right: 5px;" />Không hộp</span>
                        <span>Đóng túi lưới/túi giấy bảo vệ</span>
                    </label>
                    <label style="flex: 1; border: 1.5px solid #d1d5db; border-radius: 10px; padding: 10px; display: flex; flex-direction: column; cursor: pointer; font-size: 12px; color: #4b5563; background: white;">
                        <span style="font-weight: 700; font-size: 13px; color: #374151;"><input type="radio" name="packaging" value="Gift Box" style="accent-color: #14532d; margin-right: 5px;" />Hộp Quà tặng</span>
                        <span style="color: #166534; font-weight: 600;">+50,000 VNĐ</span>
                    </label>
                    <label style="flex: 1; border: 1.5px solid #d1d5db; border-radius: 10px; padding: 10px; display: flex; flex-direction: column; cursor: pointer; font-size: 12px; color: #4b5563; background: white;">
                        <span style="font-weight: 700; font-size: 13px; color: #374151;"><input type="radio" name="packaging" value="Foam Tray" style="accent-color: #14532d; margin-right: 5px;" />Khay xốp</span>
                        <span style="color: #166534; font-weight: 600;">+15,000 VNĐ</span>
                    </label>
                </div>
            </div>

            <!-- QUANTITY AND ADD TO CART PANEL -->
            <form action="${pageContext.request.contextPath}/cart/add" method="POST" id="addToCartForm">
                <input type="hidden" name="variantId" id="formVariantId" value="" />
                <input type="hidden" name="packagingOption" id="formPackaging" value="None" />
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}" />

                <div style="display: flex; gap: 15px; align-items: center; margin-top: 15px;">
                    <div style="display: flex; border: 1.5px solid #d1d5db; border-radius: 10px; overflow: hidden; height: 46px; align-items: center; width: 120px;">
                        <button type="button" onclick="adjustQty(-1)" style="border: none; background: #f3f4f6; width: 35px; height: 100%; font-size: 18px; font-weight: bold; cursor: pointer; color: #4b5563;">-</button>
                        <input type="number" name="quantity" id="quantityInput" value="1" min="1" readonly style="width: 50px; border: none; text-align: center; font-size: 15px; font-weight: 700; color: #1f2937; pointer-events: none;" />
                        <button type="button" onclick="adjustQty(1)" style="border: none; background: #f3f4f6; width: 35px; height: 100%; font-size: 18px; font-weight: bold; cursor: pointer; color: #4b5563;">+</button>
                    </div>

                    <c:choose>
                        <c:when test="${not inSeason}">
                            <button type="button" disabled style="background-color: #9ca3af; color: white; border: none; padding: 12px 30px; border-radius: 10px; font-weight: 700; font-size: 15px; flex-grow: 1; height: 46px; cursor: not-allowed; display: flex; align-items: center; justify-content: center; gap: 8px;">
                                <i class="fa-solid fa-ban"></i> Đang Hết Mùa Vụ
                            </button>
                        </c:when>
                        <c:otherwise>
                            <button type="submit" id="submitBtn" style="background-color: #14532d; color: white; border: none; padding: 12px 30px; border-radius: 10px; font-weight: 700; font-size: 15px; flex-grow: 1; height: 46px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 15px rgba(20,83,45,0.15); transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#166534'" onmouseout="this.style.backgroundColor='#14532d'">
                                <i class="fa-solid fa-cart-plus"></i> Thêm vào giỏ hàng
                            </button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>

        </div>

    </div>

    <!-- PRODUCT EXTENDED METADATA AND SPECS -->
    <div style="background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03); border: 1px solid rgba(20, 83, 45, 0.08); margin-bottom: 40px;">
        <h3 style="color: #14532d; font-size: 18px; font-weight: 700; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 10px; margin-top: 0; margin-bottom: 15px;"><i class="fa-solid fa-file-lines"></i> Chi tiết sản phẩm</h3>
        <table style="width: 100%; border-collapse: collapse; font-size: 14px;">
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 10px 0; font-weight: 600; color: #4b5563; width: 180px;">Xuất xứ</td>
                <td style="padding: 10px 0; color: #1f2937;"><c:out value="${product.originRegion}"/>, <c:out value="${product.originCountry}"/></td>
            </tr>
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 10px 0; font-weight: 600; color: #4b5563;">Hạn sử dụng</td>
                <td style="padding: 10px 0; color: #1f2937;"><c:out value="${product.shelfLifeDays}"/> ngày kể từ ngày thu hoạch</td>
            </tr>
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 10px 0; font-weight: 600; color: #4b5563;">Ngày thu hoạch</td>
                <td style="padding: 10px 0; color: #1f2937;">${product.harvestDate}</td>
            </tr>
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 10px 0; font-weight: 600; color: #4b5563;">Mô tả sản phẩm</td>
                <td style="padding: 10px 0; color: #1f2937; line-height: 1.6;"><c:out value="${product.description}"/></td>
            </tr>
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 10px 0; font-weight: 600; color: #4b5563;">Hướng dẫn bảo quản</td>
                <td style="padding: 10px 0; color: #1f2937;"><c:out value="${product.storageInstruction}"/></td>
            </tr>
        </table>
    </div>

    <!-- II.21 Smart Personalized Recommendation Block -->
    <c:if test="${not empty recommendations}">
        <div style="background: white; border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03); border: 1px solid rgba(20, 83, 45, 0.08);">
            <h3 style="color: #14532d; font-size: 18px; font-weight: 700; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 10px; margin-top: 0; margin-bottom: 20px;"><i class="fa-solid fa-thumbs-up"></i> Có Thể Bạn Cũng Thích (II.21)</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 20px;">
                <c:forEach var="rec" items="${recommendations}">
                    
                    <a href="${pageContext.request.contextPath}/products/detail?id=${rec.productId}" style="text-decoration: none; color: inherit; background: white; border-radius: 12px; border: 1px solid #f3f4f6; overflow: hidden; display: flex; flex-direction: column; transition: transform 0.2s;" onmouseover="this.style.transform='translateY(-3px)'" onmouseout="this.style.transform='translateY(0)'">
                        <div style="height: 140px; background-color: #f9fafb; display: flex; align-items: center; justify-content: center;">
                            <img src="${pageContext.request.contextPath}/assets/img/logo-leaf.png" alt="fruit" style="max-height: 70px;" />
                        </div>
                        <div style="padding: 12px; display: flex; flex-direction: column; flex-grow: 1;">
                            <h4 style="margin: 0 0 6px 0; font-size: 13px; font-weight: 700; color: #1f2937; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: 38px;"><c:out value="${rec.name}"/></h4>
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto;">
                                <ft:stars rating="${rec.rating}" />
                                <span style="background-color: #f0fdf4; color: #166534; font-size: 10px; font-weight: 600; padding: 2px 6px; border-radius: 20px;">Xem</span>
                            </div>
                        </div>
                    </a>
                    
                </c:forEach>
            </div>
        </div>
    </c:if>

</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    
    // Cookie Logger to append current ID to recently_viewed_ids (II.23)
    const currentProductId = "${product.productId}";
    if (currentProductId) {
        logRecentlyViewed(currentProductId);
    }

    const displayedPrice = document.getElementById("displayedPrice");
    const strikethroughPrice = document.getElementById("strikethroughPrice");
    const stockBadge = document.getElementById("stockBadge");
    const formVariantId = document.getElementById("formVariantId");
    const formPackaging = document.getElementById("formPackaging");
    const submitBtn = document.getElementById("submitBtn");

    let activeVariantPrice = 0;
    let activeVariantDiscount = null;
    let activeVariantStock = 0;

    // Handle variant chips switcher selection
    const chips = document.querySelectorAll(".variant-chip");
    chips.forEach(chip => {
        chip.addEventListener("click", function() {
            chips.forEach(c => {
                c.style.backgroundColor = "white";
                c.style.color = "#4b5563";
                c.style.borderColor = "#d1d5db";
            });
            this.style.backgroundColor = "#14532d";
            this.style.color = "white";
            this.style.borderColor = "#14532d";

            activeVariantPrice = parseFloat(this.getAttribute("data-price"));
            const disc = this.getAttribute("data-discount");
            activeVariantDiscount = disc ? parseFloat(disc) : null;
            activeVariantStock = parseInt(this.getAttribute("data-stock"));
            
            formVariantId.value = this.getAttribute("data-id");

            updatePricingUI();
        });
    });

    // Auto-select first chip
    if (chips.length > 0) {
        chips[0].click();
    }

    // Packaging option radio listener (II.8)
    const radios = document.querySelectorAll("input[name='packaging']");
    radios.forEach(radio => {
        radio.addEventListener("change", function() {
            // Update visual active state
            radios.forEach(r => {
                const label = r.closest("label");
                label.style.backgroundColor = "white";
                label.style.borderColor = "#d1d5db";
                label.querySelector("span").style.color = "#374151";
            });
            const selectedLabel = this.closest("label");
            selectedLabel.style.backgroundColor = "#f0fdf4";
            selectedLabel.style.borderColor = "#14532d";
            selectedLabel.querySelector("span").style.color = "#14532d";

            formPackaging.value = this.value;
            updatePricingUI();
        });
    });

    function updatePricingUI() {
        let packageCost = 0;
        const selectedPackaging = document.querySelector("input[name='packaging']:checked").value;
        if (selectedPackaging === "Gift Box") {
            packageCost = 50000;
        } else if (selectedPackaging === "Foam Tray") {
            packageCost = 15000;
        }

        // Add packaging cost dynamically (II.8)
        const finalPriceVal = activeVariantPrice + packageCost;
        let finalDiscountVal = activeVariantDiscount ? (activeVariantDiscount + packageCost) : null;

        if (finalDiscountVal !== null) {
            displayedPrice.textContent = formatCurrency(finalDiscountVal);
            strikethroughPrice.textContent = formatCurrency(finalPriceVal);
            strikethroughPrice.style.display = "inline";
        } else {
            displayedPrice.textContent = formatCurrency(finalPriceVal);
            strikethroughPrice.style.display = "none";
        }

        // Update Stock status (II.11)
        if (activeVariantStock <= 0) {
            stockBadge.innerHTML = `<span style="color: #ef4444;"><i class="fa-solid fa-circle-xmark"></i> Hết hàng tồn kho</span>`;
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.style.backgroundColor = "#9ca3af";
                submitBtn.style.cursor = "not-allowed";
                submitBtn.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> Hết hàng tồn kho`;
            }
        } else {
            stockBadge.innerHTML = `<span style="color: #16a34a;"><i class="fa-solid fa-circle-check"></i> Còn lại: ${activeVariantStock} quả trong kho</span>`;
            if (submitBtn && "${inSeason}" === "true") {
                submitBtn.disabled = false;
                submitBtn.style.backgroundColor = "#14532d";
                submitBtn.style.cursor = "pointer";
                submitBtn.innerHTML = `<i class="fa-solid fa-cart-plus"></i> Thêm vào giỏ hàng`;
            }
        }
    }

    function formatCurrency(val) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val).replace("₫", "VNĐ");
    }

    // Append viewed product ID to recently_viewed_ids cookie (II.23)
    function logRecentlyViewed(id) {
        let cookieVal = getCookie("recently_viewed_ids") || "";
        let arr = cookieVal ? cookieVal.split(",") : [];
        
        // Remove existing to place it at the end (newest)
        arr = arr.filter(x => x !== id);
        arr.push(id);

        // Keep maximum of 10 history items
        if (arr.length > 10) {
            arr.shift();
        }

        // Save back with 30 days max-age
        document.cookie = "recently_viewed_ids=" + encodeURIComponent(arr.join(",")) + "; max-age=" + (30 * 24 * 60 * 60) + "; path=/;";
    }

    function getCookie(name) {
        let matches = document.cookie.match(new RegExp(
            "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
        ));
        return matches ? decodeURIComponent(matches[1]) : undefined;
    }
});

function adjustQty(amount) {
    const qtyInput = document.getElementById("quantityInput");
    let val = parseInt(qtyInput.value) + amount;
    if (val < 1) val = 1;
    qtyInput.value = val;
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
