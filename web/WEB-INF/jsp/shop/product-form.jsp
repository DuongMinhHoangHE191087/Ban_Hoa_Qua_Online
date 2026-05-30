<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="${product != null ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}"/></jsp:include>

<div class="shop-container" style="max-width: 1000px; margin: 40px auto; padding: 20px; font-family: 'Plus Jakarta Sans', sans-serif;">
    <div style="background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(10px); border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); border: 1px solid rgba(20, 83, 45, 0.1);">
        
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 15px;">
            <h1 style="color: #14532d; font-size: 28px; font-weight: 700; margin: 0;">
                <i class="fa-solid fa-square-plus" style="margin-right: 10px;"></i>
                ${product != null ? 'Chỉnh sửa sản phẩm' : 'Đăng bán sản phẩm mới'}
            </h1>
            <a href="${pageContext.request.contextPath}/shop/products" style="text-decoration: none; color: #6b7280; font-weight: 600; display: flex; align-items: center; gap: 5px; transition: color 0.2s;" onmouseover="this.style.color='#14532d'" onmouseout="this.style.color='#6b7280'">
                <i class="fa-solid fa-arrow-left"></i> Quay lại
            </a>
        </div>

        <c:if test="${not empty sessionScope.flashMsg}">
            <div style="padding: 15px; border-radius: 10px; margin-bottom: 20px; font-weight: 500; font-size: 14px; 
                        background-color: ${sessionScope.flashType == 'success' ? '#dcfce7' : '#fee2e2'}; 
                        color: ${sessionScope.flashType == 'success' ? '#14532d' : '#991b1b'}; 
                        border: 1px solid ${sessionScope.flashType == 'success' ? '#bbf7d0' : '#fca5a5'};">
                <i class="fa-solid ${sessionScope.flashType == 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}" style="margin-right: 8px;"></i>
                <c:out value="${sessionScope.flashMsg}"/>
            </div>
            <% session.removeAttribute("flashMsg"); session.removeAttribute("flashType"); %>
        </c:if>

        <form id="productForm" action="${pageContext.request.contextPath}/shop/products" method="POST">
            <input type="hidden" name="id" value="${product != null ? product.productId : ''}" />
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}" />

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px;">
                
                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Tên sản phẩm <span style="color: #ef4444;">*</span></label>
                    <input type="text" name="name" value="<c:out value='${product.name}'/>" required placeholder="Ví dụ: Măng cụt Lái Thiêu"
                           style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; transition: border-color 0.2s;" 
                           onfocus="this.style.borderColor='#14532d'" onblur="this.style.borderColor='#d1d5db'" />
                </div>

                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Danh mục <span style="color: #ef4444;">*</span></label>
                    <select name="categoryId" required style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; background: white; transition: border-color 0.2s;">
                        <option value="">-- Chọn danh mục --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.categoryId}" ${product != null && product.categoryId == cat.categoryId ? 'selected' : ''}>
                                <c:out value="${cat.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </div>

            <div style="margin-bottom: 25px;">
                <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Mô tả chi tiết</label>
                <textarea name="description" rows="4" placeholder="Nhập hương vị, lợi ích sức khỏe, cam kết nguồn gốc..."
                          style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; transition: border-color 0.2s;"
                          onfocus="this.style.borderColor='#14532d'" onblur="this.style.borderColor='#d1d5db'"><c:out value="${product.description}"/></textarea>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px;">
                
                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Quốc gia xuất xứ</label>
                    <input type="text" name="originCountry" value="<c:out value='${product.originCountry}'/>" placeholder="Ví dụ: Việt Nam"
                           style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box;" />
                </div>

                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Vùng trồng trọt</label>
                    <input type="text" name="originRegion" value="<c:out value='${product.originRegion}'/>" placeholder="Ví dụ: Bình Dương"
                           style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box;" />
                </div>

            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px; margin-bottom: 25px;">
                
                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Ngày thu hoạch</label>
                    <input type="date" name="harvestDate" value="${product != null ? product.harvestDate : ''}"
                           style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box;" />
                </div>

                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Hạn sử dụng (ngày)</label>
                    <input type="number" name="shelfLifeDays" value="${product != null ? product.shelfLifeDays : ''}" min="1" placeholder="Ví dụ: 14"
                           style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box;" />
                </div>

                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Nhãn dán đặc biệt (II.9)</label>
                    <select name="labelType" style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; background: white;">
                        <option value="">Không sử dụng</option>
                        <option value="Organic" ${product != null && 'Organic'.equals(product.labelType) ? 'selected' : ''}>Hữu cơ (Organic)</option>
                        <option value="Imported" ${product != null && 'Imported'.equals(product.labelType) ? 'selected' : ''}>Nhập khẩu (Imported)</option>
                    </select>
                </div>

            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px; background: rgba(20, 83, 45, 0.04); padding: 15px; border-radius: 12px; border: 1px dashed rgba(20, 83, 45, 0.2);">
                
                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #14532d;"><i class="fa-solid fa-calendar-days"></i> Mùa vụ bắt đầu (Tháng - II.10)</label>
                    <select name="seasonStart" style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; background: white;">
                        <option value="">Bán quanh năm</option>
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${product != null && product.seasonStart == m ? 'selected' : ''}>Tháng ${m}</option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #14532d;"><i class="fa-solid fa-calendar-check"></i> Mùa vụ kết thúc (Tháng - II.10)</label>
                    <select name="seasonEnd" style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box; background: white;">
                        <option value="">Bán quanh năm</option>
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${product != null && product.seasonEnd == m ? 'selected' : ''}>Tháng ${m}</option>
                        </c:forEach>
                    </select>
                </div>

            </div>

            <div style="margin-bottom: 25px;">
                <label style="display: block; font-weight: 600; margin-bottom: 8px; color: #374151;">Hướng dẫn bảo quản</label>
                <input type="text" name="storageInstruction" value="<c:out value='${product.storageInstruction}'/>" placeholder="Ví dụ: Bảo quản mát tủ lạnh từ 4 - 8 độ C"
                       style="width: 100%; padding: 12px; border: 1.5px solid #d1d5db; border-radius: 10px; font-size: 15px; box-sizing: border-box;" />
            </div>

            <!-- DYNAMIC WEIGHT VARIANTS BUILDER PANEL (II.7) -->
            <div style="margin-bottom: 30px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin: 0;">
                        <i class="fa-solid fa-scale-balanced" style="margin-right: 8px;"></i>
                        Thiết lập các biến thể khối lượng và giá (II.7, II.8, II.14, II.15)
                    </h3>
                    <button type="button" id="addVariantBtn" style="background-color: #14532d; color: white; border: none; padding: 8px 15px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 5px; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#166534'" onmouseout="this.style.backgroundColor='#14532d'">
                        <i class="fa-solid fa-plus"></i> Thêm biến thể
                    </button>
                </div>

                <div style="overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 14px;">
                        <thead>
                            <tr style="background-color: #f9fafb; border-bottom: 1.5px solid #e5e7eb; color: #4b5563;">
                                <th style="padding: 12px 15px; font-weight: 600;">Khối lượng (gam)</th>
                                <th style="padding: 12px 15px; font-weight: 600;">Giá bán gốc (VNĐ)</th>
                                <th style="padding: 12px 15px; font-weight: 600;">Giá khuyến mãi (VNĐ - Tùy chọn)</th>
                                <th style="padding: 12px 15px; font-weight: 600;">Số lượng tồn kho ban đầu</th>
                                <th style="padding: 12px 15px; font-weight: 600;">Đóng gói sẵn (II.8)</th>
                                <th style="padding: 12px 15px; font-weight: 600; text-align: center; width: 60px;">Xóa</th>
                            </tr>
                        </thead>
                        <tbody id="variantTableBody">
                            <!-- Prepopulate variants if editing -->
                            <c:choose>
                                <c:when test="${not empty variants}">
                                    <c:forEach var="v" items="${variants}">
                                        <tr style="border-bottom: 1px solid #e5e7eb; transition: background-color 0.15s;">
                                            <td style="padding: 10px 15px;">
                                                <input type="number" name="weights[]" value="${v.weightGrams}" required min="1" placeholder="Ví dụ: 1000" style="width: 100px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                            </td>
                                            <td style="padding: 10px 15px;">
                                                <input type="number" name="prices[]" value="${v.price.intValue()}" required min="1000" placeholder="Ví dụ: 50000" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                            </td>
                                            <td style="padding: 10px 15px;">
                                                <input type="number" name="discountPrices[]" value="${v.discountPrice != null ? v.discountPrice.intValue() : ''}" min="0" placeholder="Trống nếu không KM" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                            </td>
                                            <td style="padding: 10px 15px;">
                                                <input type="number" name="stocks[]" value="${v.stockQuantity}" required min="0" placeholder="Ví dụ: 100" style="width: 90px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                            </td>
                                            <td style="padding: 10px 15px;">
                                                <select name="packagingOptions[]" style="width: 110px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; background: white;">
                                                    <option value="">Không có</option>
                                                    <option value="Gift Box" ${'Gift Box'.equals(v.packagingOption) ? 'selected' : ''}>Gift Box (+50k)</option>
                                                    <option value="Foam Tray" ${'Foam Tray'.equals(v.packagingOption) ? 'selected' : ''}>Foam Tray (+15k)</option>
                                                </select>
                                            </td>
                                            <td style="padding: 10px 15px; text-align: center;">
                                                <button type="button" class="remove-row-btn" style="background: none; border: none; color: #ef4444; cursor: pointer; font-size: 16px; transition: color 0.15s;" onmouseover="this.style.color='#b91c1c'" onmouseout="this.style.color='#ef4444'"><i class="fa-solid fa-trash-can"></i></button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr style="border-bottom: 1px solid #e5e7eb; transition: background-color 0.15s;">
                                        <td style="padding: 10px 15px;">
                                            <input type="number" name="weights[]" required min="1" placeholder="Ví dụ: 1000" style="width: 100px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                        </td>
                                        <td style="padding: 10px 15px;">
                                            <input type="number" name="prices[]" required min="1000" placeholder="Ví dụ: 50000" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                        </td>
                                        <td style="padding: 10px 15px;">
                                            <input type="number" name="discountPrices[]" min="0" placeholder="Trống nếu không KM" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                        </td>
                                        <td style="padding: 10px 15px;">
                                            <input type="number" name="stocks[]" required min="0" placeholder="Ví dụ: 100" style="width: 90px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
                                        </td>
                                        <td style="padding: 10px 15px;">
                                            <select name="packagingOptions[]" style="width: 110px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; background: white;">
                                                <option value="">Không có</option>
                                                <option value="Gift Box">Gift Box (+50k)</option>
                                                <option value="Foam Tray">Foam Tray (+15k)</option>
                                            </select>
                                        </td>
                                        <td style="padding: 10px 15px; text-align: center;">
                                            <button type="button" class="remove-row-btn" style="background: none; border: none; color: #ef4444; cursor: pointer; font-size: 16px; transition: color 0.15s;" onmouseover="this.style.color='#b91c1c'" onmouseout="this.style.color='#ef4444'"><i class="fa-solid fa-trash-can"></i></button>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <div style="display: flex; gap: 15px; justify-content: flex-end; margin-top: 35px; border-top: 1.5px solid #e5e7eb; padding-top: 20px;">
                <button type="button" onclick="window.location.href='${pageContext.request.contextPath}/shop/products'" style="background-color: #f3f4f6; color: #4b5563; border: 1px solid #d1d5db; padding: 12px 25px; border-radius: 10px; font-weight: 600; cursor: pointer; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#e5e7eb'" onmouseout="this.style.backgroundColor='#f3f4f6'">
                    Hủy bỏ
                </button>
                <button type="submit" style="background-color: #14532d; color: white; border: none; padding: 12px 35px; border-radius: 10px; font-weight: 700; font-size: 15px; cursor: pointer; display: flex; align-items: center; gap: 8px; box-shadow: 0 4px 15px rgba(20, 83, 45, 0.15); transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#166534'" onmouseout="this.style.backgroundColor='#14532d'">
                    <i class="fa-solid fa-circle-check"></i> Lưu sản phẩm
                </button>
            </div>
        </form>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const tableBody = document.getElementById("variantTableBody");
    const addBtn = document.getElementById("addVariantBtn");

    // Add variant row
    addBtn.addEventListener("click", function() {
        const row = document.createElement("tr");
        row.style.borderBottom = "1px solid #e5e7eb";
        row.style.transition = "background-color 0.15s";
        row.innerHTML = `
            <td style="padding: 10px 15px;">
                <input type="number" name="weights[]" required min="1" placeholder="Ví dụ: 1000" style="width: 100px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
            </td>
            <td style="padding: 10px 15px;">
                <input type="number" name="prices[]" required min="1000" placeholder="Ví dụ: 50000" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
            </td>
            <td style="padding: 10px 15px;">
                <input type="number" name="discountPrices[]" min="0" placeholder="Trống nếu không KM" style="width: 130px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
            </td>
            <td style="padding: 10px 15px;">
                <input type="number" name="stocks[]" required min="0" placeholder="Ví dụ: 100" style="width: 90px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box;" />
            </td>
            <td style="padding: 10px 15px;">
                <select name="packagingOptions[]" style="width: 110px; padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; box-sizing: border-box; background: white;">
                    <option value="">Không có</option>
                    <option value="Gift Box">Gift Box (+50k)</option>
                    <option value="Foam Tray">Foam Tray (+15k)</option>
                </select>
            </td>
            <td style="padding: 10px 15px; text-align: center;">
                <button type="button" class="remove-row-btn" style="background: none; border: none; color: #ef4444; cursor: pointer; font-size: 16px; transition: color 0.15s;" onmouseover="this.style.color='#b91c1c'" onmouseout="this.style.color='#ef4444'"><i class="fa-solid fa-trash-can"></i></button>
            </td>
        `;
        tableBody.appendChild(row);
    });

    // Remove variant row
    tableBody.addEventListener("click", function(e) {
        const removeBtn = e.target.closest(".remove-row-btn");
        if (removeBtn) {
            const rows = tableBody.querySelectorAll("tr");
            if (rows.length <= 1) {
                alert("Sản phẩm phải có ít nhất một biến thể!");
                return;
            }
            const row = removeBtn.closest("tr");
            row.remove();
        }
    });

    // Form validation
    const form = document.getElementById("productForm");
    form.addEventListener("submit", function(e) {
        const weights = Array.from(document.querySelectorAll('input[name="weights[]"]')).map(el => parseInt(el.value));
        const prices = Array.from(document.querySelectorAll('input[name="prices[]"]')).map(el => parseFloat(el.value));
        const discountPrices = Array.from(document.querySelectorAll('input[name="discountPrices[]"]')).map(el => el.value ? parseFloat(el.value) : null);

        // 1. Check duplicate weights
        const weightSet = new Set();
        for (let i = 0; i < weights.length; i++) {
            if (weightSet.has(weights[i])) {
                alert("Lỗi: Các biến thể khối lượng không được trùng lặp (" + weights[i] + "g)!");
                e.preventDefault();
                return;
            }
            weightSet.add(weights[i]);
        }

        // 2. Check discount prices bounds (must be < base price)
        for (let i = 0; i < prices.length; i++) {
            const discPrice = discountPrices[i];
            if (discPrice !== null) {
                if (discPrice >= prices[i]) {
                    alert("Lỗi: Giá khuyến mãi (" + discPrice + " VNĐ) tại dòng " + (i + 1) + " phải nhỏ hơn giá gốc (" + prices[i] + " VNĐ)!");
                    e.preventDefault();
                    return;
                }
                if (discPrice <= 0) {
                    alert("Lỗi: Giá khuyến mãi phải lớn hơn 0 VNĐ!");
                    e.preventDefault();
                    return;
                }
            }
        }
    });
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
