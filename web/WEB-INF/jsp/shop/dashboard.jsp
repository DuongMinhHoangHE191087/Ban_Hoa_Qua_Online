<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Dashboard Cửa Hàng - MetaFruit"/></jsp:include>

<div class="shop-container" style="max-width: 1100px; margin: 40px auto; padding: 0 15px; font-family: 'Plus Jakarta Sans', sans-serif;">
    
    <!-- DASHBOARD HEADER -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px;">
        <h1 style="color: #14532d; font-size: 28px; font-weight: 700; margin: 0;">
            <i class="fa-solid fa-chart-line" style="margin-right: 10px;"></i>
            Tổng quan vận hành cửa hàng
        </h1>
        <div style="display: flex; gap: 10px;">
            <a href="${pageContext.request.contextPath}/shop/products?action=create" style="text-decoration: none; background-color: #14532d; color: white; padding: 10px 20px; border-radius: 8px; font-weight: 700; font-size: 14px; box-shadow: 0 4px 12px rgba(20,83,45,0.15);">
                <i class="fa-solid fa-plus"></i> Đăng quả mới
            </a>
            <a href="${pageContext.request.contextPath}/shop/inventory" style="text-decoration: none; background-color: white; color: #14532d; border: 1.5px solid #14532d; padding: 8px 18px; border-radius: 8px; font-weight: 700; font-size: 14px;">
                <i class="fa-solid fa-warehouse"></i> Quản lý kho
            </a>
        </div>
    </div>

    <!-- FLASH MESSAGES -->
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

    <!-- LOW STOCK WARN PANEL (II.12) -->
    <c:if test="${not empty lowStock}">
        <div style="background-color: #fef2f2; border: 1.5px solid #fca5a5; border-radius: 16px; padding: 20px; margin-bottom: 30px; box-shadow: 0 6px 15px rgba(239, 68, 68, 0.05); position: relative; overflow: hidden;">
            <div style="position: absolute; top: 0; left: 0; bottom: 0; width: 6px; background-color: #ef4444;"></div>
            
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                <h3 style="color: #991b1b; font-size: 17px; font-weight: 800; margin: 0; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-triangle-exclamation" style="color: #ef4444; font-size: 18px;"></i>
                    CẢNH BÁO: CÓ BIẾN THỂ TRÁI CÂY SẮP HẾT HÀNG (II.12)
                </h3>
                <a href="${pageContext.request.contextPath}/shop/inventory" style="text-decoration: none; background-color: #ef4444; color: white; padding: 6px 14px; border-radius: 6px; font-size: 12px; font-weight: 700; display: flex; align-items: center; gap: 5px;">
                    <i class="fa-solid fa-truck-ramp-box"></i> Nhập thêm hàng ngay
                </a>
            </div>
            
            <div style="overflow-x: auto; background: white; border-radius: 10px; border: 1px solid #fee2e2;">
                <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; color: #374151;">
                    <thead>
                        <tr style="background-color: #fff5f5; border-bottom: 1px solid #fee2e2; color: #991b1b;">
                            <th style="padding: 10px 15px; font-weight: 700;">Mã Biến thể (SKU)</th>
                            <th style="padding: 10px 15px; font-weight: 700;">Khối lượng định lượng</th>
                            <th style="padding: 10px 15px; font-weight: 700;">Số lượng tồn kho còn lại</th>
                            <th style="padding: 10px 15px; font-weight: 700; text-align: center;">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="ls" items="${lowStock}">
                            <tr style="border-bottom: 1px solid #fee2e2; background-color: #fffdfd;">
                                <td style="padding: 10px 15px; font-family: monospace; font-weight: bold;"><c:out value="${ls.sku}"/></td>
                                <td style="padding: 10px 15px; font-weight: 600;"><c:out value="${ls.variantLabel}"/></td>
                                <td style="padding: 10px 15px; font-weight: 800; color: #ef4444; font-size: 14px;"><c:out value="${ls.stockQuantity}"/> quả</td>
                                <td style="padding: 10px 15px; text-align: center;">
                                    <span style="background-color: #fee2e2; color: #991b1b; padding: 2px 8px; border-radius: 20px; font-size: 11px; font-weight: 700;">
                                        ${ls.stockQuantity == 0 ? 'HẾT HÀNG' : 'SẮP HẾT'}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- KPI DASHBOARD METRICS WIDGETS -->
    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; margin-bottom: 40px;">
        
        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08); display: flex; align-items: center; gap: 15px;">
            <div style="background-color: #f0fdf4; width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #16a34a; font-size: 20px;">
                <i class="fa-solid fa-sack-dollar"></i>
            </div>
            <div>
                <div style="color: #6b7280; font-size: 13px; font-weight: 500;">Doanh số tháng này</div>
                <h3 style="margin: 3px 0 0 0; color: #1f2937; font-size: 20px; font-weight: 800;"><ft:currency value="${revenue}"/></h3>
            </div>
        </div>

        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08); display: flex; align-items: center; gap: 15px;">
            <div style="background-color: #eff6ff; width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #3b82f6; font-size: 20px;">
                <i class="fa-solid fa-cart-shopping"></i>
            </div>
            <div>
                <div style="color: #6b7280; font-size: 13px; font-weight: 500;">Số lượng đơn đặt hàng</div>
                <h3 style="margin: 3px 0 0 0; color: #1f2937; font-size: 20px; font-weight: 800;"><c:out value="${orderCount}"/> đơn đặt</h3>
            </div>
        </div>

        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08); display: flex; align-items: center; gap: 15px;">
            <div style="background-color: #fff7ed; width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #f97316; font-size: 20px;">
                <i class="fa-solid fa-handshake"></i>
            </div>
            <div>
                <div style="color: #6b7280; font-size: 13px; font-weight: 500;">Trạng thái đối soát</div>
                <h3 style="margin: 3px 0 0 0; color: #f97316; font-size: 20px; font-weight: 800;"><c:out value="${settlementStatus}"/></h3>
            </div>
        </div>

    </div>

    <!-- QUICK OPERATIONS SECTION -->
    <div style="background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); border: 1px solid rgba(20, 83, 45, 0.08);">
        <h3 style="color: #14532d; font-size: 18px; font-weight: 700; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 10px; margin-top: 0; margin-bottom: 20px;">Thao tác nhanh cửa hàng</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 15px;">
            
            <a href="${pageContext.request.contextPath}/shop/products" style="text-decoration: none; color: inherit; border: 1px solid #e5e7eb; border-radius: 12px; padding: 15px; display: flex; align-items: center; gap: 15px; transition: border-color 0.2s;" onmouseover="this.style.borderColor='#14532d'" onmouseout="this.style.borderColor='#e5e7eb'">
                <div style="background-color: #f0fdf4; width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #16a34a; font-size: 16px;"><i class="fa-solid fa-boxes-stacked"></i></div>
                <div>
                    <h4 style="margin: 0; font-size: 14px; font-weight: 700; color: #374151;">Xem danh sách quả</h4>
                    <span style="color: #6b7280; font-size: 12px;">Chỉnh sửa, ẩn hoặc cập nhật quả của bạn</span>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/shop/inventory" style="text-decoration: none; color: inherit; border: 1px solid #e5e7eb; border-radius: 12px; padding: 15px; display: flex; align-items: center; gap: 15px; transition: border-color 0.2s;" onmouseover="this.style.borderColor='#14532d'" onmouseout="this.style.borderColor='#e5e7eb'">
                <div style="background-color: #f0fdf4; width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #16a34a; font-size: 16px;"><i class="fa-solid fa-truck-ramp-box"></i></div>
                <div>
                    <h4 style="margin: 0; font-size: 14px; font-weight: 700; color: #374151;">Nhập thêm hàng hóa</h4>
                    <span style="color: #6b7280; font-size: 12px;">Nhập thêm tồn kho, xem log biến động kho</span>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/shop/orders" style="text-decoration: none; color: inherit; border: 1px solid #e5e7eb; border-radius: 12px; padding: 15px; display: flex; align-items: center; gap: 15px; transition: border-color 0.2s;" onmouseover="this.style.borderColor='#14532d'" onmouseout="this.style.borderColor='#e5e7eb'">
                <div style="background-color: #f0fdf4; width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #16a34a; font-size: 16px;"><i class="fa-solid fa-clipboard-list"></i></div>
                <div>
                    <h4 style="margin: 0; font-size: 14px; font-weight: 700; color: #374151;">Quản lý đơn hàng</h4>
                    <span style="color: #6b7280; font-size: 12px;">Xác nhận đơn, cập nhật chuẩn bị hoa quả</span>
                </div>
            </a>

        </div>
    </div>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
