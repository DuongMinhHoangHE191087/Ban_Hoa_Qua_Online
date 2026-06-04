<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ Cửa hàng | Kênh Người Bán</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

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
        .form-input {
            width: 100%;
            padding: 0.625rem 1rem;
            border: 1px solid #e2ece7;
            border-radius: 0.75rem;
            font-family: 'Lexend', sans-serif;
            font-size: 0.875rem;
            background: #fff;
            transition: border-color 0.2s, box-shadow 0.2s;
            box-sizing: border-box;
        }
        .form-input:focus {
            outline: none;
            border-color: #4d661c;
            box-shadow: 0 0 0 3px rgba(77, 102, 28, 0.08);
        }
    </style>
</head>
<body class="antialiased text-[#0f172a]">
<div class="flex min-h-screen">

    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="profile"/>
    </jsp:include>

    <!-- Main Content -->
    <main class="flex-1 p-6 md:p-8 overflow-y-auto">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Hồ sơ Cửa hàng</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Cập nhật thông tin hiển thị và địa chỉ lấy hàng của cửa hàng bạn.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-white/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-store text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Hồ sơ Shop</span>
            </div>
        </div>

        <!-- Flash / Alert Message -->
        <c:if test="${not empty sessionScope.flashMsg}">
            <div id="flash-alert" class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 shadow-sm text-sm font-semibold
                 ${sessionScope.flashType == 'error' ? 'bg-red-50 border-red-500 text-red-800' : 'bg-emerald-50 border-emerald-500 text-emerald-800'}">
                <i class="fa-solid ${sessionScope.flashType == 'error' ? 'fa-circle-exclamation' : 'fa-circle-check'}"></i>
                <span class="flex-1"><c:out value="${sessionScope.flashMsg}"/></span>
                <button onclick="document.getElementById('flash-alert').remove()" class="opacity-60 hover:opacity-100 transition-opacity">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
            <c:remove var="flashMsg" scope="session"/>
            <c:remove var="flashType" scope="session"/>
        </c:if>

        <!-- Profile Content: Two Columns on large screens -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

            <!-- Left: Shop Avatar / Info Card -->
            <div class="lg:col-span-1">
                <div class="glass-card rounded-2xl p-6 flex flex-col items-center text-center">
                    <!-- Shop Avatar -->
                    <div class="w-24 h-24 rounded-2xl bg-gradient-to-br from-[#4d661c] to-[#84cc16] flex items-center justify-center shadow-lg mb-4">
                        <i class="fa-solid fa-store text-4xl text-white"></i>
                    </div>
                    <h2 class="text-base font-extrabold text-txt">
                        <c:choose>
                            <c:when test="${not empty shopProfile.shopName}"><c:out value="${shopProfile.shopName}"/></c:when>
                            <c:otherwise>Cửa hàng của bạn</c:otherwise>
                        </c:choose>
                    </h2>
                    <p class="text-xs text-txt-3 mt-1 font-medium">Kênh Người Bán · MetaFruit</p>

                    <!-- Shop Stats -->
                    <div class="w-full mt-6 space-y-3">
                        <div class="flex items-center gap-3 p-3 bg-[#f9fdf9] rounded-xl border border-[#e2ece7]">
                            <div class="w-8 h-8 rounded-lg bg-[#edf7f2] text-primary flex items-center justify-center shrink-0">
                                <i class="fa-solid fa-user text-sm"></i>
                            </div>
                            <div class="text-left">
                                <p class="text-[10px] text-txt-3 uppercase tracking-wider font-bold">Chủ cửa hàng</p>
                                <p class="text-xs font-semibold text-txt truncate">
                                    <c:out value="${sessionScope.currentUser.fullName}"/>
                                </p>
                            </div>
                        </div>
                        <div class="flex items-center gap-3 p-3 bg-[#f9fdf9] rounded-xl border border-[#e2ece7]">
                            <div class="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
                                <i class="fa-solid fa-envelope text-sm"></i>
                            </div>
                            <div class="text-left">
                                <p class="text-[10px] text-txt-3 uppercase tracking-wider font-bold">Email</p>
                                <p class="text-xs font-semibold text-txt truncate">
                                    <c:out value="${sessionScope.currentUser.email}"/>
                                </p>
                            </div>
                        </div>
                        <c:if test="${not empty shopProfile.preferredCategories}">
                            <div class="flex items-center gap-3 p-3 bg-[#f9fdf9] rounded-xl border border-[#e2ece7]">
                                <div class="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
                                    <i class="fa-solid fa-tags text-sm"></i>
                                </div>
                                <div class="text-left">
                                    <p class="text-[10px] text-txt-3 uppercase tracking-wider font-bold">Danh mục</p>
                                    <p class="text-xs font-semibold text-txt truncate"><c:out value="${shopProfile.preferredCategories}"/></p>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <!-- Quick nav links -->
                    <div class="w-full mt-6 grid grid-cols-2 gap-2">
                        <a href="${pageContext.request.contextPath}/shop/products"
                           class="flex flex-col items-center gap-1 p-3 rounded-xl border border-[#e2ece7] hover:border-primary/30 hover:bg-[#f4fbf7] transition-all text-xs font-bold text-txt-2 hover:text-primary">
                            <i class="fa-solid fa-box text-lg text-gray-300 group-hover:text-primary"></i>Sản phẩm
                        </a>
                        <a href="${pageContext.request.contextPath}/shop/orders"
                           class="flex flex-col items-center gap-1 p-3 rounded-xl border border-[#e2ece7] hover:border-primary/30 hover:bg-[#f4fbf7] transition-all text-xs font-bold text-txt-2 hover:text-primary">
                            <i class="fa-solid fa-clipboard-list text-lg text-gray-300"></i>Đơn hàng
                        </a>
                    </div>
                </div>
            </div>

            <!-- Right: Edit Form -->
            <div class="lg:col-span-2">
                <div class="glass-card rounded-2xl overflow-hidden">
                    <div class="flex items-center gap-3 p-5 border-b border-[#e2ece7] bg-[#f9fdf9]">
                        <div class="w-9 h-9 rounded-xl bg-[#edf7f2] text-primary flex items-center justify-center">
                            <i class="fa-solid fa-pen-to-square"></i>
                        </div>
                        <h2 class="text-sm font-bold text-txt">Cập nhật hồ sơ cửa hàng</h2>
                    </div>
                    <div class="p-6">
                        <form action="${pageContext.request.contextPath}/shop/profile" method="post" id="profileForm">

                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="shopName">
                                    Tên cửa hàng <span class="text-red-500">*</span>
                                </label>
                                <input type="text" class="form-input" id="shopName" name="shopName"
                                       value="<c:out value='${shopProfile.shopName}'/>" required
                                       placeholder="Nhập tên cửa hàng của bạn...">
                                <p class="text-[10px] text-txt-3 mt-1.5">Tên sẽ hiển thị trên trang sản phẩm và đơn hàng.</p>
                            </div>

                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="shopDescription">
                                    Mô tả cửa hàng
                                </label>
                                <textarea class="form-input" id="shopDescription" name="shopDescription" rows="4"
                                          placeholder="Giới thiệu ngắn gọn về cửa hàng, loại hàng kinh doanh, thế mạnh..."><c:out value="${shopProfile.shopDescription}"/></textarea>
                            </div>

                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="preferredCategories">
                                    Loại trái cây kinh doanh chính
                                </label>
                                <input type="text" class="form-input" id="preferredCategories" name="preferredCategories"
                                       value="<c:out value='${shopProfile.preferredCategories}'/>"
                                       placeholder="VD: Trái cây nhập khẩu, Trái cây hữu cơ...">
                            </div>

                            <div class="mb-6">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="deliveryAddress">
                                    Địa chỉ lấy hàng mặc định
                                </label>
                                <textarea class="form-input" id="deliveryAddress" name="deliveryAddress" rows="3"
                                          placeholder="Số nhà, tên đường, phường, quận, tỉnh/thành phố..."><c:out value="${shopProfile.deliveryAddress}"/></textarea>
                                <p class="text-[10px] text-txt-3 mt-1.5 flex items-center gap-1">
                                    <i class="fa-solid fa-circle-info text-blue-400"></i>
                                    Địa chỉ này sẽ được cung cấp cho nhân viên giao hàng đến lấy hàng.
                                </p>
                            </div>

                            <div class="flex gap-3">
                                <button type="submit" id="saveBtn"
                                        class="flex items-center gap-2 px-6 py-2.5 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-xl transition-all duration-200 shadow-sm">
                                    <i class="fa-solid fa-floppy-disk"></i>
                                    Lưu hồ sơ cửa hàng
                                </button>
                                <button type="reset"
                                        class="px-6 py-2.5 border border-border text-xs font-bold text-txt-2 hover:bg-gray-50 rounded-xl transition-colors">
                                    <i class="fa-solid fa-rotate-left mr-1"></i>Đặt lại
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

        </div>
    </main>
</div>

<script>
    // Simple save animation
    document.getElementById('profileForm').addEventListener('submit', function() {
        const btn = document.getElementById('saveBtn');
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
        btn.disabled = true;
    });
</script>
</body>
</html>
