<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%-- SweetAlert2: offline local asset --%>
            <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
            <script>
                /* Global native-alert override → SweetAlert2 premium */
                window.alert = function (msg) {
                    Swal.fire({
                        icon: 'info', title: 'Thông báo', text: msg,
                        confirmButtonText: 'Đồng ý', confirmButtonColor: '#4D661C',
                        background: '#fff', borderRadius: '12px'
                    });
                };
            </script>


            <aside id="admin-sidebar">
                <%-- Logo --%>
                    <div class="sb-logo">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="brand-lockup">
                            <img src="${pageContext.request.contextPath}/assets/images/logo_light.png" alt="MetaFruit"
                                class="brand-mark">
                            <div class="sb-logo-text">Meta<span>Fruit</span></div>
                        </a>
                    </div>

                    <%-- Navigation --%>
                        <nav class="sb-nav">
                            <div class="sb-section-label">Quản trị hệ thống</div>
                            <ul class="sb-nav-list">
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/dashboard"
                                        class="sb-nav-link ${param.activeMenu == 'dashboard' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-chart-pie"></i></span>
                                        <span>Tổng quan</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/users"
                                        class="sb-nav-link ${param.activeMenu == 'users' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-users"></i></span>
                                        <span>Quản lý người dùng</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/chat"
                                        class="sb-nav-link ${param.activeMenu == 'chat' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-comments"></i></span>
                                        <span>Chat Hỗ trợ</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/reports"
                                        class="sb-nav-link ${param.activeMenu == 'reports' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-chart-column"></i></span>
                                        <span>Báo cáo thống kê</span>
                                    </a>
                                </li>
                            </ul>

                            <div class="sb-section-label">Thương mại</div>
                            <ul class="sb-nav-list">
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/shops"
                                        class="sb-nav-link ${param.activeMenu == 'shops' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-store-slash"></i></span>
                                        <span>Phê duyệt Cửa hàng</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/shops/manage"
                                        class="sb-nav-link ${param.activeMenu == 'manage-shops' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-store"></i></span>
                                        <span>Quản lý Cửa hàng</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/products"
                                        class="sb-nav-link ${param.activeMenu == 'admin-products' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-clipboard-check"></i></span>
                                        <span>Phê duyệt Sản phẩm</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/categories"
                                        class="sb-nav-link ${param.activeMenu == 'categories' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-tags"></i></span>
                                        <span>Danh mục Sản phẩm</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/orders"
                                        class="sb-nav-link ${param.activeMenu == 'orders' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-box-open"></i></span>
                                        <span>Giám sát Đơn hàng</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/promotions"
                                        class="sb-nav-link ${param.activeMenu == 'promotions' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-tags"></i></span>
                                        <span>Voucher sàn</span>
                                    </a>
                                </li>
                            </ul>

                            <div class="sb-section-label">Tài chính</div>
                            <ul class="sb-nav-list">
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/settlements"
                                        class="sb-nav-link ${param.activeMenu == 'settlements' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-file-invoice-dollar"></i></span>
                                        <span>Đối soát Thanh toán</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/payments"
                                        class="sb-nav-link ${param.activeMenu == 'payments' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-credit-card"></i></span>
                                        <span>Giám sát Thanh toán</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/refunds"
                                        class="sb-nav-link ${param.activeMenu == 'returns' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-rotate-left"></i></span>
                                        <span>Yêu cầu Đổi trả</span>
                                    </a>
                                </li>
                            </ul>

                            <div class="sb-section-label">Nội dung</div>
                            <ul class="sb-nav-list">
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/reviews"
                                        class="sb-nav-link ${param.activeMenu == 'reviews' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-star"></i></span>
                                        <span>Kiểm duyệt Đánh giá</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/notifications"
                                        class="sb-nav-link ${param.activeMenu == 'notifications' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-bell"></i></span>
                                        <span>Gửi Thông báo</span>
                                    </a>
                                </li>
                            </ul>
                            <div class="sb-section-label">Hệ thống</div>
                            <ul class="sb-nav-list">
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/config"
                                        class="sb-nav-link ${param.activeMenu == 'config' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-cogs"></i></span>
                                        <span>Cấu hình Hệ thống</span>
                                    </a>
                                </li>
                                <li class="sb-nav-item">
                                    <a href="${pageContext.request.contextPath}/admin/profile"
                                        class="sb-nav-link ${param.activeMenu == 'profile' ? 'active' : ''}">
                                        <span class="sb-icon"><i class="fa-solid fa-user-shield"></i></span>
                                        <span>Hồ sơ Cá nhân</span>
                                    </a>
                                </li>
                            </ul>
                        </nav>

                        <%-- Footer --%>
                            <div class="sb-footer">
                                <div class="flex items-center gap-2 px-2 py-1 mb-1">
                                    <div
                                        class="w-7 h-7 rounded-full bg-[#edf7f2] text-[#4d661c] flex items-center justify-center text-xs font-bold shrink-0">
                                        <i class="fa-solid fa-user-shield"></i>
                                    </div>
                                    <div class="flex-1 min-w-0">
                                        <p class="m-0 text-xs font-bold text-[#0f172a] truncate">
                                            <c:out value="${sessionScope.currentUser.fullName}" />
                                        </p>
                                        <p class="m-0 text-[10px] text-gray-400 truncate">Quản trị viên</p>
                                    </div>
                                </div>
                                <a href="${pageContext.request.contextPath}/" class="sb-footer-btn sb-btn-home">
                                    <i class="fa-solid fa-house"></i> Về trang chủ
                                </a>

                                <a href="${pageContext.request.contextPath}/auth/logout"
                                    class="sb-footer-btn sb-btn-logout">
                                    <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                                </a>
                            </div>
            </aside>

            <script>
                document.addEventListener("DOMContentLoaded", function () {
                    if (window.innerWidth <= 1024) {
                        const btn = document.createElement('button');
                        btn.type = 'button';
                        btn.id = 'sidebarMobileToggle';
                        btn.className = 'sidebar-mobile-toggle';
                        btn.setAttribute('aria-label', 'Thu gọn hoặc mở sidebar');
                        btn.title = 'Thu gọn hoặc mở sidebar';
                        btn.innerHTML = '<i class="fa-solid fa-angles-right"></i>';

                        const sidebar = document.getElementById('admin-sidebar');
                        if (sidebar) {
                            const syncTogglePosition = function () {
                                if (sidebar.classList.contains('active')) {
                                    const sidebarWidth = sidebar.getBoundingClientRect().width || 270;
                                    const maxLeft = Math.max(20, window.innerWidth - 64);
                                    btn.style.left = Math.min(sidebarWidth, maxLeft) + 'px';
                                } else {
                                    btn.style.left = '20px';
                                }
                            };

                            btn.addEventListener('click', function (e) {
                                e.stopPropagation();
                                sidebar.classList.toggle('active');
                                const icon = btn.querySelector('i');
                                if (sidebar.classList.contains('active')) {
                                    icon.className = 'fa-solid fa-angles-left';
                                } else {
                                    icon.className = 'fa-solid fa-angles-right';
                                }
                                syncTogglePosition();
                            });

                            document.addEventListener('click', function (ev) {
                                if (sidebar.classList.contains('active') && !sidebar.contains(ev.target) && ev.target !== btn) {
                                    sidebar.classList.remove('active');
                                    const icon = btn.querySelector('i');
                                    if (icon) icon.className = 'fa-solid fa-angles-right';
                                    syncTogglePosition();
                                }
                            });

                            window.addEventListener('resize', syncTogglePosition);
                            syncTogglePosition();
                            document.body.appendChild(btn);
                        }
                    }
                });
            </script>
            <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
