<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hóa Đơn Điện Tử #${order.orderId} - METAFRUIT</title>
    
    <!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&amp;display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

    <script id="tailwind-config">
      tailwind.config = {
        darkMode: "class",
        theme: {
          extend: {
            "colors": {
              "primary-fixed": "#ceee93",
              "primary": "#4d661c",
              "primary-container": "#d9f99d",
              "surface": "#eaffea",
              "on-secondary-container": "#386f50",
              "inverse-surface": "#00391a",
              "outline-variant": "#c5c8b7",
              "surface-variant": "#b1f2be",
              "surface-container": "#bcfdc9",
              "on-surface-variant": "#44483b",
              "background": "#eaffea",
              "secondary": "#31694b",
              "surface-container-high": "#b7f7c3",
              "error": "#ba1a1a",
              "error-container": "#ffdad6"
            },
            "fontFamily": {
              "sans": ["Lexend", "sans-serif"]
            }
          }
        }
      }
    </script>
    
</head>
<body class="bg-slate-50 font-sans text-slate-800 antialiased min-h-screen py-8 print:py-0 print:bg-white">

<div class="max-w-4xl mx-auto px-4 print:px-0">
    <!-- Nút hành động (ẩn khi in) -->
    <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6 print-hidden">
        <a href="${pageContext.request.contextPath}/profile/order-detail?orderId=${order.orderId}" 
           class="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-primary transition-all">
            <span class="material-symbols-outlined text-lg">arrow_back</span>
            Quay lại chi tiết đơn hàng
        </a>
        <button onclick="window.print();" 
                class="inline-flex items-center gap-2 bg-primary hover:bg-opacity-90 text-white font-bold py-3 px-6 rounded-xl transition-all shadow-md active:scale-95 transform cursor-pointer">
            <span class="material-symbols-outlined text-lg">print</span>
            In / Tải Hóa Đơn (PDF)
        </button>
    </div>

    <!-- Khung Hóa Đơn Premium -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-xl p-8 md:p-12 print-card">
        <!-- Header Hóa đơn -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center border-b-2 border-primary pb-6 mb-8 gap-6 print:border-black">
            <div>
                <div class="text-3xl font-black text-primary tracking-wider mb-2 print:text-black">METAFRUIT</div>
                <p class="text-xs text-slate-500 font-medium">Hệ thống Trái cây Cao cấp Việt Nam</p>
                <p class="text-xs text-slate-500 mt-1">Địa chỉ: 123 Đường Láng, Đống Đa, Hà Nội</p>
                <p class="text-xs text-slate-500">MST: 01099887766 • Hotline: 1900 8888</p>
            </div>
            <div class="md:text-right">
                <h1 class="text-2xl font-bold text-slate-800 tracking-tight mb-2 print:text-black">HÓA ĐƠN BÁN HÀNG</h1>
                <p class="text-sm font-medium text-slate-600">Mã Hóa Đơn: <strong class="text-slate-800">#INV-${order.orderId}</strong></p>
                <p class="text-xs text-slate-500 mt-1">Ngày Xuất: ${order.updatedAt}</p>
            </div>
        </div>

        <!-- Thông tin hai bên -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8 text-sm">
            <div class="space-y-1">
                <h6 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Thông tin người mua:</h6>
                <p><span class="font-medium text-slate-500">Khách hàng:</span> <strong class="text-slate-800">Khách mua hàng ẩn danh</strong></p>
                <p class="leading-relaxed"><span class="font-medium text-slate-500">Địa chỉ giao hàng:</span> <strong class="text-slate-800">${order.deliveryAddress}</strong></p>
                <p><span class="font-medium text-slate-500">Số điện thoại:</span> <strong class="text-slate-800">Liên hệ qua hệ thống</strong></p>
            </div>
            <div class="md:text-right space-y-1">
                <h6 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Chi tiết thanh toán:</h6>
                <p><span class="font-medium text-slate-500">Mã tham chiếu đơn hàng:</span> <strong class="text-slate-800">#${order.orderId}</strong></p>
                <p><span class="font-medium text-slate-500">Phương thức thanh toán:</span> 
                    <strong class="text-slate-800">
                        <c:choose>
                            <c:when test="${order.paymentMethod == 'COD'}">Thanh toán khi nhận hàng (COD)</c:when>
                            <c:when test="${order.paymentMethod == 'CK'}">Chuyển khoản QR ngân hàng</c:when>
                            <c:otherwise>${order.paymentMethod}</c:otherwise>
                        </c:choose>
                    </strong>
                </p>
                <p class="flex items-center md:justify-end gap-1.5 mt-1">
                    <span class="font-medium text-slate-500">Trạng thái:</span>
                    <span class="inline-flex px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 print:border print:border-black print:bg-white print:text-black">
                        Đã Thanh Toán
                    </span>
                </p>
            </div>
        </div>

        <!-- Bảng sản phẩm -->
        <div class="overflow-x-auto mb-8 border border-slate-100 rounded-xl">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="bg-slate-50 text-slate-500 text-xs font-bold uppercase tracking-wider">
                        <th class="py-4 px-6 text-center w-16">STT</th>
                        <th class="py-4 px-6">Tên Sản Phẩm / Phân Loại</th>
                        <th class="py-4 px-6 text-center w-28">Số Lượng</th>
                        <th class="py-4 px-6 text-right w-36">Đơn Giá</th>
                        <th class="py-4 px-6 text-right w-44">Thành Tiền</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-sm font-medium text-slate-700">
                    <c:forEach var="item" varStatus="status" items="${orderItems}">
                        <tr class="hover:bg-slate-50/40 transition-colors">
                            <td class="py-4 px-6 text-center text-slate-400">${status.index + 1}</td>
                            <td class="py-4 px-6">
                                <div class="font-semibold text-slate-800">${item.productNameSnapshot}</div>
                                <div class="text-xs text-slate-400 font-normal mt-0.5">
                                    Phân loại: ${item.variantLabelSnapshot}
                                    <c:if test="${not empty item.packagingLabelSnapshot}">
                                        • Đóng gói: ${item.packagingLabelSnapshot}
                                    </c:if>
                                </div>
                            </td>
                            <td class="py-4 px-6 text-center">${item.quantity}</td>
                            <td class="py-4 px-6 text-right font-medium">
                                <ft:currency value="${item.unitPrice}"/>
                                <c:if test="${item.packagingPriceSnapshot > 0}">
                                    <div class="text-[10px] text-slate-400 font-normal">+<ft:currency value="${item.packagingPriceSnapshot}"/> bao bì</div>
                                </c:if>
                            </td>
                            <td class="py-4 px-6 text-right font-semibold text-slate-800"><ft:currency value="${item.subtotal}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <!-- Khối tính tiền -->
        <div class="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
            <div class="md:col-span-6">
                <p class="text-xs text-slate-400 italic mt-2 leading-relaxed">
                    Cảm ơn quý khách đã tin dùng nông sản tươi sạch cao cấp của MetaFruit!<br/>
                    Hệ thống bảo quản lạnh cold-chain giúp giữ trọn vẹn dinh dưỡng của sản phẩm đến tay khách hàng.
                </p>
            </div>
            <div class="md:col-span-6 space-y-3 text-sm">
                <div class="flex justify-between items-center text-slate-500">
                    <span>Cộng tiền sản phẩm:</span>
                    <span class="font-semibold text-slate-700"><ft:currency value="${order.totalAmount}"/></span>
                </div>
                <div class="flex justify-between items-center text-slate-500">
                    <span>Phí giao hàng:</span>
                    <span class="font-semibold text-slate-700"><ft:currency value="${order.deliveryFee}"/></span>
                </div>
                <c:if test="${order.shopDiscountAmount > 0}">
                    <div class="flex justify-between items-center text-primary">
                        <span>Giảm giá từ Shop:</span>
                        <span class="font-bold">-<ft:currency value="${order.shopDiscountAmount}"/></span>
                    </div>
                </c:if>
                <c:if test="${order.systemDiscountAmount > 0}">
                    <div class="flex justify-between items-center text-primary">
                        <span>Giảm giá từ Sàn (MetaFruit):</span>
                        <span class="font-bold">-<ft:currency value="${order.systemDiscountAmount}"/></span>
                    </div>
                </c:if>
                <hr class="border-slate-100">
                <div class="flex justify-between items-center font-bold text-base">
                    <span class="text-slate-800">Tổng cộng thanh toán:</span>
                    <span class="text-xl text-[#ba1a1a] print:text-black font-black"><ft:currency value="${order.finalAmount}"/></span>
                </div>
            </div>
        </div>

        <!-- Chân hóa đơn -->
        <div class="text-center mt-12 pt-6 border-t border-slate-100 text-xs text-slate-400 font-medium">
            Đây là hóa đơn điện tử tự động sinh bởi Hệ thống bán lẻ trái cây METAFRUIT.
        </div>
    </div>
</div>

</body>
</html>
