<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>

<%-- Load header — Tailwind + global config already included by header.jsp --%>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Giới thiệu về chúng tôi" />
</jsp:include>

<%-- Page-specific styles (no Tailwind re-import — header.jsp already loaded it) --%>
<style>
    /* ── Glassmorphism card ── */
    .glass-card {
        background: rgba(255, 255, 255, 0.72);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.55);
    }

    /* ── Hover lift animation ── */
    .hover-lift {
        transition: transform 0.32s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease;
    }
    .hover-lift:hover {
        transform: translateY(-8px);
        box-shadow: 0 24px 48px rgba(77, 102, 28, 0.1);
    }

    /* ── FIXED text gradient ── */
    .text-gradient {
        background: linear-gradient(135deg, #4d661c 0%, #31694b 100%);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;
        color: transparent;
        display: inline;
    }

    /* ── Floating blob animations ── */
    .blob-1 { animation: blob-float-1 14s ease-in-out infinite; }
    .blob-2 { animation: blob-float-2 18s ease-in-out infinite; }
    .blob-3 { animation: blob-float-3 11s ease-in-out infinite; }

    @keyframes blob-float-1 {
        0%, 100% { transform: translateY(0) scale(1); }
        50% { transform: translateY(-24px) scale(1.08); }
    }
    @keyframes blob-float-2 {
        0%, 100% { transform: translateY(0) scale(1.05); }
        50% { transform: translateY(18px) scale(0.92); }
    }
    @keyframes blob-float-3 {
        0%, 100% { transform: translateX(0) scale(1); }
        50% { transform: translateX(-16px) scale(1.06); }
    }

    /* ── Timeline connector ── */
    .step-connector::after {
        content: '';
        position: absolute;
        left: 20px;
        top: 48px;
        bottom: -16px;
        width: 2px;
        background: linear-gradient(to bottom, #4d661c33, transparent);
    }

    /* ── Contact input focus ── */
    .contact-input:focus {
        outline: none;
        border-color: #4d661c;
        box-shadow: 0 0 0 3px rgba(77, 102, 28, 0.15);
    }

    /* ── Section divider ── */
    .section-divider {
        background: linear-gradient(90deg, transparent, rgba(77, 102, 28, 0.15), transparent);
    }
</style>

<%-- ====================================================================
     MAIN PAGE WRAPPER
     ==================================================================== --%>
<div class="bg-gradient-to-br from-[#eaffea] via-white to-[#d1ffd8]/40 min-h-screen text-[#00210d] antialiased pb-0 relative overflow-hidden">

    <%-- Decorative floating blobs --%>
    <div aria-hidden="true" class="absolute top-16 -left-24 w-96 h-96 bg-emerald-200/20 rounded-full blur-3xl pointer-events-none blob-1"></div>
    <div aria-hidden="true" class="absolute bottom-1/3 -right-20 w-[480px] h-[480px] bg-lime-200/15 rounded-full blur-3xl pointer-events-none blob-2"></div>
    <div aria-hidden="true" class="absolute top-1/2 left-1/2 -translate-x-1/2 w-64 h-64 bg-emerald-100/30 rounded-full blur-2xl pointer-events-none blob-3"></div>

    <%-- ── BREADCRUMB ── --%>
    <nav aria-label="Breadcrumb" class="max-w-7xl mx-auto px-6 pt-24 pb-4 flex items-center gap-1.5 text-xs text-[#44483b]/70 font-medium">
        <a href="${pageContext.request.contextPath}/home"
           class="hover:text-[#4d661c] transition-colors flex items-center gap-1">
            <span class="material-symbols-outlined text-[16px]">home</span>
            Trang chủ
        </a>
        <span class="material-symbols-outlined text-[14px]">chevron_right</span>
        <span class="text-[#4d661c] font-semibold">Giới thiệu về MetaFruit</span>
    </nav>

    <%-- ================================================================
         HERO SECTION
         ================================================================ --%>
    <section class="max-w-7xl mx-auto px-6 pt-6 pb-20 grid grid-cols-1 lg:grid-cols-12 gap-14 items-center relative z-10">

        <%-- Left: text --%>
        <div class="lg:col-span-7 space-y-7">
            <div class="inline-flex items-center gap-2 bg-emerald-100 border border-emerald-200/60 px-4 py-1.5 rounded-full text-xs font-semibold text-[#4d661c] shadow-sm">
                <span class="material-symbols-outlined text-[15px]">eco</span>
                Kiến tạo kỷ nguyên Nông sản Sạch Việt Nam
            </div>

            <h1 class="text-4xl md:text-5xl lg:text-6xl font-extrabold tracking-tight leading-tight">
                Về chúng tôi &amp;<br/>
                <span class="text-gradient">Hành trình mang<br/>Trái cây Sạch tới mọi nhà</span>
            </h1>

            <p class="text-base md:text-lg text-[#44483b] font-light leading-relaxed max-w-xl">
                MetaFruit ra đời với sứ mệnh thu hẹp khoảng cách giữa các hợp tác xã nông sản đạt chuẩn hữu cơ/VietGAP của Việt Nam và hàng triệu người tiêu dùng. Chúng tôi cam kết mang đến trái cây tươi ngon nhất, chín tự nhiên và an toàn tuyệt đối.
            </p>

            <%-- Stats row --%>
            <div class="grid grid-cols-3 gap-5 pt-4 border-t border-[#4d661c]/10 max-w-sm">
                <div>
                    <h3 class="text-3xl font-extrabold text-[#4d661c]">100%</h3>
                    <p class="text-[11px] text-[#44483b]/80 font-medium mt-0.5">Đạt chuẩn VietGAP / Organic</p>
                </div>
                <div>
                    <h3 class="text-3xl font-extrabold text-[#4d661c]">50+</h3>
                    <p class="text-[11px] text-[#44483b]/80 font-medium mt-0.5">Trang trại liên kết</p>
                </div>
                <div>
                    <h3 class="text-3xl font-extrabold text-[#4d661c]">2H</h3>
                    <p class="text-[11px] text-[#44483b]/80 font-medium mt-0.5">Giao hàng hoả tốc nội thành</p>
                </div>
            </div>

            <%-- CTA buttons --%>
            <div class="flex flex-wrap gap-3 pt-2">
                <a href="${pageContext.request.contextPath}/products"
                   class="inline-flex items-center gap-2 bg-[#4d661c] hover:bg-[#364e03] text-white text-sm font-bold px-6 py-3 rounded-full transition-all shadow-lg shadow-[#4d661c]/25 hover:shadow-xl hover:-translate-y-0.5">
                    <span class="material-symbols-outlined text-[18px]">shopping_basket</span>
                    Mua sắm ngay
                </a>
                <a href="#contact"
                   class="inline-flex items-center gap-2 border border-[#4d661c] text-[#4d661c] hover:bg-emerald-50 text-sm font-bold px-6 py-3 rounded-full transition-all">
                    <span class="material-symbols-outlined text-[18px]">mail</span>
                    Liên hệ với chúng tôi
                </a>
            </div>
        </div>

        <%-- Right: Premium Card --%>
        <div class="lg:col-span-5 flex items-center justify-center relative">
            <div class="w-full max-w-[380px] bg-gradient-to-br from-emerald-800 to-[#4d661c] rounded-[32px] p-6 text-white flex flex-col justify-between shadow-2xl relative overflow-hidden group min-h-[440px]">
                <%-- Inner radial overlay --%>
                <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.12),transparent)] pointer-events-none rounded-[32px]"></div>
                <div class="absolute inset-0 bg-[radial-gradient(circle_at_bottom_left,rgba(132,204,22,0.08),transparent)] pointer-events-none rounded-[32px]"></div>

                <%-- Card header --%>
                <div class="flex justify-between items-start z-10">
                    <div class="bg-white/15 backdrop-blur-md border border-white/20 p-2.5 rounded-2xl">
                        <span class="material-symbols-outlined text-[28px]">spa</span>
                    </div>
                    <span class="text-[10px] font-bold bg-white/20 px-3 py-1.5 rounded-full uppercase tracking-widest">MetaFruit Premium</span>
                </div>

                <%-- Decorative fruit icon instead of broken image --%>
                <div class="flex justify-center items-center py-8 z-10">
                    <div class="w-44 h-44 rounded-full bg-white/15 border-4 border-white/25 flex flex-col items-center justify-center shadow-xl group-hover:scale-105 transition-transform duration-500 backdrop-blur-sm">
                        <span class="material-symbols-outlined text-[72px] text-white/90">local_florist</span>
                        <span class="text-xs font-bold text-white/80 mt-1 tracking-wider">ORGANIC</span>
                    </div>
                </div>

                <%-- Card footer --%>
                <div class="space-y-2 z-10">
                    <h4 class="text-lg font-bold">100% Nông sản chính chủ từ người nông dân</h4>
                    <p class="text-xs text-emerald-100/80 font-light leading-relaxed">Mỗi sản phẩm bán ra đều trực tiếp hỗ trợ kinh tế địa phương và thúc đẩy nông nghiệp bền vững.</p>
                </div>
            </div>

            <%-- Floating review badge --%>
            <div class="absolute -bottom-4 -left-4 glass-card p-4 rounded-2xl shadow-xl flex items-center gap-3 max-w-[210px] hover:scale-105 transition-transform duration-300">
                <div class="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center text-amber-500 shrink-0">
                    <span class="material-symbols-outlined font-bold text-[22px]">star</span>
                </div>
                <div>
                    <h4 class="text-sm font-bold text-[#00210d]">4.9 / 5.0</h4>
                    <p class="text-[10px] text-[#44483b] font-light">Hài lòng từ 10k+ khách hàng</p>
                </div>
            </div>
        </div>
    </section>

    <div class="section-divider h-px max-w-5xl mx-auto mb-4"></div>

    <%-- ================================================================
         CORE VALUES
         ================================================================ --%>
    <section class="max-w-7xl mx-auto px-6 py-16 relative z-10">
        <div class="text-center max-w-2xl mx-auto mb-12 space-y-3">
            <div class="inline-flex items-center gap-2 bg-[#4d661c]/10 border border-[#4d661c]/15 px-4 py-1.5 rounded-full text-xs font-semibold text-[#4d661c]">
                <span class="material-symbols-outlined text-[15px]">verified</span>
                Giá trị cốt lõi
            </div>
            <h2 class="text-3xl font-extrabold tracking-tight">Giá trị làm nên thương hiệu</h2>
            <p class="text-sm text-[#44483b] font-light">Chúng tôi không chỉ kinh doanh nông sản — chúng tôi lan tỏa lối sống khỏe mạnh và có trách nhiệm.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="glass-card p-6 rounded-3xl hover-lift space-y-4 border border-emerald-100/50">
                <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-2xl flex items-center justify-center">
                    <span class="material-symbols-outlined text-[26px]">verified_user</span>
                </div>
                <h3 class="text-base font-bold">Chất lượng tuyển chọn</h3>
                <p class="text-xs text-[#44483b] font-light leading-relaxed">Trái cây được giám sát từ giai đoạn gieo trồng, đạt tiêu chuẩn khắt khe về tồn dư hóa chất bảo vệ thực vật trước khi thu hái.</p>
            </div>
            <div class="glass-card p-6 rounded-3xl hover-lift space-y-4 border border-lime-100/50">
                <div class="w-12 h-12 bg-lime-100 text-lime-700 rounded-2xl flex items-center justify-center">
                    <span class="material-symbols-outlined text-[26px]">local_shipping</span>
                </div>
                <h3 class="text-base font-bold">Bảo quản lạnh chuỗi cung ứng</h3>
                <p class="text-xs text-[#44483b] font-light leading-relaxed">Hệ thống kho mát và vận chuyển khép kín từ nông trại đảm bảo trái cây giữ trọn dưỡng chất và độ tươi ngon.</p>
            </div>
            <div class="glass-card p-6 rounded-3xl hover-lift space-y-4 border border-amber-100/50">
                <div class="w-12 h-12 bg-amber-100 text-amber-700 rounded-2xl flex items-center justify-center">
                    <span class="material-symbols-outlined text-[26px]">handshake</span>
                </div>
                <h3 class="text-base font-bold">Hỗ trợ nông dân Việt</h3>
                <p class="text-xs text-[#44483b] font-light leading-relaxed">Hợp tác trực tiếp, cam kết thu mua với mức giá công bằng, loại bỏ trung gian để tối ưu hóa thu nhập của bà con nông dân.</p>
            </div>
            <div class="glass-card p-6 rounded-3xl hover-lift space-y-4 border border-teal-100/50">
                <div class="w-12 h-12 bg-teal-100 text-teal-700 rounded-2xl flex items-center justify-center">
                    <span class="material-symbols-outlined text-[26px]">package_2</span>
                </div>
                <h3 class="text-base font-bold">Bao bì thân thiện</h3>
                <p class="text-xs text-[#44483b] font-light leading-relaxed">Khuyến khích dùng khay giấy tái chế, túi sinh học tự hủy, hạn chế rác thải nhựa và gìn giữ màu xanh Trái Đất.</p>
            </div>
        </div>
    </section>

    <div class="section-divider h-px max-w-5xl mx-auto mb-4"></div>

    <%-- ================================================================
         ECOSYSTEM SECTION
         ================================================================ --%>
    <section class="max-w-7xl mx-auto px-6 py-16 relative z-10">
        <div class="bg-white/50 border border-white/60 backdrop-blur-md rounded-[36px] p-8 md:p-12">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">

                <div class="lg:col-span-5 space-y-6">
                    <div class="inline-flex items-center gap-2 bg-lime-100 border border-lime-200/50 px-3 py-1 rounded-full text-xs font-semibold text-lime-800">
                        <span class="material-symbols-outlined text-[15px]">settings_accessibility</span>
                        Quy trình vận hành 3 bên
                    </div>
                    <h2 class="text-3xl font-extrabold tracking-tight">Hệ sinh thái Nông sản số thông minh</h2>
                    <p class="text-sm text-[#44483b] font-light leading-relaxed">MetaFruit xây dựng nền tảng hiện đại kết nối trực tiếp ba tác nhân cốt lõi trong chuỗi giá trị nông sản:</p>

                    <div class="space-y-5">
                        <div class="flex gap-4 relative step-connector pb-4">
                            <div class="w-10 h-10 rounded-full bg-[#4d661c]/10 border-2 border-[#4d661c]/20 flex items-center justify-center text-[#4d661c] font-bold text-sm shrink-0">1</div>
                            <div>
                                <h4 class="text-sm font-bold">Nhà vườn &amp; Chủ shop</h4>
                                <p class="text-xs text-[#44483b] font-light mt-0.5">Tạo gian hàng số nhanh chóng, tiếp cận hàng triệu khách hàng và kiểm soát tồn kho thông minh.</p>
                            </div>
                        </div>
                        <div class="flex gap-4 relative step-connector pb-4">
                            <div class="w-10 h-10 rounded-full bg-amber-100 border-2 border-amber-200 flex items-center justify-center text-amber-700 font-bold text-sm shrink-0">2</div>
                            <div>
                                <h4 class="text-sm font-bold">Đội ngũ Giao hàng (Delivery)</h4>
                                <p class="text-xs text-[#44483b] font-light mt-0.5">Tài xế tự do được phân bổ đơn hàng tự động dựa trên vị trí địa lý, tối ưu thời gian giao hàng.</p>
                            </div>
                        </div>
                        <div class="flex gap-4">
                            <div class="w-10 h-10 rounded-full bg-lime-100 border-2 border-lime-200 flex items-center justify-center text-lime-700 font-bold text-sm shrink-0">3</div>
                            <div>
                                <h4 class="text-sm font-bold">Người tiêu dùng</h4>
                                <p class="text-xs text-[#44483b] font-light mt-0.5">Mua hoa quả sạch chính gốc, thanh toán bảo mật VietQR và phản hồi chất lượng minh bạch.</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="lg:col-span-7 grid grid-cols-1 sm:grid-cols-3 gap-5">
                    <div class="glass-card p-5 rounded-2xl flex flex-col items-center text-center space-y-3 hover:border-[#4d661c]/30 transition-colors border border-transparent">
                        <div class="w-14 h-14 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center">
                            <span class="material-symbols-outlined text-[30px]">storefront</span>
                        </div>
                        <div>
                            <h4 class="font-bold text-sm">Chủ vườn / Shop</h4>
                            <p class="text-[11px] text-[#44483b] font-light mt-1">Đăng bán hoa quả tươi sống, nông sản chính hãng theo mùa.</p>
                        </div>
                    </div>
                    <div class="glass-card p-5 rounded-2xl flex flex-col items-center text-center space-y-3 hover:border-amber-300/40 transition-colors border border-transparent">
                        <div class="w-14 h-14 bg-amber-100 text-amber-700 rounded-full flex items-center justify-center">
                            <span class="material-symbols-outlined text-[30px]">motorcycle</span>
                        </div>
                        <div>
                            <h4 class="font-bold text-sm">Delivery Rider</h4>
                            <p class="text-[11px] text-[#44483b] font-light mt-1">Nhận đơn hàng trên app, tối ưu hoá lộ trình giao hàng nhanh.</p>
                        </div>
                    </div>
                    <div class="glass-card p-5 rounded-2xl flex flex-col items-center text-center space-y-3 hover:border-lime-300/40 transition-colors border border-transparent">
                        <div class="w-14 h-14 bg-lime-100 text-lime-700 rounded-full flex items-center justify-center">
                            <span class="material-symbols-outlined text-[30px]">person_celebrate</span>
                        </div>
                        <div>
                            <h4 class="font-bold text-sm">Khách hàng</h4>
                            <p class="text-[11px] text-[#44483b] font-light mt-1">Nhận sầu riêng, dâu tây chín mọng tận cửa chỉ sau 2 giờ.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="section-divider h-px max-w-5xl mx-auto mb-4"></div>

    <%-- ================================================================
         REGIONAL SPECIALTIES
         ================================================================ --%>
    <section class="max-w-7xl mx-auto px-6 py-16 relative z-10">
        <div class="text-center max-w-2xl mx-auto mb-12 space-y-3">
            <div class="inline-flex items-center gap-2 bg-[#4d661c]/10 border border-[#4d661c]/15 px-4 py-1.5 rounded-full text-xs font-semibold text-[#4d661c]">
                <span class="material-symbols-outlined text-[15px]">location_on</span>
                Vùng nguyên liệu
            </div>
            <h2 class="text-3xl font-extrabold tracking-tight">Vùng nguyên liệu trứ danh</h2>
            <p class="text-sm text-[#44483b] font-light">Chúng tôi đồng hành cùng các hợp tác xã để phát triển chỉ dẫn địa lý nông sản Việt Nam.</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <%-- Card 1: Đà Lạt --%>
            <article class="bg-white rounded-3xl overflow-hidden shadow-sm border border-[#4d661c]/8 flex flex-col group hover-lift">
                <div class="relative bg-gradient-to-br from-red-50 to-pink-100 flex items-center justify-center" style="min-height:180px;">
                    <span class="material-symbols-outlined text-[80px] text-red-300">local_florist</span>
                    <span class="absolute top-3 left-3 bg-red-600 text-white text-[10px] font-bold px-2.5 py-1 rounded-md">Đà Lạt</span>
                </div>
                <div class="p-5 flex-grow">
                    <h3 class="font-bold text-sm text-[#00210d]">Dâu tây hữu cơ New Zealand</h3>
                    <p class="text-xs text-[#44483b]/80 font-light mt-1 leading-relaxed">Canh tác thủy canh trong nhà kính hiện đại tại cao nguyên Đà Lạt mờ sương.</p>
                </div>
            </article>

            <%-- Card 2: Bắc Giang --%>
            <article class="bg-white rounded-3xl overflow-hidden shadow-sm border border-[#4d661c]/8 flex flex-col group hover-lift">
                <div class="relative bg-gradient-to-br from-pink-50 to-rose-100 flex items-center justify-center" style="min-height:180px;">
                    <span class="material-symbols-outlined text-[80px] text-pink-300">spa</span>
                    <span class="absolute top-3 left-3 bg-red-600 text-white text-[10px] font-bold px-2.5 py-1 rounded-md">Bắc Giang</span>
                </div>
                <div class="p-5 flex-grow">
                    <h3 class="font-bold text-sm text-[#00210d]">Vải thiều Lục Ngạn</h3>
                    <p class="text-xs text-[#44483b]/80 font-light mt-1 leading-relaxed">Những chùm vải chín đỏ ngọt lịm từ vùng đất đồi trung du trù phú Bắc Giang.</p>
                </div>
            </article>

            <%-- Card 3: Miền Tây --%>
            <article class="bg-white rounded-3xl overflow-hidden shadow-sm border border-[#4d661c]/8 flex flex-col group hover-lift">
                <div class="relative bg-gradient-to-br from-yellow-50 to-amber-100 flex items-center justify-center" style="min-height:180px;">
                    <span class="material-symbols-outlined text-[80px] text-amber-400">emoji_nature</span>
                    <span class="absolute top-3 left-3 bg-red-600 text-white text-[10px] font-bold px-2.5 py-1 rounded-md">Miền Tây</span>
                </div>
                <div class="p-5 flex-grow">
                    <h3 class="font-bold text-sm text-[#00210d]">Sầu riêng Ri6 cơm vàng</h3>
                    <p class="text-xs text-[#44483b]/80 font-light mt-1 leading-relaxed">Cơm vàng hạt lép siêu ngọt béo ngậy, tuyển chọn kỹ lưỡng từ vườn Miền Tây.</p>
                </div>
            </article>

            <%-- Card 4: Ninh Thuận --%>
            <article class="bg-white rounded-3xl overflow-hidden shadow-sm border border-[#4d661c]/8 flex flex-col group hover-lift">
                <div class="relative bg-gradient-to-br from-purple-50 to-violet-100 flex items-center justify-center" style="min-height:180px;">
                    <span class="material-symbols-outlined text-[80px] text-purple-400">grocery</span>
                    <span class="absolute top-3 left-3 bg-red-600 text-white text-[10px] font-bold px-2.5 py-1 rounded-md">Ninh Thuận</span>
                </div>
                <div class="p-5 flex-grow">
                    <h3 class="font-bold text-sm text-[#00210d]">Nho xanh không hạt</h3>
                    <p class="text-xs text-[#44483b]/80 font-light mt-1 leading-relaxed">Giòn ngọt mọng nước từ thủ phủ nho Ninh Thuận đầy nắng và gió cát miền Trung.</p>
                </div>
            </article>
        </div>
    </section>

    <div class="section-divider h-px max-w-5xl mx-auto mb-4"></div>

    <%-- ================================================================
         CONTACT SECTION
         ================================================================ --%>
    <section id="contact" class="max-w-7xl mx-auto px-6 py-16 relative z-10 scroll-mt-20">
        <div class="text-center max-w-2xl mx-auto mb-12 space-y-3">
            <div class="inline-flex items-center gap-2 bg-[#4d661c]/10 border border-[#4d661c]/15 px-4 py-1.5 rounded-full text-xs font-semibold text-[#4d661c]">
                <span class="material-symbols-outlined text-[15px]">forum</span>
                Liên hệ với chúng tôi
            </div>
            <h2 class="text-3xl font-extrabold tracking-tight">Chúng tôi luôn lắng nghe</h2>
            <p class="text-sm text-[#44483b] font-light">Dù bạn là khách hàng, nhà vườn hay đối tác — hãy liên hệ với chúng tôi. Đội ngũ sẽ phản hồi trong vòng 24 giờ.</p>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">

            <%-- Left: Contact Info Cards --%>
            <div class="lg:col-span-5 space-y-5">

                <%-- Info card 1: Phone --%>
                <div class="glass-card p-5 rounded-2xl border border-emerald-100/60 flex gap-4 items-start hover-lift">
                    <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-2xl flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-[24px]">call</span>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm text-[#00210d]">Hotline hỗ trợ</h4>
                        <p class="text-xs text-[#44483b] font-light mt-0.5">Thứ 2 – Chủ nhật, 8:00 – 22:00</p>
                        <a href="tel:19008198" class="text-[#4d661c] font-bold text-sm mt-1 inline-block hover:underline">1900 8198</a>
                    </div>
                </div>

                <%-- Info card 2: Email --%>
                <div class="glass-card p-5 rounded-2xl border border-lime-100/60 flex gap-4 items-start hover-lift">
                    <div class="w-12 h-12 bg-lime-100 text-lime-700 rounded-2xl flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-[24px]">mail</span>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm text-[#00210d]">Email liên hệ</h4>
                        <p class="text-xs text-[#44483b] font-light mt-0.5">Phản hồi trong vòng 24 giờ làm việc</p>
                        <a href="mailto:support@metafruit.com" class="text-[#4d661c] font-bold text-sm mt-1 inline-block hover:underline">support@metafruit.com</a>
                    </div>
                </div>

                <%-- Info card 3: Address --%>
                <div class="glass-card p-5 rounded-2xl border border-amber-100/60 flex gap-4 items-start hover-lift">
                    <div class="w-12 h-12 bg-amber-100 text-amber-700 rounded-2xl flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-[24px]">location_on</span>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm text-[#00210d]">Địa chỉ văn phòng</h4>
                        <p class="text-xs text-[#44483b] font-light mt-0.5">Tầng 12, Toà nhà FPT</p>
                        <p class="text-xs text-[#44483b] font-light">Khu CNC Hoà Lạc, Thạch Thất, Hà Nội</p>
                    </div>
                </div>

                <%-- Social links row --%>
                <div class="glass-card p-5 rounded-2xl border border-[#4d661c]/10 hover-lift">
                    <p class="text-xs font-bold text-[#4d661c] mb-3 uppercase tracking-wider">Kết nối trên mạng xã hội</p>
                    <div class="flex gap-3">
                        <a href="#" title="Facebook"
                           class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 hover:scale-110 transition-all shadow-sm">
                            <i class="fa-brands fa-facebook-f text-sm"></i>
                        </a>
                        <a href="#" title="Instagram"
                           class="w-10 h-10 rounded-xl bg-gradient-to-br from-pink-500 to-rose-600 flex items-center justify-center text-white hover:scale-110 transition-all shadow-sm">
                            <i class="fa-brands fa-instagram text-sm"></i>
                        </a>
                        <a href="#" title="YouTube"
                           class="w-10 h-10 rounded-xl bg-red-600 flex items-center justify-center text-white hover:bg-red-700 hover:scale-110 transition-all shadow-sm">
                            <i class="fa-brands fa-youtube text-sm"></i>
                        </a>
                        <a href="#" title="TikTok"
                           class="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-white hover:bg-slate-900 hover:scale-110 transition-all shadow-sm">
                            <i class="fa-brands fa-tiktok text-sm"></i>
                        </a>
                    </div>
                </div>
            </div>

            <%-- Right: Contact Form --%>
            <div class="lg:col-span-7">
                <div class="glass-card border border-white/70 rounded-3xl p-8 shadow-xl">
                    <h3 class="text-lg font-bold text-[#00210d] mb-6 flex items-center gap-2">
                        <span class="material-symbols-outlined text-[22px] text-[#4d661c]">edit_note</span>
                        Gửi tin nhắn cho chúng tôi
                    </h3>
                    <form id="contactForm" action="${pageContext.request.contextPath}/contact" method="post" class="space-y-5">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div class="flex flex-col gap-1.5">
                                <label for="contactName" class="text-xs font-bold text-[#4d661c]">Họ và tên *</label>
                                <input id="contactName" name="contactName" type="text" required placeholder="Nguyễn Văn A"
                                       class="contact-input w-full px-4 py-2.5 bg-white border border-[#c5c8b7]/60 rounded-xl text-sm transition-all placeholder:text-[#75796a]/60 font-light">
                            </div>
                            <div class="flex flex-col gap-1.5">
                                <label for="contactEmail" class="text-xs font-bold text-[#4d661c]">Email *</label>
                                <input id="contactEmail" name="contactEmail" type="email" required placeholder="email@example.com"
                                       class="contact-input w-full px-4 py-2.5 bg-white border border-[#c5c8b7]/60 rounded-xl text-sm transition-all placeholder:text-[#75796a]/60 font-light">
                            </div>
                        </div>

                        <div class="flex flex-col gap-1.5">
                            <label for="contactPhone" class="text-xs font-bold text-[#4d661c]">Số điện thoại</label>
                            <input id="contactPhone" name="contactPhone" type="tel" placeholder="0912 345 678"
                                   class="contact-input w-full px-4 py-2.5 bg-white border border-[#c5c8b7]/60 rounded-xl text-sm transition-all placeholder:text-[#75796a]/60 font-light">
                        </div>

                        <div class="flex flex-col gap-1.5">
                            <label for="contactSubject" class="text-xs font-bold text-[#4d661c]">Chủ đề</label>
                            <select id="contactSubject" name="contactSubject"
                                    class="contact-input w-full px-4 py-2.5 bg-white border border-[#c5c8b7]/60 rounded-xl text-sm transition-all text-[#00210d]">
                                <option value="">Chọn chủ đề liên hệ</option>
                                <option value="Hỏi về sản phẩm / đặt hàng">Hỏi về sản phẩm / đặt hàng</option>
                                <option value="Hỗ trợ khiếu nại / hoàn trả">Hỗ trợ khiếu nại / hoàn trả</option>
                                <option value="Đăng ký mở gian hàng">Đăng ký mở gian hàng</option>
                                <option value="Hợp tác kinh doanh">Hợp tác kinh doanh</option>
                                <option value="Khác">Khác</option>
                            </select>
                        </div>

                        <div class="flex flex-col gap-1.5">
                            <label for="contactMessage" class="text-xs font-bold text-[#4d661c]">Nội dung tin nhắn *</label>
                            <textarea id="contactMessage" name="contactMessage" required rows="4"
                                      placeholder="Nhập nội dung bạn muốn gửi đến chúng tôi..."
                                      class="contact-input w-full px-4 py-2.5 bg-white border border-[#c5c8b7]/60 rounded-xl text-sm transition-all placeholder:text-[#75796a]/60 font-light resize-none leading-relaxed"></textarea>
                        </div>

                        <button type="submit" id="contactSubmitBtn"
                                class="w-full bg-[#4d661c] hover:bg-[#364e03] text-white font-bold text-sm py-3.5 px-6 rounded-xl transition-all flex items-center justify-center gap-2 shadow-lg shadow-[#4d661c]/20 hover:shadow-xl hover:-translate-y-0.5 active:scale-[0.98] group cursor-pointer">
                            <span class="material-symbols-outlined text-[18px] group-hover:translate-x-0.5 transition-transform">send</span>
                            Gửi tin nhắn
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </section>

    <div class="section-divider h-px max-w-5xl mx-auto mb-4"></div>

    <%-- ================================================================
         CALL TO ACTION
         ================================================================ --%>
    <section class="max-w-4xl mx-auto px-6 py-12 relative z-10 text-center">
        <div class="glass-card bg-white/60 border border-emerald-200/40 p-10 md:p-14 rounded-[36px] shadow-xl space-y-6">
            <span class="material-symbols-outlined text-[48px] text-[#4d661c]/60">volunteer_activism</span>
            <h2 class="text-3xl font-extrabold text-[#00210d] tracking-tight">Đồng hành phát triển nông nghiệp Việt</h2>
            <p class="text-sm text-[#44483b] max-w-xl mx-auto font-light leading-relaxed">
                Dù bạn là khách hàng muốn những trái quả ngọt ngon hay nông dân sở hữu nông trại chất lượng — MetaFruit đều có một vị trí tuyệt vời dành cho bạn.
            </p>
            <div class="flex flex-col sm:flex-row justify-center items-center gap-4 pt-2">
                <a href="${pageContext.request.contextPath}/products"
                   class="w-full sm:w-auto bg-[#4d661c] hover:bg-[#364e03] text-white font-bold text-sm px-8 py-3.5 rounded-full transition-all flex items-center justify-center gap-2 shadow-md hover:shadow-lg hover:-translate-y-0.5">
                    <span class="material-symbols-outlined text-[18px]">shopping_basket</span>
                    Mua sắm ngay
                </a>
                <a href="${pageContext.request.contextPath}/auth/register?accountType=SHOP_OWNER"
                   class="w-full sm:w-auto bg-white border border-[#4d661c] text-[#4d661c] hover:bg-emerald-50 font-bold text-sm px-8 py-3.5 rounded-full transition-all flex items-center justify-center gap-2 shadow-sm hover:-translate-y-0.5">
                    <span class="material-symbols-outlined text-[18px]">store</span>
                    Đăng ký mở Cửa hàng
                </a>
            </div>
        </div>
    </section>

</div><%-- end main wrapper --%>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
