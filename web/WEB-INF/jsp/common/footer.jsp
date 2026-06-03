<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- footer.jsp — Đóng <main> và <body>, load JS cuối trang. --%>
    </main><%-- end .main-content --%>

    <footer class="site-footer">
        <div class="container footer__grid">
            <!-- Column 1: Brand & Organic Values -->
            <div class="footer__col footer__col-brand">
                <a href="${pageContext.request.contextPath}/home" class="footer__logo">
                    <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" style="height: 38px; width: 38px; border-radius: 8px; object-fit: cover;">
                    <span class="logo-text">Meta<span class="text-highlight">Fruit</span></span>
                </a>
                <p class="footer__description">
                    MetaFruit - Sàn thương mại điện tử chuyên cung cấp trái cây sạch, hữu cơ và đặc sản vùng miền chất lượng cao hàng đầu Việt Nam. Tươi ngon, an toàn từ trang trại tới bàn ăn của gia đình bạn.
                </p>
                <div class="footer__socials">
                    <a href="#" class="social-link" title="Facebook"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="#" class="social-link" title="Instagram"><i class="fa-brands fa-instagram"></i></a>
                    <a href="#" class="social-link" title="YouTube"><i class="fa-brands fa-youtube"></i></a>
                    <a href="#" class="social-link" title="TikTok"><i class="fa-brands fa-tiktok"></i></a>
                </div>
            </div>

            <!-- Column 2: Quick Links -->
            <div class="footer__col">
                <h4 class="footer__title">Khám phá</h4>
                <ul class="footer__links">
                    <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-chevron-right"></i> Tất cả sản phẩm</a></li>
                    <li><a href="${pageContext.request.contextPath}/home?category=import"><i class="fa-solid fa-chevron-right"></i> Trái cây nhập khẩu</a></li>
                    <li><a href="${pageContext.request.contextPath}/home?category=local"><i class="fa-solid fa-chevron-right"></i> Đặc sản Việt Nam</a></li>
                    <li><a href="${pageContext.request.contextPath}/home?tag=flashsale"><i class="fa-solid fa-chevron-right"></i> Khuyến mãi Hot</a></li>
                    <li><a href="${pageContext.request.contextPath}/auth/register?accountType=SHOP_OWNER"><i class="fa-solid fa-chevron-right"></i> Đăng ký mở cửa hàng</a></li>
                </ul>
            </div>

            <!-- Column 3: Customer Policy -->
            <div class="footer__col">
                <h4 class="footer__title">Chính sách & Hỗ trợ</h4>
                <ul class="footer__links">
                    <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-chevron-right"></i> Chính sách bảo mật</a></li>
                    <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-chevron-right"></i> Quy định đổi trả & hoàn tiền</a></li>
                    <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-chevron-right"></i> Quy trình giao nhận hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-chevron-right"></i> Hướng dẫn cho Shop & Đối tác</a></li>
                </ul>
            </div>

            <!-- Column 4: Contact & Secure Payments -->
            <div class="footer__col footer__col-contact">
                <h4 class="footer__title">Liên hệ & Thanh toán</h4>
                <p class="footer__contact-info">
                    <i class="fa-solid fa-location-dot"></i> Tầng 12, Toà nhà FPT, Khu công nghệ cao Hoà Lạc, Hà Nội
                </p>
                <p class="footer__contact-info">
                    <i class="fa-solid fa-phone"></i> Hotline: 1900 8198 (8:00 - 22:00)
                </p>
                <p class="footer__contact-info">
                    <i class="fa-solid fa-envelope"></i> Email: support@metafruit.com
                </p>
                
                <div class="footer__payments">
                    <span class="payment-title">Thanh toán an toàn qua SePay & VietQR:</span>
                    <div class="payment-badges">
                        <span class="payment-badge vietqr" title="Thanh toán VietQR"><i class="fa-solid fa-qrcode"></i> VietQR</span>
                        <span class="payment-badge sepay" title="Tích hợp SePay tự động"><i class="fa-solid fa-shield-halved"></i> SePay Auto</span>
                        <span class="payment-badge cod" title="Giao hàng thu tiền COD"><i class="fa-solid fa-truck-fast"></i> COD</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="footer__bottom">
            <div class="container footer__bottom-inner">
                <p class="copyright">&copy; 2026 MetaFruit Premium. Tất cả các quyền được bảo lưu. Thiết kế bởi Antigravity.</p>
                <p class="compliance"><i class="fa-solid fa-circle-check"></i> Đã thông báo Bộ Công Thương</p>
            </div>
        </div>
    </footer>

    <%-- JS chính --%>
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <%-- Thêm JS trang cụ thể nếu cần --%>
</body>
</html>
