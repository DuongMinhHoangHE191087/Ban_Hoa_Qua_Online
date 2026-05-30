<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quản Lý Kho Hàng - MetaFruit"/></jsp:include>

<div class="shop-container" style="max-width: 1100px; margin: 40px auto; padding: 0 15px; font-family: 'Plus Jakarta Sans', sans-serif;">
    
    <!-- HEADER -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 15px;">
        <h1 style="color: #14532d; font-size: 28px; font-weight: 700; margin: 0;">
            <i class="fa-solid fa-warehouse" style="margin-right: 10px;"></i>
            Quản lý kho hàng & Tồn kho
        </h1>
        <a href="${pageContext.request.contextPath}/shop/dashboard" style="text-decoration: none; color: #6b7280; font-weight: 600; display: flex; align-items: center; gap: 5px;" onmouseover="this.style.color='#14532d'" onmouseout="this.style.color='#6b7280'">
            <i class="fa-solid fa-gauge-high"></i> Dashboard
        </a>
    </div>

    <!-- FLASH MESSAGE -->
    <c:if test="${not empty sessionScope.flashMsg}">
        <div style="padding: 15px; border-radius: 10px; margin-bottom: 25px; font-weight: 500; font-size: 14px; 
                    background-color: ${sessionScope.flashType == 'success' ? '#dcfce7' : '#fee2e2'}; 
                    color: ${sessionScope.flashType == 'success' ? '#14532d' : '#991b1b'}; 
                    border: 1px solid ${sessionScope.flashType == 'success' ? '#bbf7d0' : '#fca5a5'};">
            <i class="fa-solid ${sessionScope.flashType == 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}" style="margin-right: 8px;"></i>
            <c:out value="${sessionScope.flashMsg}"/>
        </div>
        <% session.removeAttribute("flashMsg"); session.removeAttribute("flashType"); %>
    </c:if>

    <!-- MAIN TWO-COLUMN LAYOUT -->
    <div style="display: grid; grid-template-columns: 350px 1fr; gap: 30px; margin-bottom: 40px;">
        
        <!-- RESTOCK ADJUSTMENT PANEL (II.13) -->
        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08); height: fit-content;">
            <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin-top: 0; margin-bottom: 15px; border-bottom: 1.5px solid rgba(20, 83, 45, 0.08); padding-bottom: 8px;">
                <i class="fa-solid fa-truck-ramp-box"></i> Nhập thêm quả (II.13)
            </h3>
            
            <form id="restockForm" action="${pageContext.request.contextPath}/shop/inventory" method="POST">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}" />

                <div style="margin-bottom: 15px;">
                    <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px; font-size: 13px;">Chọn Biến thể Quả</label>
                    <select name="variantId" required style="width: 100%; padding: 10px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 14px; background: white;">
                        <option value="">-- Chọn biến thể quả --</option>
                        <c:forEach var="v" items="${variants}">
                            <option value="${v.variantId}">
                                <c:out value="${v.sku}"/> (<c:out value="${v.variantLabel}"/>) - Tồn: <c:out value="${v.stockQuantity}"/> quả
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div style="margin-bottom: 20px;">
                    <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px; font-size: 13px;">Số lượng nhập kho</label>
                    <input type="number" id="quantityInput" name="quantity" required min="1" placeholder="Ví dụ: 50"
                           style="width: 100%; padding: 10px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 14px; box-sizing: border-box;" />
                </div>

                <button type="submit" style="width: 100%; background-color: #14532d; color: white; border: none; padding: 12px; border-radius: 8px; font-weight: 700; font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 10px rgba(20,83,45,0.12);" onmouseover="this.style.backgroundColor='#166534'" onmouseout="this.style.backgroundColor='#14532d'">
                    <i class="fa-solid fa-circle-check"></i> Xác nhận nhập kho
                </button>
            </form>
        </div>

        <!-- CURRENT STOCK REPORT GRID (II.12 Low stock visual) -->
        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08);">
            <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin-top: 0; margin-bottom: 15px; border-bottom: 1.5px solid rgba(20, 83, 45, 0.08); padding-bottom: 8px;">
                <i class="fa-solid fa-table-list"></i> Báo cáo trạng thái tồn kho
            </h3>
            
            <div style="overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 12px;">
                <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; color: #374151;">
                    <thead>
                        <tr style="background-color: #f9fafb; border-bottom: 1.5px solid #e5e7eb; color: #4b5563;">
                            <th style="padding: 12px 15px; font-weight: 700;">Mã SKU</th>
                            <th style="padding: 12px 15px; font-weight: 700;">Định lượng</th>
                            <th style="padding: 12px 15px; font-weight: 700;">Giá gốc (VNĐ)</th>
                            <th style="padding: 12px 15px; font-weight: 700;">Tồn kho</th>
                            <th style="padding: 12px 15px; font-weight: 700;">Đóng gói sẵn (II.8)</th>
                            <th style="padding: 12px 15px; font-weight: 700; text-align: center;">Tình trạng (II.12)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="v" items="${variants}">
                            <tr style="border-bottom: 1px solid #e5e7eb; transition: background-color 0.15s; 
                                       background-color: ${v.stockQuantity <= 5 ? '#fff5f5' : 'white'};">
                                <td style="padding: 12px 15px; font-family: monospace; font-weight: bold;"><c:out value="${v.sku}"/></td>
                                <td style="padding: 12px 15px; font-weight: 600;"><c:out value="${v.variantLabel}"/></td>
                                <td style="padding: 12px 15px;"><ft:currency value="${v.price}"/></td>
                                <td style="padding: 12px 15px; font-weight: 800; color: ${v.stockQuantity <= 5 ? '#ef4444' : '#1f2937'};"><c:out value="${v.stockQuantity}"/> quả</td>
                                <td style="padding: 12px 15px;"><c:out value="${v.packagingOption != null ? v.packagingOption : 'Không có'}"/></td>
                                <td style="padding: 12px 15px; text-align: center;">
                                    <c:choose>
                                        <c:when test="${v.stockQuantity == 0}">
                                            <span style="background-color: #fee2e2; color: #991b1b; padding: 2px 8px; border-radius: 20px; font-size: 10px; font-weight: 700;">HẾT HÀNG</span>
                                        </c:when>
                                        <c:when test="${v.stockQuantity <= 5}">
                                            <span style="background-color: #ffedd5; color: #9a3412; padding: 2px 8px; border-radius: 20px; font-size: 10px; font-weight: 700;">CẢNH BÁO ĐỎ</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background-color: #dcfce7; color: #14532d; padding: 2px 8px; border-radius: 20px; font-size: 10px; font-weight: 700;">AN TOÀN</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <!-- INVENTORY ADJUSTMENT AUDIT LOG HISTORY TABLE -->
    <div style="background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08);">
        <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin-top: 0; margin-bottom: 15px; border-bottom: 1.5px solid rgba(20, 83, 45, 0.08); padding-bottom: 8px;">
            <i class="fa-solid fa-clock-rotate-left"></i> Nhật ký biến động kho hàng (Audit Trail Logs)
        </h3>
        
        <div style="overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 12px;">
            <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; color: #374151;">
                <thead>
                    <tr style="background-color: #f9fafb; border-bottom: 1.5px solid #e5e7eb; color: #4b5563;">
                        <th style="padding: 12px 15px; font-weight: 700; width: 80px;">Mã Log</th>
                        <th style="padding: 12px 15px; font-weight: 700; width: 120px;">Mã Biến thể</th>
                        <th style="padding: 12px 15px; font-weight: 700;">Hành động</th>
                        <th style="padding: 12px 15px; font-weight: 700;">Biến động (+/-)</th>
                        <th style="padding: 12px 15px; font-weight: 700;">Tồn kho sau hành động</th>
                        <th style="padding: 12px 15px; font-weight: 700;">Ghi chú chi tiết</th>
                        <th style="padding: 12px 15px; font-weight: 700;">Thời gian thực hiện</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="log" items="${logs}">
                        <tr style="border-bottom: 1px solid #e5e7eb; transition: background-color 0.15s;">
                            <td style="padding: 12px 15px; font-weight: bold; color: #6b7280;">#<c:out value="${log.logId}"/></td>
                            <td style="padding: 12px 15px; font-family: monospace; font-weight: 600;"><c:out value="${log.variantId}"/></td>
                            <td style="padding: 12px 15px;">
                                <span style="font-weight: 700; color: 
                                      ${'MANUAL_ADJUST'.equals(log.changeType) ? '#16a34a' : 
                                        'ORDER_RESERVE'.equals(log.changeType) ? '#2563eb' : 
                                        'ORDER_RELEASE'.equals(log.changeType) ? '#f59e0b' : '#374151'};">
                                    <c:out value="${log.changeType}"/>
                                </span>
                            </td>
                            <td style="padding: 12px 15px; font-weight: bold; color: ${log.quantityDelta > 0 ? '#16a34a' : '#ef4444'};">
                                ${log.quantityDelta > 0 ? '+' : ''}<c:out value="${log.quantityDelta}"/> quả
                            </td>
                            <td style="padding: 12px 15px; font-weight: 700;"><c:out value="${log.quantityAfter}"/> quả</td>
                            <td style="padding: 12px 15px; color: #4b5563;"><c:out value="${log.note}"/></td>
                            <td style="padding: 12px 15px; color: #6b7280;">${log.changedAt}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const restockForm = document.getElementById("restockForm");
    const quantityInput = document.getElementById("quantityInput");

    // Client-side validation: II.13 Test case: negative/zero quantities must be rejected
    restockForm.addEventListener("submit", function(e) {
        const qty = parseInt(quantityInput.value);
        if (isNaN(qty) || qty <= 0) {
            alert("Lỗi: Số lượng nhập kho phải lớn hơn 0 quả!");
            e.preventDefault();
            return;
        }
    });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
