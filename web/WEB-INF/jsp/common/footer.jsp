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

    <%-- ===================== AI BOT FLOATING SEARCH WIDGET ===================== --%>
    <div id="ai-chat-bubble" onclick="toggleAiChat()" 
         class="fixed bottom-6 right-6 z-[999] w-14 h-14 bg-gradient-to-r from-emerald-600 to-teal-700 text-white rounded-full flex items-center justify-center cursor-pointer shadow-2xl hover:scale-110 active:scale-95 transition-all duration-300 group"
         title="Hỏi Trợ lý AI Tìm sản phẩm">
        <span class="material-symbols-outlined text-[28px] animate-pulse">psychology</span>
        <span class="absolute -top-1 -right-1 flex h-4 w-4">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-4 w-4 bg-red-500 text-[9px] font-extrabold text-white items-center justify-center">AI</span>
        </span>
    </div>

    <div id="ai-chat-box" 
         class="fixed bottom-24 right-6 z-[999] w-96 h-[520px] max-w-[calc(100vw-2rem)] bg-white/95 backdrop-blur-md rounded-3xl shadow-2xl flex flex-col overflow-hidden border border-emerald-100/50 transition-all duration-300 transform scale-95 origin-bottom-right opacity-0 pointer-events-none">
        
        <%-- Header --%>
        <div class="bg-gradient-to-r from-[#14532D] to-[#0f3d24] text-white p-4 flex items-center justify-between shadow-md">
            <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-full bg-white/10 flex items-center justify-center shadow-inner">
                    <span class="material-symbols-outlined text-[22px] text-emerald-300">psychology</span>
                </div>
                <div>
                    <h4 class="font-bold text-sm leading-tight">Trợ lý AI MetaFruit 🌿</h4>
                    <p class="text-[10px] text-emerald-300/80 font-light">Tư vấn tìm kiếm hoa quả chín cây</p>
                </div>
            </div>
            <div class="flex items-center gap-1">
                <button onclick="clearAiChatHistory()" title="Xóa lịch sử chat" class="text-white/50 hover:text-red-300 hover:bg-white/10 rounded-full w-7 h-7 flex items-center justify-center transition-colors">
                    <span class="material-symbols-outlined text-[17px]">delete_sweep</span>
                </button>
                <button onclick="toggleAiChat()" class="text-white/70 hover:text-white hover:bg-white/10 rounded-full w-7 h-7 flex items-center justify-center transition-colors">
                    <span class="material-symbols-outlined text-[20px]">close</span>
                </button>
            </div>
        </div>

        <%-- Message Log --%>
        <div id="ai-message-log" class="flex-1 p-4 overflow-y-auto space-y-4 bg-slate-50/50 hide-scrollbar">
            <%-- Initial greeting is rendered by JS to avoid duplication when history exists --%>
        </div>

        <%-- Quick Suggestion Tag list --%>
        <div class="px-4 py-2 border-t border-emerald-50 bg-white flex items-center gap-1.5 overflow-x-auto hide-scrollbar whitespace-nowrap">
            <button onclick="sendQuickAiQuery('Mua biếu người ốm nên chọn quả gì?')" class="text-[10px] bg-slate-100 hover:bg-emerald-50 border border-slate-200/60 rounded-full px-2.5 py-1 text-on-surface-variant font-medium transition-all">🏥 Người ốm</button>
            <button onclick="sendQuickAiQuery('Hoa quả nào nhập khẩu giàu vitamin C')" class="text-[10px] bg-slate-100 hover:bg-emerald-50 border border-slate-200/60 rounded-full px-2.5 py-1 text-on-surface-variant font-medium transition-all">🥝 Giàu Vitamin C</button>
            <button onclick="sendQuickAiQuery('Tư vấn hộp quà tặng sang trọng')" class="text-[10px] bg-slate-100 hover:bg-emerald-50 border border-slate-200/60 rounded-full px-2.5 py-1 text-on-surface-variant font-medium transition-all">🎁 Hộp quà</button>
            <button onclick="sendQuickAiQuery('Cách chọn sầu riêng chín ngon')" class="text-[10px] bg-slate-100 hover:bg-emerald-50 border border-slate-200/60 rounded-full px-2.5 py-1 text-on-surface-variant font-medium transition-all">👑 Chọn sầu riêng</button>
        </div>

        <%-- Input form --%>
        <div class="p-3 bg-white border-t border-emerald-100/40 flex gap-2">
            <input type="text" id="ai-chat-input" onkeydown="handleAiInputKey(event)"
                   class="flex-1 rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-on-surface placeholder:text-slate-400"
                   placeholder="Nhập câu hỏi tìm kiếm hoa quả của bạn...">
            <button onclick="sendAiChatMessage()" id="ai-chat-send-btn"
                    class="bg-primary hover:bg-primary-hover text-white px-4 py-2 rounded-xl flex items-center justify-center transition-colors shadow-md shadow-emerald-900/10 cursor-pointer">
                <span class="material-symbols-outlined text-[18px]">send</span>
            </button>
        </div>
    </div>

    <style>
        .hide-scrollbar::-webkit-scrollbar { display: none; }
        .hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
    </style>

    <script>
        function toggleAiChat() {
            const chatBox = document.getElementById('ai-chat-box');
            if (chatBox.classList.contains('pointer-events-none')) {
                chatBox.classList.remove('pointer-events-none', 'scale-95', 'opacity-0');
                chatBox.classList.add('scale-100', 'opacity-100');
                document.getElementById('ai-chat-input').focus();
                sessionStorage.setItem(AI_CHAT_OPEN_KEY, '1');
            } else {
                chatBox.classList.add('pointer-events-none', 'scale-95', 'opacity-0');
                chatBox.classList.remove('scale-100', 'opacity-100');
                sessionStorage.setItem(AI_CHAT_OPEN_KEY, '0');
            }
        }

        function handleAiInputKey(event) {
            if (event.key === 'Enter') {
                sendAiChatMessage();
            }
        }

        function sendQuickAiQuery(query) {
            document.getElementById('ai-chat-input').value = query;
            sendAiChatMessage();
        }

        // Apply quick AI prompt from home banner suggestions
        function applyAiPrompt(promptText) {
            const chatBox = document.getElementById('ai-chat-box');
            if (chatBox.classList.contains('pointer-events-none')) {
                toggleAiChat();
            }
            document.getElementById('ai-chat-input').value = promptText;
            sendAiChatMessage();
        }

        // ============================================================
        // CHAT HISTORY PERSISTENCE — sessionStorage
        // Format: aiChatHistory = [{sender,text,products,suggestedIds},...]
        // ============================================================
        const AI_HISTORY_KEY = 'aiChatHistory';
        const AI_CHAT_OPEN_KEY = 'aiChatOpen';

        function saveMsgToHistory(sender, text, products, suggestedIds) {
            try {
                let history = JSON.parse(sessionStorage.getItem(AI_HISTORY_KEY) || '[]');
                history.push({ sender, text, products: products || null, suggestedIds: suggestedIds || null });
                if (history.length > 50) history = history.slice(history.length - 50);
                sessionStorage.setItem(AI_HISTORY_KEY, JSON.stringify(history));
            } catch(e) {}
        }

        function clearAiChatHistory() {
            sessionStorage.removeItem(AI_HISTORY_KEY);
            sessionStorage.removeItem('aiFilteredProductIds');
            const log = document.getElementById('ai-message-log');
            // Xóa tất cả tin nhắn, giữ lại lời chào đầu tiên
            while (log.children.length > 1) log.removeChild(log.lastChild);
            // Nếu không còn phần tử nào (log rỗng), thêm lại lời chào
            if (log.children.length === 0) renderWelcomeMessage();
            log.scrollTop = 0;
            // B7: Nếu đang ở trang sản phẩm, cập nhật lại grid ngay lập tức
            if (typeof applyClientFilters === 'function') {
                applyClientFilters();
            }
        }

        function renderWelcomeMessage() {
            const log = document.getElementById('ai-message-log');
            if (log.querySelector('[data-welcome]')) return;
            log.insertAdjacentHTML('afterbegin', `
                <div class="flex items-start gap-2 max-w-[85%]" data-welcome="1">
                    <div class="w-7 h-7 rounded-full bg-emerald-100 flex items-center justify-center text-primary shrink-0 shadow-sm">
                        <span class="material-symbols-outlined text-[16px]">robot_2</span>
                    </div>
                    <div class="bg-white border border-emerald-100/50 rounded-2xl rounded-tl-none p-3 text-xs text-on-surface shadow-sm leading-relaxed">
                        Xin chào! Tôi là Trợ lý AI của <strong>MetaFruit</strong>. 🍊<br><br>
                        Tôi có thể giúp gì cho bạn hôm nay? Ví dụ:<br>
                        - <em>"Mua giỏ quả biếu người ốm"</em> 🏥<br>
                        - <em>"Trái cây nhập khẩu giàu Vitamin C"</em> 🥝<br>
                        - <em>"Cách chọn bưởi da xanh ngọt"</em> 🍈
                    </div>
                </div>
            `);
        }

        function restoreChatHistory() {
            try {
                renderWelcomeMessage();
                const history = JSON.parse(sessionStorage.getItem(AI_HISTORY_KEY) || '[]');
                if (history.length === 0) return;
                history.forEach(function(msg) {
                    appendMessageDOM(msg.sender, msg.text, msg.products, msg.suggestedIds, false);
                });
                const log = document.getElementById('ai-message-log');
                log.scrollTop = log.scrollHeight;
            } catch(e) {}
        }

        // Khôi phục lịch sử và trạng thái mở/đóng sau khi DOM sẵn sàng
        document.addEventListener('DOMContentLoaded', function() {
            restoreChatHistory();
            if (sessionStorage.getItem(AI_CHAT_OPEN_KEY) === '1') {
                const chatBox = document.getElementById('ai-chat-box');
                chatBox.classList.remove('pointer-events-none', 'scale-95', 'opacity-0');
                chatBox.classList.add('scale-100', 'opacity-100');
            }
        });

        function formatMarkdown(text) {
            if (!text) return "";
            return text
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
                .replace(/\*(.*?)\*/g, "<em>$1</em>")
                .replace(/`([^`]+)`/g, "<code class='bg-slate-100 text-red-600 px-1 rounded font-mono text-[10px]'>$1</code>")
                .replace(/\n/g, "<br>");
        }

        function escapeHtml(text) {
            return String(text || '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        async function sendAiChatMessage() {
            const input = document.getElementById('ai-chat-input');
            const message = input.value.trim();
            if (!message) return;

            input.value = '';
            appendMessage('user', message);
            
            // Append loading indicator
            const log = document.getElementById('ai-message-log');
            const loadingId = 'ai-loading-' + Date.now();
            const loadingHtml = '<div class="flex items-start gap-2 max-w-[85%] animate-pulse" id="' + loadingId + '">' +
                '<div class="w-7 h-7 rounded-full bg-emerald-100 flex items-center justify-center text-primary shrink-0 shadow-sm">' +
                    '<span class="material-symbols-outlined text-[16px]">robot_2</span>' +
                '</div>' +
                '<div class="bg-white border border-emerald-100/50 rounded-2xl rounded-tl-none p-3 text-xs text-on-surface-variant shadow-sm italic flex items-center gap-1.5">' +
                    '<span class="material-symbols-outlined text-[14px] animate-spin">sync</span> AI đang phân tích kho hàng...' +
                '</div>' +
            '</div>';
            log.insertAdjacentHTML('beforeend', loadingHtml);
            log.scrollTop = log.scrollHeight;

            try {
                const contextPath = window.contextPath || '${pageContext.request.contextPath}';
                const response = await fetch(`${contextPath}/api/ai/search`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: JSON.stringify({ message: message })
                });
                const responseText = await response.text();
                let data = {};
                try {
                    data = responseText ? JSON.parse(responseText) : {};
                } catch (parseError) {
                    data = {
                        success: false,
                        message: responseText && responseText.trim()
                            ? responseText.trim()
                            : 'Lỗi khi kết nối với dịch vụ AI: HTTP ' + response.status
                    };
                }
                
                // Remove loading
                const loadingEl = document.getElementById(loadingId);
                if (loadingEl) loadingEl.remove();

                if (response.ok && data.success) {
                    appendMessage('ai', data.reply, data.products, data.suggestedProductIds);
                } else {
                    appendMessage('ai', data.message || ('Lỗi khi kết nối với dịch vụ AI: HTTP ' + response.status));
                }
            } catch (error) {
                console.error('Lỗi khi gọi AI:', error);
                const loadingEl = document.getElementById(loadingId);
                if (loadingEl) loadingEl.remove();
                appendMessage('ai', 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.');
            }
        }

        // appendMessage ghi vào DOM VÀ lưu history
        function appendMessage(sender, text, products, suggestedIds) {
            appendMessageDOM(sender, text, products, suggestedIds, true);
        }

        // appendMessageDOM — render vào DOM, optionally lưu vào sessionStorage
        function appendMessageDOM(sender, text, products, suggestedIds, saveToHistory) {
            const log = document.getElementById('ai-message-log');
            let contentHtml = '';

            if (sender === 'user') {
                contentHtml = '<div class="flex justify-end w-full">' +
                    '<div class="bg-primary text-white rounded-2xl rounded-tr-none p-3 text-xs max-w-[85%] shadow-sm leading-relaxed">' +
                        formatMarkdown(text) +
                    '</div>' +
                '</div>';
            } else {
                let productsHtml = '';
                if (products && products.length > 0) {
                    productsHtml = '<div class="mt-3 grid grid-cols-1 gap-2.5 border-t border-slate-100 pt-3">';
                    products.forEach(p => {
                        let imgUrl = p.image || 'assets/img/placeholder.png';
                        if (imgUrl && !imgUrl.startsWith('http://') && !imgUrl.startsWith('https://')) {
                            if (!imgUrl.startsWith('/')) imgUrl = '/' + imgUrl;
                            imgUrl = '${pageContext.request.contextPath}' + imgUrl;
                        }

                        const contextPath = window.contextPath || '${pageContext.request.contextPath}';
                        const detailUrl = contextPath + '/products/detail?id=' + encodeURIComponent(p.productId);
                        const priceFormatted = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p.price);
                        const safeName = escapeHtml(p.name);
                        const safeUnit = escapeHtml(p.unit || 'kg');
                        const safeImgAttr = escapeHtml(imgUrl);
                        const safeNameJs = JSON.stringify(String(p.name || ''));
                        const safeImgJs = JSON.stringify(String(imgUrl));
                        const productId = Number(p.productId) || 0;
                        const variantId = Number(p.variantId || 0) || 0;

                        productsHtml += '<div class="flex items-center gap-3 bg-slate-50 border border-slate-100 p-2 rounded-xl hover:bg-emerald-50/20 transition-colors">' +
                                '<a href="' + detailUrl + '" title="Xem chi tiết sản phẩm" class="shrink-0">' +
                                    '<img src="' + safeImgAttr + '" alt="' + safeName + '" class="w-12 h-12 rounded-lg object-cover bg-emerald-50 shadow-sm border border-slate-200 hover:opacity-80 transition-opacity">' +
                                '</a>' +
                                '<div class="flex-1 min-w-0">' +
                                    '<a href="' + detailUrl + '" class="hover:text-primary transition-colors">' +
                                        '<h5 class="text-xs font-bold text-on-surface truncate">' + safeName + '</h5>' +
                                    '</a>' +
                                    '<p class="text-[11px] font-bold text-primary">' + priceFormatted + ' <span class="text-[9px] text-on-surface-variant font-light">/' + safeUnit + '</span></p>' +
                                '</div>' +
                                '<div class="flex items-center gap-1 shrink-0">' +
                                    '<a href="' + detailUrl + '" class="bg-slate-100 hover:bg-slate-200 text-on-surface p-1.5 rounded-lg flex items-center justify-center transition-colors" title="Xem chi tiết">' +
                                        '<span class="material-symbols-outlined text-[15px]">visibility</span>' +
                                    '</a>' +
                                    '<button type="button" onclick=\'triggerQuickAddFromAi(event, ' + productId + ', ' + variantId + ', ' + safeNameJs + ', ' + (p.price || 0) + ', ' + safeImgJs + ')\' class="bg-primary hover:bg-primary-hover text-white p-1.5 rounded-lg flex items-center justify-center transition-transform active:scale-90 cursor-pointer" title="Thêm nhanh vào giỏ">' +
                                        '<span class="material-symbols-outlined text-[15px]">add_shopping_cart</span>' +
                                    '</button>' +
                                '</div>' +
                            '</div>';
                    });
                    productsHtml += '</div>';

                    // Save selected product IDs to sessionStorage to maintain filter state
                    if (suggestedIds && suggestedIds.length > 0) {
                        sessionStorage.setItem('aiFilteredProductIds', JSON.stringify(suggestedIds));

                        if (typeof applyClientFilters === 'function') {
                            applyClientFilters();
                        } else {
                            const contextPath = window.contextPath || '${pageContext.request.contextPath}';
                            productsHtml += '<div class="mt-3 text-center">' +
                                '<a href="' + contextPath + '/products?fromAi=true&suggestedIds=' + encodeURIComponent(suggestedIds.join(',')) + '" class="inline-flex items-center gap-1.5 bg-emerald-100 hover:bg-emerald-200 text-emerald-800 font-bold text-[10px] px-3.5 py-2 rounded-full transition-all shadow-sm">' +
                                    '<span class="material-symbols-outlined text-[14px]">open_in_new</span> Xem danh sách trên trang Sản phẩm' +
                                '</a>' +
                            '</div>';
                        }
                    }
                }

                contentHtml = '<div class="flex items-start gap-2 max-w-[90%]">' +
                    '<div class="w-7 h-7 rounded-full bg-emerald-100 flex items-center justify-center text-primary shrink-0 shadow-sm">' +
                        '<span class="material-symbols-outlined text-[16px]">robot_2</span>' +
                    '</div>' +
                    '<div class="bg-white border border-emerald-100/50 rounded-2xl rounded-tl-none p-3 text-xs text-on-surface shadow-sm leading-relaxed w-full">' +
                        '<div>' + formatMarkdown(text) + '</div>' +
                        productsHtml +
                    '</div>' +
                '</div>';
            }

            log.insertAdjacentHTML('beforeend', contentHtml);
            log.scrollTop = log.scrollHeight;

            if (saveToHistory) {
                saveMsgToHistory(sender, text, products || null, suggestedIds || null);
            }
        }

        function triggerQuickAddFromAi(event, productId, variantId, name, price, imagePath) {
            if (event) {
                event.preventDefault();
                event.stopPropagation();
            }
            if (variantId > 0 && typeof window.addCartItem === 'function') {
                window.addCartItem(variantId, 1, name, price, imagePath, 99, productId);
            } else {
                if (window.quickAddProductGlobal) {
                    window.quickAddProductGlobal(productId);
                } else if (typeof quickAddProduct === 'function') {
                    quickAddProduct(null, productId);
                } else {
                    window.location.href = '${pageContext.request.contextPath}/products/detail?id=' + productId;
                }
            }
        }
    </script>
</body>
</html>
