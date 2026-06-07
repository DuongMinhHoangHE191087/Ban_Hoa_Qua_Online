<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- footer.jsp — Premium Tailwind-based footer. Đóng </main> và </body>. --%>
    </main><%-- end .main-content --%>

    <%-- ===================== PREMIUM FOOTER ===================== --%>
    <footer class="relative bg-gradient-to-br from-[#0d2b1a] via-[#14532D] to-[#0f3d24] text-white overflow-hidden mt-0">

        <%-- Decorative overlay patterns --%>
        <div class="absolute inset-0 pointer-events-none">
            <div class="absolute top-0 left-0 w-[500px] h-[500px] bg-emerald-400/5 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2"></div>
            <div class="absolute bottom-0 right-0 w-[400px] h-[400px] bg-lime-400/5 rounded-full blur-3xl translate-x-1/2 translate-y-1/2"></div>
            <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.03),transparent)]"></div>
        </div>

        <div class="relative z-10 max-w-7xl mx-auto px-6 pt-14 pb-6">

            <%-- Top Grid --%>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10 pb-12 border-b border-white/10">

                <%-- Column 1: Brand --%>
                <div class="space-y-5 lg:col-span-1">
                    <a href="${pageContext.request.contextPath}/home" class="flex items-center gap-3 group">
                        <img src="${pageContext.request.contextPath}/assets/images/logo.png"
                             alt="MetaFruit"
                             class="h-10 w-10 rounded-xl object-cover ring-2 ring-white/20 group-hover:ring-emerald-400/60 transition-all duration-300">
                        <div class="leading-tight">
                            <span class="text-xl font-extrabold tracking-tight text-white">Meta</span><span class="text-xl font-extrabold tracking-tight text-emerald-400">Fruit</span>
                            <div class="text-[10px] text-emerald-300/70 font-light tracking-widest uppercase">Premium Organic</div>
                        </div>
                    </a>
                    <p class="text-sm text-emerald-100/70 font-light leading-relaxed">
                        Sàn thương mại điện tử chuyên cung cấp trái cây sạch, hữu cơ và đặc sản vùng miền chất lượng cao hàng đầu Việt Nam.
                    </p>

                    <%-- Social links --%>
                    <div class="flex items-center gap-3 pt-1">
                        <a href="#" title="Facebook"
                           class="w-9 h-9 rounded-xl bg-white/10 border border-white/10 hover:bg-emerald-500 hover:border-emerald-500 flex items-center justify-center transition-all duration-300 hover:scale-110 hover:shadow-lg hover:shadow-emerald-500/20">
                            <i class="fa-brands fa-facebook-f text-sm"></i>
                        </a>
                        <a href="#" title="Instagram"
                           class="w-9 h-9 rounded-xl bg-white/10 border border-white/10 hover:bg-pink-500 hover:border-pink-500 flex items-center justify-center transition-all duration-300 hover:scale-110 hover:shadow-lg hover:shadow-pink-500/20">
                            <i class="fa-brands fa-instagram text-sm"></i>
                        </a>
                        <a href="#" title="YouTube"
                           class="w-9 h-9 rounded-xl bg-white/10 border border-white/10 hover:bg-red-600 hover:border-red-600 flex items-center justify-center transition-all duration-300 hover:scale-110 hover:shadow-lg hover:shadow-red-500/20">
                            <i class="fa-brands fa-youtube text-sm"></i>
                        </a>
                        <a href="#" title="TikTok"
                           class="w-9 h-9 rounded-xl bg-white/10 border border-white/10 hover:bg-slate-800 hover:border-slate-600 flex items-center justify-center transition-all duration-300 hover:scale-110">
                            <i class="fa-brands fa-tiktok text-sm"></i>
                        </a>
                    </div>
                </div>

                <%-- Column 2: Khám phá --%>
                <div class="space-y-5">
                    <h4 class="text-sm font-bold text-white uppercase tracking-widest flex items-center gap-2">
                        <span class="w-5 h-[2px] bg-emerald-400 rounded-full inline-block"></span>
                        Khám phá
                    </h4>
                    <ul class="space-y-3">
                        <li>
                            <a href="${pageContext.request.contextPath}/home"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Tất cả sản phẩm
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=import"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Trái cây nhập khẩu
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=local"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Đặc sản Việt Nam
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/about"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Giới thiệu về chúng tôi
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/auth/register?accountType=SHOP_OWNER"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Đăng ký mở cửa hàng
                            </a>
                        </li>
                    </ul>
                </div>

                <%-- Column 3: Chính sách --%>
                <div class="space-y-5">
                    <h4 class="text-sm font-bold text-white uppercase tracking-widest flex items-center gap-2">
                        <span class="w-5 h-[2px] bg-emerald-400 rounded-full inline-block"></span>
                        Chính sách
                    </h4>
                    <ul class="space-y-3">
                        <li>
                            <a href="${pageContext.request.contextPath}/home"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Chính sách bảo mật
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/home"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Đổi trả &amp; Hoàn tiền
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/home"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Quy trình giao nhận
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/home"
                               class="group flex items-center gap-2 text-sm text-emerald-100/70 hover:text-white transition-colors duration-200">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400/50 group-hover:bg-emerald-400 transition-colors shrink-0"></span>
                                Hướng dẫn Shop &amp; Đối tác
                            </a>
                        </li>
                    </ul>
                </div>

                <%-- Column 4: Contact & Payment --%>
                <div class="space-y-5">
                    <h4 class="text-sm font-bold text-white uppercase tracking-widest flex items-center gap-2">
                        <span class="w-5 h-[2px] bg-emerald-400 rounded-full inline-block"></span>
                        Liên hệ
                    </h4>
                    <ul class="space-y-3.5 text-sm text-emerald-100/70">
                        <li class="flex items-start gap-3">
                            <i class="fa-solid fa-location-dot text-emerald-400 mt-0.5 shrink-0 text-[13px]"></i>
                            <span class="font-light leading-relaxed">Tầng 12, Toà nhà FPT, Khu CNC Hoà Lạc, Hà Nội</span>
                        </li>
                        <li class="flex items-center gap-3">
                            <i class="fa-solid fa-phone text-emerald-400 shrink-0 text-[13px]"></i>
                            <span class="font-light">Hotline: <span class="font-semibold text-white">1900 8198</span> (8:00–22:00)</span>
                        </li>
                        <li class="flex items-center gap-3">
                            <i class="fa-solid fa-envelope text-emerald-400 shrink-0 text-[13px]"></i>
                            <span class="font-light">support@metafruit.com</span>
                        </li>
                    </ul>

                    <%-- Payment Methods --%>
                    <div class="pt-1">
                        <p class="text-[11px] font-semibold text-emerald-300/60 uppercase tracking-wider mb-3">Thanh toán an toàn</p>
                        <div class="flex flex-wrap gap-2">
                            <span class="inline-flex items-center gap-1.5 bg-white/10 border border-white/10 hover:bg-white/15 text-white text-[11px] font-semibold px-3 py-1.5 rounded-lg transition-colors cursor-default">
                                <i class="fa-solid fa-qrcode text-emerald-400 text-[11px]"></i> VietQR
                            </span>
                            <span class="inline-flex items-center gap-1.5 bg-white/10 border border-white/10 hover:bg-white/15 text-white text-[11px] font-semibold px-3 py-1.5 rounded-lg transition-colors cursor-default">
                                <i class="fa-solid fa-shield-halved text-emerald-400 text-[11px]"></i> SePay
                            </span>
                            <span class="inline-flex items-center gap-1.5 bg-white/10 border border-white/10 hover:bg-white/15 text-white text-[11px] font-semibold px-3 py-1.5 rounded-lg transition-colors cursor-default">
                                <i class="fa-solid fa-truck-fast text-emerald-400 text-[11px]"></i> COD
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <%-- Bottom Bar --%>
            <div class="pt-6 flex flex-col sm:flex-row justify-between items-center gap-3 text-xs text-emerald-100/50">
                <p>&copy; 2026 <span class="font-semibold text-emerald-300/80">MetaFruit Premium</span>. Tất cả các quyền được bảo lưu.</p>
                <p class="flex items-center gap-1.5">
                    <i class="fa-solid fa-circle-check text-emerald-400 text-[11px]"></i>
                    Đã thông báo Bộ Công Thương &mdash; <span class="text-emerald-400 font-semibold ml-1">Group 1 SE2017 JS (IT)</span>
                </p>
            </div>
        </div>
    </footer>

    <%-- JS chính --%>
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <%-- Thêm JS trang cụ thể nếu cần --%>
</body>
</html>
