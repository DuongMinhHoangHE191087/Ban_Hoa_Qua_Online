<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Theo dõi yêu cầu đổi trả | Kênh Người Bán</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">

    <!-- Tailwind & SweetAlert -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:         '#4d661c',
                        'primary-hover': '#364e03',
                        'primary-lt':    '#f0f7e6',
                        border:          '#e2ece7',
                        'txt':           '#0f172a',
                        'txt-2':         '#475569',
                        'txt-3':         '#94a3b8',
                    },
                    fontFamily: { sans: ['Lexend', 'sans-serif'] }
                }
            }
        }
    </script>

    <style>
        body { background-color: #f4fbf7; font-family: 'Lexend', sans-serif; }
        .glass-card {
            background: #ffffff;
            border: 1px solid #e2ece7;
            box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 4px 16px -4px rgba(20,83,45,.06);
        }
        .table-responsive { width: 100%; overflow-x: auto; }
        .table { width: 100%; border-collapse: collapse; text-align: left; }
        .table th, .table td { padding: 1rem 1.25rem; border-bottom: 1px solid #e2ece7; font-size: 0.9rem; }
        .table th { background-color: #f8fcf9; font-weight: 600; color: #0f172a; text-transform: uppercase; letter-spacing: 0.05em; font-size: 0.8rem; }
        .table tr:hover { background-color: rgba(77, 102, 28, 0.015); }
    </style>
</head>
<body class="antialiased text-[#0f172a]">
<div class="flex min-h-screen">

    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="orders"/>
    </jsp:include>

    <!-- Main Content -->
    <main class="flex-1 p-6 md:p-8 overflow-y-auto">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Yêu cầu Đổi trả hàng</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Quản lý và theo dõi các yêu cầu đổi trả, hoàn tiền từ khách hàng.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-white/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-rotate-left text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Đổi trả</span>
            </div>
        </div>

        <!-- Main Card -->
        <div class="glass-card rounded-2xl overflow-hidden mb-8">
            <div class="p-6 border-b border-[#e2ece7] bg-[#f9fdf9] flex justify-between items-center">
                <h2 class="text-base font-bold text-txt flex items-center gap-2">
                    <i class="fa-solid fa-clock-rotate-left text-primary"></i> Lịch sử yêu cầu đổi trả
                </h2>
                <span class="text-xs text-txt-2 bg-primary-lt px-3 py-1 rounded-full border border-primary/10">
                    Gian hàng trung gian
                </span>
            </div>
            <div class="p-0">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th class="text-center">Mã YC</th>
                                <th>Đơn hàng</th>
                                <th>Sản phẩm & Phân loại</th>
                                <th>Khách hàng</th>
                                <th class="text-center">Loại YC</th>
                                <th>Phân loại bán Lý do & Mô tả</th>
                                <th class="text-center">Số lượng</th>
                                <th class="text-right">Số tiền hoàn</th>
                                <th class="text-center">Trạng thái</th>
                                <th>Ngày tạo</th>
                                <th>Phán quyết của Sàn</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="req" items="${returnRequests}">
                                <tr>
                                    <td class="text-center font-bold text-primary">#<c:out value="${req.returnRequestId}"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/shop/orders?action=detail&orderId=${req.orderId}" class="text-primary hover:underline font-semibold">
                                            #<c:out value="${req.orderId}"/>
                                        </a>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty req.productName}">
                                                <div class="font-bold text-txt"><c:out value="${req.productName}"/></div>
                                                <c:if test="${not empty req.variantLabel}">
                                                    <span class="inline-block mt-0.5 text-[11px] bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
                                                        Phân loại: <c:out value="${req.variantLabel}"/>
                                                    </span>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-txt-3 italic text-xs">Toàn bộ đơn hàng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="text-xs text-txt-2 font-medium">Khách hàng ID:</div>
                                        <div class="font-semibold text-txt">#<c:out value="${req.customerId}"/></div>
                                    </td>
                                    <td class="text-center">
                                        <span class="inline-block px-2.5 py-1 rounded-full text-xs font-bold ${req.requestType == 'RETURN' ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-blue-50 text-blue-700 border border-blue-200'}">
                                            <c:out value="${req.requestType}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="font-bold text-txt text-xs"><c:out value="${req.reasonCode}"/></div>
                                        <c:if test="${not empty req.description}">
                                            <div class="text-txt-2 text-xs mt-1 bg-slate-50 p-2 rounded border border-slate-100"><c:out value="${req.description}"/></div>
                                        </c:if>
                                        <c:if test="${not empty req.evidenceUrl}">
                                            <div class="mt-1.5">
                                                <a href="<c:out value="${req.evidenceUrl}"/>" target="_blank" class="inline-flex items-center gap-1 text-[11px] text-primary hover:underline font-bold bg-[#edf7f2] px-2 py-1 rounded border border-[#bbf7d0]">
                                                    <i class="fa-solid fa-image"></i> Xem minh chứng
                                                </a>
                                            </div>
                                        </c:if>
                                    </td>
                                    <td class="text-center font-bold text-txt"><c:out value="${req.requestedQuantity}"/></td>
                                    <td class="text-right font-extrabold text-red-600"><c:out value="${req.refundAmount}"/>đ</td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${req.status == 'REQUESTED'}">
                                                <span class="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">Đang chờ</span>
                                            </c:when>
                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">Đã nhận</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">Từ chối</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-xs text-txt-2 font-medium"><c:out value="${req.createdAt}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${req.status == 'REQUESTED'}">
                                                <span class="text-amber-600 font-bold text-xs italic flex items-center gap-1">
                                                    <i class="fa-solid fa-spinner fa-spin"></i> Đang chờ xử lý...
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="text-xs font-bold text-txt">
                                                    ${req.status == 'APPROVED' ? 'Chấp nhận hoàn tiền' : 'Từ chối hoàn trả'}
                                                </div>
                                                <c:if test="${not empty req.decisionReason}">
                                                    <div class="text-[11px] text-txt-2 italic mt-0.5">Lý do: <c:out value="${req.decisionReason}"/></div>
                                                </c:if>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty returnRequests}">
                                <tr>
                                    <td colspan="11" class="text-center py-8 text-txt-3 italic">
                                        Chưa có yêu cầu đổi trả nào từ khách hàng.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
