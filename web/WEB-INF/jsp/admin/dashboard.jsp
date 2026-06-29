<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan – Admin MetaFruit</title>
</head>
<body>
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="dashboard"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Tổng Quan Hệ Thống</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Xem nhanh các chỉ số vận hành quan trọng và các thao tác nhanh.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-leaf text-[#84cc16]"></i>
                <span class="text-xs font-bold uppercase tracking-wider">MetaFruit Live</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- KPI Cards Grid --%>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <%-- Total Users --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-1 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-users"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng người dùng</span>
                    <h3 class="text-2xl font-black text-txt mt-0.5">${totalUsers != null ? totalUsers : 0}</h3>
                </div>
            </div>

            <%-- Total Orders --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-1 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-primary-lt text-primary flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-cart-shopping"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng đơn hàng</span>
                    <h3 class="text-2xl font-black text-txt mt-0.5">${totalOrders != null ? totalOrders : 0}</h3>
                </div>
            </div>

            <%-- Refund Requests Pending --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-1 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-rotate-left"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Đổi trả chờ duyệt</span>
                    <h3 class="text-2xl font-black text-txt mt-0.5">${pendingRefunds != null ? pendingRefunds : 0}</h3>
                </div>
            </div>

            <%-- Unpaid Settlements --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-1 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-file-invoice-dollar"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Đối soát cần TT</span>
                    <h3 class="text-2xl font-black text-txt mt-0.5">${unpaidSettlements != null ? unpaidSettlements : 0}</h3>
                </div>
            </div>
        </div>

        <%-- Quick Access Section --%>
        <h2 class="text-base md:text-lg font-extrabold text-txt mb-4 flex items-center gap-2">
            <i class="fa-solid fa-bolt text-amber-500"></i> Truy cập nhanh
        </h2>
        
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <%-- User management --%>
            <div class="glass-card p-6 flex flex-col items-center text-center justify-between hover:shadow-card hover:-translate-y-1 transition-all duration-200">
                <div class="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-2xl mb-4 shadow-sm">
                    <i class="fa-solid fa-user-shield"></i>
                </div>
                <h3 class="text-base font-bold text-txt mb-2">Quản lý Người dùng</h3>
                <p class="text-xs text-txt-2 mb-6">Tra cứu, xem thông tin chi tiết, phân quyền, và khoá/mở khoá tài khoản thành viên.</p>
                <a href="${pageContext.request.contextPath}/admin/users" 
                   class="w-full py-2.5 bg-blue-50 text-blue-700 hover:bg-blue-600 hover:text-white rounded-xl text-xs font-bold transition-all duration-150 cursor-pointer">
                    Truy cập ngay
                </a>
            </div>

            <%-- Order Monitoring --%>
            <div class="glass-card p-6 flex flex-col items-center text-center justify-between hover:shadow-card hover:-translate-y-1 transition-all duration-200">
                <div class="w-14 h-14 rounded-2xl bg-primary-lt text-primary flex items-center justify-center text-2xl mb-4 shadow-sm">
                    <i class="fa-solid fa-box-open"></i>
                </div>
                <h3 class="text-base font-bold text-txt mb-2">Giám sát Đơn hàng</h3>
                <p class="text-xs text-txt-2 mb-6">Kiểm duyệt giao dịch chuyển khoản VietQR, đối soát COD và theo dõi hành trình đơn.</p>
                <a href="${pageContext.request.contextPath}/admin/orders" 
                   class="w-full py-2.5 bg-primary-lt text-primary hover:bg-primary hover:text-white rounded-xl text-xs font-bold transition-all duration-150 cursor-pointer">
                    Truy cập ngay
                </a>
            </div>

            <%-- Reports & Thống kê --%>
            <div class="glass-card p-6 flex flex-col items-center text-center justify-between hover:shadow-card hover:-translate-y-1 transition-all duration-200">
                <div class="w-14 h-14 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-2xl mb-4 shadow-sm">
                    <i class="fa-solid fa-chart-column"></i>
                </div>
                <h3 class="text-base font-bold text-txt mb-2">Báo cáo Thống kê</h3>
                <p class="text-xs text-txt-2 mb-6">Xem xu hướng doanh số, biểu đồ tăng trưởng và báo cáo sử dụng chi tiết trái cây.</p>
                <a href="${pageContext.request.contextPath}/admin/reports" 
                   class="w-full py-2.5 bg-amber-50 text-amber-700 hover:bg-amber-600 hover:text-white rounded-xl text-xs font-bold transition-all duration-150 cursor-pointer">
                    Truy cập ngay
                </a>
            </div>

            <%-- Send Notifications --%>
            <div class="glass-card p-6 flex flex-col items-center text-center justify-between hover:shadow-card hover:-translate-y-1 transition-all duration-200">
                <div class="w-14 h-14 rounded-2xl bg-purple-50 text-purple-600 flex items-center justify-center text-2xl mb-4 shadow-sm">
                    <i class="fa-solid fa-bullhorn"></i>
                </div>
                <h3 class="text-base font-bold text-txt mb-2">Gửi Thông báo</h3>
                <p class="text-xs text-txt-2 mb-6">Soạn và gửi thông báo khuyến mãi, bảo trì hệ thống tới các nhóm đối tượng người dùng.</p>
                <a href="${pageContext.request.contextPath}/admin/notifications" 
                   class="w-full py-2.5 bg-purple-50 text-purple-700 hover:bg-purple-600 hover:text-white rounded-xl text-xs font-bold transition-all duration-150 cursor-pointer">
                    Truy cập ngay
                </a>
            </div>
        </div>

    </main>
</div>
</body>
</html>
