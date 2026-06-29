<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Quản lý Tin nhắn (Shop)" />
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

<main class="flex-1 overflow-hidden bg-[#eaffea] text-[#00210d] flex chat-layout relative" style="background-image: radial-gradient(circle at top right, rgba(217,249,157,0.15), transparent 40%);">
    
    <!-- Left: Session List -->
    <aside class="w-full md:w-[300px] lg:w-[340px] flex-shrink-0 flex flex-col border-r border-white/30 glass-panel bg-white/40 relative z-10 hidden md:flex">
        <div class="p-4 border-b border-white/20">
            <h1 class="text-lg font-bold text-[#4d661c] mb-3 flex items-center gap-2">
                <span class="material-symbols-outlined">forum</span> Khách hàng
                <c:if test="${not empty chatSessions}">
                    <c:set var="totalUnread" value="0"/>
                    <c:forEach var="s" items="${chatSessions}"><c:set var="totalUnread" value="${totalUnread + s.unreadCount}"/></c:forEach>
                    <c:if test="${totalUnread > 0}"><span class="unread-badge ml-auto">${totalUnread > 99 ? '99+' : totalUnread}</span></c:if>
                </c:if>
            </h1>
            <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">search</span>
                <input id="searchSessions" class="w-full pl-10 pr-4 py-2 bg-white/50 border border-white/40 rounded-xl focus:outline-none focus:border-[#4d661c] text-sm placeholder:text-slate-400" placeholder="Tìm khách hàng..." type="text">
            </div>
        </div>
        <div id="sessionList" class="flex-1 overflow-y-auto p-2 space-y-1">
            <c:choose>
                <c:when test="${empty chatSessions}">
                    <div class="p-8 text-center text-slate-400">
                        <span class="material-symbols-outlined text-4xl block mb-2">inbox</span>
                        <p class="text-sm">Chưa có khách hàng nào nhắn tin.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="session" items="${chatSessions}">
                        <a href="${pageContext.request.contextPath}/shop/chat?sessionId=${session.sessionId}"
                           class="session-item flex items-center gap-3 p-3 rounded-xl hover:bg-white/40 border border-transparent transition-all ${session.sessionId == activeSessionId ? 'active shadow-sm' : 'bg-white/20'}"
                           data-name="${fn:escapeXml(session.partnerName)}" data-session-id="${session.sessionId}">
                            <div class="relative shrink-0">
                                <c:choose>
                                    <c:when test="${not empty session.partnerAvatar}">
                                        <img src="${fn:startsWith(session.partnerAvatar,'http') ? session.partnerAvatar : pageContext.request.contextPath.concat('/').concat(session.partnerAvatar)}" class="w-11 h-11 rounded-full object-cover border border-white shadow-sm" alt="Avatar">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="w-11 h-11 rounded-full bg-slate-200 flex items-center justify-center text-slate-500 border border-white shadow-sm">
                                            <span class="material-symbols-outlined text-lg">person</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <span class="online-dot-${session.sessionId} absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white bg-slate-300"></span>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex justify-between items-center mb-0.5">
                                    <span class="text-sm font-semibold text-slate-800 truncate">
                                        <c:choose>
                                            <c:when test="${not empty session.partnerName}"><c:out value="${session.partnerName}"/></c:when>
                                            <c:otherwise>Khách hàng #${session.customerId}</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="text-[10px] text-slate-400 shrink-0 ml-1"><fmt:formatDate value="${session.updatedAtAsDate}" pattern="HH:mm"/></span>
                                </div>
                                <div class="flex items-center gap-1">
                                    <p class="text-xs text-slate-400 truncate flex-1 last-msg-${session.sessionId}">
                                        <c:choose>
                                            <c:when test="${session.lastMessageType == 'IMAGE'}">📷 Hình ảnh</c:when>
                                            <c:when test="${session.lastMessageType == 'VIDEO'}">🎥 Video</c:when>
                                            <c:when test="${not empty session.lastMessage}"><c:out value="${fn:length(session.lastMessage) > 35 ? fn:substring(session.lastMessage,0,35).concat('…') : session.lastMessage}"/></c:when>
                                            <c:otherwise>Bấm để trả lời hỗ trợ</c:otherwise>
                                        </c:choose>
                                    </p>
                                    <c:if test="${session.unreadCount > 0}">
                                        <span class="unread-badge shrink-0 unread-badge-${session.sessionId}">${session.unreadCount > 99 ? '99+' : session.unreadCount}</span>
                                    </c:if>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </aside>
    
    <!-- Center: Chat window -->
    <section class="flex-1 flex flex-col relative bg-white/40 min-w-0">
        <c:choose>
            <c:when test="${activeSessionId > 0}">
                <!-- Chat Header -->
                <div class="px-4 py-3 border-b border-white/40 glass-panel bg-white/70 flex justify-between items-center sticky top-0 z-10 shadow-sm">
                    <div class="flex items-center gap-3">
                        <div class="md:hidden">
                            <a href="${pageContext.request.contextPath}/shop/chat" class="p-2 text-slate-500 hover:bg-slate-100 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">arrow_back</span>
                            </a>
                        </div>
                        <div class="relative">
                            <c:choose>
                                <c:when test="${not empty activeSession.partnerAvatar}">
                                    <img id="headerAvatar" src="${fn:startsWith(activeSession.partnerAvatar,'http') ? activeSession.partnerAvatar : pageContext.request.contextPath.concat('/').concat(activeSession.partnerAvatar)}" class="w-10 h-10 rounded-full object-cover border border-white shadow-sm" alt="Avatar">
                                </c:when>
                                <c:otherwise>
                                    <div class="w-10 h-10 rounded-full bg-[#b4f0c9] flex items-center justify-center text-[#175034] border border-white shadow-sm">
                                        <span class="material-symbols-outlined">person</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <span id="partnerOnlineDot" class="absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white bg-slate-300"></span>
                        </div>
                        <div>
                            <h2 class="text-sm font-bold text-slate-800">
                                <c:choose>
                                    <c:when test="${not empty activeSession.partnerName}"><c:out value="${activeSession.partnerName}"/></c:when>
                                    <c:otherwise>Khách hàng #${activeSession.customerId}</c:otherwise>
                                </c:choose>
                            </h2>
                            <div id="partnerStatusText" class="ws-status text-slate-400">
                                <span class="ws-dot offline"></span> Đang kiểm tra...
                            </div>
                        </div>
                    </div>
                    <div class="flex items-center gap-2">
                        <div id="wsStatusBadge" class="ws-status text-amber-600 hidden lg:flex">
                            <span class="ws-dot connecting"></span> Đang kết nối...
                        </div>
                        <!-- Toggle info panel button -->
                        <button id="btnToggleInfo" title="Thông tin khách hàng" class="p-2 text-slate-500 hover:text-[#4d661c] hover:bg-[#eaffea] rounded-full transition-colors flex items-center justify-center">
                            <span class="material-symbols-outlined text-xl">info</span>
                        </button>
                    </div>
                </div>
                
                <!-- Messages Area -->
                <div id="chatBox" class="flex-1 overflow-y-auto p-4 space-y-3 flex flex-col">
                    <!-- Skeleton loading -->
                    <div id="skeletonLoader" class="space-y-3">
                        <div class="flex gap-2 items-end max-w-[60%]">
                            <div class="w-7 h-7 rounded-full skeleton flex-shrink-0"></div>
                            <div class="space-y-1 flex-1"><div class="h-10 skeleton rounded-2xl rounded-bl-sm"></div></div>
                        </div>
                        <div class="flex gap-2 items-end max-w-[60%] self-end flex-row-reverse">
                            <div class="space-y-1 flex-1"><div class="h-8 skeleton rounded-2xl rounded-br-sm"></div></div>
                        </div>
                        <div class="flex gap-2 items-end max-w-[50%]">
                            <div class="w-7 h-7 rounded-full skeleton flex-shrink-0"></div>
                            <div class="space-y-1 flex-1"><div class="h-6 skeleton rounded-2xl rounded-bl-sm"></div></div>
                        </div>
                    </div>
                    <div id="chatStart" class="hidden flex justify-center">
                        <span class="text-[11px] bg-white/50 backdrop-blur-sm border border-white/40 text-slate-400 px-3 py-1 rounded-full shadow-sm">Bắt đầu cuộc trò chuyện</span>
                    </div>
                </div>

                <!-- Media Preview Panel -->
                <div id="uploadPreviewPanel" class="hidden px-4 py-2 bg-slate-50 border-t border-white/40 flex items-center gap-3">
                    <div class="relative w-14 h-14 rounded-lg border border-slate-300 overflow-hidden bg-black flex items-center justify-center">
                        <img id="imagePreview" class="hidden w-full h-full object-cover" alt="preview"/>
                        <video id="videoPreview" class="hidden w-full h-full object-cover"></video>
                        <div id="uploadProgressOverlay" class="absolute inset-0 bg-black/60 flex items-center justify-center text-white text-xs font-bold">0%</div>
                    </div>
                    <div class="flex-1 min-w-0">
                        <p class="text-xs font-semibold text-slate-700 truncate" id="previewFileName">file.jpg</p>
                        <p class="text-[10px] text-slate-400" id="previewStatus">Đang tải lên...</p>
                    </div>
                    <button id="btnCancelUpload" class="p-1 hover:bg-slate-200 text-slate-500 rounded-full transition-colors">
                        <span class="material-symbols-outlined text-lg">close</span>
                    </button>
                </div>
                
                <!-- Input Area -->
                <div class="p-3 bg-white/60 backdrop-blur-lg border-t border-white/40 shadow-sm z-10">
                    <div class="flex items-end gap-2 bg-[#eaffea] border border-[#c5c8b7]/40 p-2 rounded-2xl shadow-inner focus-within:border-[#4d661c] transition-colors">
                        <input type="file" id="mediaInput" accept="image/*,video/*" class="hidden">
                        <button type="button" id="btnTriggerUpload" class="p-2 text-slate-500 hover:text-[#4d661c] transition-colors rounded-full hover:bg-white/40 flex-shrink-0" title="Tải ảnh/video">
                            <span class="material-symbols-outlined text-xl">image</span>
                        </button>
                        <textarea id="chatInput" class="flex-1 bg-transparent border-none resize-none focus:ring-0 text-slate-800 placeholder:text-slate-400 text-sm py-2 px-1 max-h-32 min-h-[40px] focus:outline-none" placeholder="Nhập câu trả lời hỗ trợ..." rows="1"></textarea>
                        <button type="button" id="btnSendMessage" class="p-2 bg-[#4d661c] text-white hover:bg-[#31694b] transition-colors rounded-full flex-shrink-0 mb-0.5 disabled:opacity-50" disabled>
                            <span class="material-symbols-outlined text-xl" style="font-variation-settings:'FILL' 1;">send</span>
                        </button>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="flex-1 flex flex-col items-center justify-center text-slate-400 p-8">
                    <div class="w-24 h-24 bg-[#b4f0c9]/30 rounded-full flex items-center justify-center mb-4 border border-white shadow-inner">
                        <span class="material-symbols-outlined text-5xl text-[#4d661c]/60">chat_bubble</span>
                    </div>
                    <h3 class="text-base font-bold text-[#4d661c] mb-1">Chưa chọn khách hàng</h3>
                    <p class="text-xs text-slate-500">Chọn một khách hàng ở danh sách bên trái để bắt đầu</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- Right: Customer Info Panel (collapsible) -->
    <c:if test="${activeSessionId > 0}">
    <aside id="infoPanel" class="w-[260px] flex-shrink-0 flex flex-col border-l border-white/30 glass-panel bg-white/40 overflow-y-auto collapsed">
        <div class="p-4 border-b border-white/20 flex items-center justify-between">
            <h3 class="text-sm font-bold text-[#4d661c] flex items-center gap-1.5">
                <span class="material-symbols-outlined text-base">person_info</span> Thông tin KH
            </h3>
            <button id="btnCloseInfo" class="p-1 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition-colors">
                <span class="material-symbols-outlined text-lg">close</span>
            </button>
        </div>
        <!-- Avatar + Name -->
        <div class="p-4 flex flex-col items-center text-center border-b border-white/20">
            <c:choose>
                <c:when test="${not empty activeSession.partnerAvatar}">
                    <img src="${fn:startsWith(activeSession.partnerAvatar,'http') ? activeSession.partnerAvatar : pageContext.request.contextPath.concat('/').concat(activeSession.partnerAvatar)}" class="w-16 h-16 rounded-full object-cover border-2 border-white shadow-md mb-2" alt="Avatar">
                </c:when>
                <c:otherwise>
                    <div class="w-16 h-16 rounded-full bg-[#b4f0c9] flex items-center justify-center text-[#175034] border-2 border-white shadow-md mb-2">
                        <span class="material-symbols-outlined text-2xl">person</span>
                    </div>
                </c:otherwise>
            </c:choose>
            <p class="text-sm font-bold text-slate-800">
                <c:choose>
                    <c:when test="${not empty activeSession.partnerName}"><c:out value="${activeSession.partnerName}"/></c:when>
                    <c:otherwise>Khách hàng #${activeSession.customerId}</c:otherwise>
                </c:choose>
            </p>
            <p class="text-[11px] text-slate-400 mt-0.5">ID: #${activeSession.customerId}</p>
            <div class="mt-2 ws-status text-slate-400" id="infoPanelStatus">
                <span class="ws-dot offline"></span> <span id="infoPanelStatusLabel">Không trực tuyến</span>
            </div>
        </div>
        <!-- Session Info -->
        <div class="p-4 border-b border-white/20 space-y-2">
            <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide">Phiên chat</p>
            <div class="flex justify-between text-xs"><span class="text-slate-500">Session ID</span><span class="font-medium">#${activeSession.sessionId}</span></div>
            <div class="flex justify-between text-xs"><span class="text-slate-500">Trạng thái</span><span class="font-medium ${activeSession.status == 'ACTIVE' ? 'text-emerald-600' : 'text-slate-500'}">${activeSession.status}</span></div>
            <div class="flex justify-between text-xs"><span class="text-slate-500">Bắt đầu</span><span class="font-medium"><fmt:formatDate value="${activeSession.createdAtAsDate}" pattern="dd/MM HH:mm"/></span></div>
        </div>
        <!-- Shared Media -->
        <div class="p-4 flex-1">
            <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wide mb-3">Ảnh &amp; Video đã chia sẻ</p>
            <div id="sharedMediaGrid" class="grid grid-cols-3 gap-1">
                <p class="col-span-3 text-xs text-slate-400 text-center py-4">Chưa có ảnh/video</p>
            </div>
        </div>
    </aside>
    </c:if>
</main>

<c:if test="${activeSessionId > 0}">
<script>
(function() {
    const CTX = '${pageContext.request.contextPath}';
    const sessionId = parseInt('${activeSessionId}', 10);
    const currentUserId = parseInt('${sessionScope.currentUser != null ? sessionScope.currentUser.userId : -1}', 10);
    const partnerUserId = parseInt('${activeSession.customerId}', 10);
    const CSRF_TOKEN = '${sessionScope._csrfToken}';

    // ── DOM refs ──────────────────────────────────────────────────────
    const chatBox = document.getElementById('chatBox');
    const chatInput = document.getElementById('chatInput');
    const btnSend = document.getElementById('btnSendMessage');
    const wsBadge = document.getElementById('wsStatusBadge');
    const partnerDot = document.getElementById('partnerOnlineDot');
    const partnerStatus = document.getElementById('partnerStatusText');
    const infoPanel = document.getElementById('infoPanel');
    const btnToggleInfo = document.getElementById('btnToggleInfo');
    const btnCloseInfo = document.getElementById('btnCloseInfo');
    const skeletonLoader = document.getElementById('skeletonLoader');
    const chatStart = document.getElementById('chatStart');
    const sharedMediaGrid = document.getElementById('sharedMediaGrid');
    const uploadPanel = document.getElementById('uploadPreviewPanel');
    const imgPreview = document.getElementById('imagePreview');
    const vidPreview = document.getElementById('videoPreview');
    const previewFileName = document.getElementById('previewFileName');
    const previewStatus = document.getElementById('previewStatus');
    const progressOverlay = document.getElementById('uploadProgressOverlay');
    const mediaInput = document.getElementById('mediaInput');

    // ── State ─────────────────────────────────────────────────────────
    let ws = null, wsReady = false, isUploading = false;
    let renderedIds = new Set();
    let pendingMedia = null; // {url, type}
    let sharedMedia = [];
    let lastMsgEl = null; // last sent message element for status update

    // ── Info Panel Toggle ─────────────────────────────────────────────
    if (btnToggleInfo) {
        btnToggleInfo.addEventListener('click', () => {
            infoPanel && infoPanel.classList.toggle('collapsed');
        });
    }
    if (btnCloseInfo) {
        btnCloseInfo.addEventListener('click', () => {
            infoPanel && infoPanel.classList.add('collapsed');
        });
    }

    // ── WS Own Status ─────────────────────────────────────────────────
    function setWsStatus(state, label) {
        if (!wsBadge) return;
        wsBadge.innerHTML = '<span class="ws-dot ' + state + '"></span> ' + label;
        wsBadge.className = 'ws-status hidden lg:flex ' + (state==='connected' ? 'text-emerald-600' : state==='connecting' ? 'text-amber-600' : 'text-red-500');
    }

    // ── Partner Online Status ─────────────────────────────────────────
    function setPartnerOnline(online) {
        const cls = online ? 'connected' : 'offline';
        const txt = online ? 'Đang trực tuyến' : 'Không trực tuyến';
        const color = online ? 'text-emerald-600' : 'text-slate-400';
        if (partnerStatus) { partnerStatus.innerHTML = '<span class="ws-dot ' + cls + '"></span> ' + txt; partnerStatus.className = 'ws-status ' + color; }
        if (partnerDot) partnerDot.className = 'absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white ' + (online ? 'bg-emerald-400' : 'bg-slate-300');
        // Info panel status
        const dot = document.querySelector('#infoPanelStatus .ws-dot');
        const lbl = document.getElementById('infoPanelStatusLabel');
        if (dot) dot.className = 'ws-dot ' + cls;
        if (lbl) lbl.textContent = txt;
        // Sidebar dot
        const sdot = document.querySelector('.online-dot-' + sessionId);
        if (sdot) sdot.className = 'online-dot-' + sessionId + ' absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white ' + (online ? 'bg-emerald-400' : 'bg-slate-300');
    }

    function pollPartnerStatus() {
        fetch(CTX + '/api/chat?action=getOnlineStatus&sessionId=' + sessionId + '&userId=' + partnerUserId)
            .then(r => r.json())
            .then(resp => { if (resp.success && resp.data != null) setPartnerOnline(resp.data.online); })
            .catch(() => {});
    }

    // ── WebSocket ─────────────────────────────────────────────────────
    function connectWS() {
        const proto = location.protocol === 'https:' ? 'wss' : 'ws';
        ws = new WebSocket(proto + '://' + location.host + CTX + '/ws/chat/' + sessionId);
        setWsStatus('connecting', 'Đang kết nối...');

        ws.onopen = () => {
            wsReady = true;
            setWsStatus('connected', 'Kết nối');
            pollPartnerStatus();
            btnSend.disabled = false;
        };
        ws.onmessage = (e) => {
            try {
                const msg = JSON.parse(e.data);
                if (msg.error) { console.warn('WS err:', msg.error); return; }
                if (msg.messageId && !renderedIds.has(msg.messageId)) {
                    renderedIds.add(msg.messageId);
                    const isMine = msg.senderId === currentUserId;
                    
                    if (isMine) {
                        const optimisticEl = chatBox.querySelector('[data-msg-id^="tmp-"]');
                        if (optimisticEl) {
                            optimisticEl.dataset.msgId = msg.messageId;
                            setMsgStatus(optimisticEl, 'sent');
                            if (msg.mediaUrl) trackMedia(msg);
                            refreshSidebar();
                            return;
                        }
                    }

                    appendMessage(msg, isMine);
                    // Nếu là tin của đối tác → đánh dấu tin của mình là "Đã xem"
                    if (!isMine && lastMsgEl) setMsgStatus(lastMsgEl, 'seen');
                    if (!isMine) fetch(CTX + '/api/chat?action=markRead&sessionId=' + sessionId).catch(() => {});
                    if (msg.mediaUrl) trackMedia(msg);
                    refreshSidebar();
                }
            } catch(err) { console.error('WS parse err', err); }
        };
        ws.onclose = () => {
            wsReady = false;
            setWsStatus('disconnected', 'Mất kết nối');
            btnSend.disabled = true;
            setTimeout(connectWS, 3000);
        };
        ws.onerror = () => { setWsStatus('disconnected', 'Lỗi kết nối'); };
    }

    // ── Helpers ───────────────────────────────────────────────────────
    function formatTime(val) {
        if (!val) return '';
        let d;
        if (Array.isArray(val)) d = new Date(val[0], val[1]-1, val[2], val[3]||0, val[4]||0, val[5]||0);
        else d = new Date(val);
        if (isNaN(d.getTime())) return '';
        return d.toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'});
    }

    function createLinkifiedText(text) {
        const frag = document.createDocumentFragment();
        if (!text) return frag;

        const urlRe = /(https?:\/\/[^\s<>"]+)/g;
        let lastIndex = 0;
        let match;
        while ((match = urlRe.exec(text)) !== null) {
            if (match.index > lastIndex) {
                frag.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
            }

            const href = match[1];
            const a = document.createElement('a');
            a.href = href;
            a.target = '_blank';
            a.rel = 'noopener noreferrer';
            a.className = 'msg-link';
            a.textContent = href;
            frag.appendChild(a);
            lastIndex = urlRe.lastIndex;
        }

        if (lastIndex < text.length) {
            frag.appendChild(document.createTextNode(text.slice(lastIndex)));
        }

        return frag;
    }

    function isSafeMediaUrl(url) {
        if (!url) return false;
        try {
            const parsed = new URL(url, window.location.origin);
            return parsed.protocol === 'http:' || parsed.protocol === 'https:' || parsed.protocol === 'blob:'
                || parsed.origin === window.location.origin;
        } catch (err) {
            return false;
        }
    }

    // ── Message Status Ticks ──────────────────────────────────────────
    function createStatusEl(initial) {
        // initial: 'sending' | 'sent' | 'received' | 'seen'
        const el = document.createElement('div');
        el.className = 'msg-status';
        setMsgStatusInner(el, initial);
        return el;
    }
    function setMsgStatusInner(el, status) {
        const icons = { sending: '⏳', sent: '✓', received: '✓✓', seen: '✓✓' };
        const labels = { sending: 'Đang gửi', sent: 'Đã gửi', received: 'Đã nhận', seen: 'Đã xem' };
        el.innerHTML = '<span>' + (icons[status]||'') + '</span><span>' + (labels[status]||'') + '</span>';
        el.className = 'msg-status' + (status === 'seen' ? ' seen' : '');
    }
    function setMsgStatus(msgWrap, status) {
        const el = msgWrap.querySelector('.msg-status');
        if (el) setMsgStatusInner(el, status);
    }

    // ── Render Message ────────────────────────────────────────────────
    function appendMessage(msg, isMine, statusOverride) {
        const wrap = document.createElement('div');
        wrap.dataset.msgId = msg.messageId;
        wrap.className = 'flex gap-2 max-w-[80%] ' + (isMine ? 'self-end flex-row-reverse' : 'items-end');

        if (!isMine) {
            const av = document.createElement('div');
            av.className = 'w-7 h-7 rounded-full bg-[#b4f0c9] flex items-center justify-center text-[#175034] border border-white/50 shadow-sm shrink-0';
            av.innerHTML = '<span class="material-symbols-outlined text-sm">person</span>';
            wrap.appendChild(av);
        }

        const col = document.createElement('div');
        col.className = 'flex flex-col gap-0.5 ' + (isMine ? 'items-end' : 'items-start');

        // Time label
        const time = document.createElement('span');
        time.className = 'text-[10px] text-slate-400 ' + (isMine ? 'mr-1' : 'ml-1');
        time.textContent = (isMine ? 'Bạn' : 'Khách') + ' · ' + formatTime(msg.createdAt);
        col.appendChild(time);

        // Bubble
        const bubble = document.createElement('div');
        bubble.className = isMine
            ? 'bg-[#4d661c] text-white p-3 rounded-2xl rounded-br-sm shadow-md text-sm max-w-xs'
            : 'glass-panel bg-white/90 p-3 rounded-2xl rounded-bl-sm shadow-sm text-slate-800 text-sm max-w-xs';

        const safeMediaUrl = isSafeMediaUrl(msg.mediaUrl) ? msg.mediaUrl : null;
        if (safeMediaUrl) {
            const mw = document.createElement('div'); mw.className = 'mb-1';
            if (msg.mediaType === 'IMAGE') {
                const img = document.createElement('img');
                img.src = safeMediaUrl; img.alt = 'Hình ảnh';
                img.className = 'max-w-[200px] max-h-48 rounded-xl shadow-sm cursor-zoom-in block';
                img.loading = 'lazy';
                img.onclick = () => window.open(safeMediaUrl, '_blank', 'noopener,noreferrer');
                mw.appendChild(img);
            } else if (msg.mediaType === 'VIDEO') {
                const vid = document.createElement('video');
                vid.src = safeMediaUrl; vid.controls = true;
                vid.className = 'max-w-[200px] max-h-48 rounded-xl shadow-sm block';
                vid.preload = 'metadata';
                mw.appendChild(vid);
            }
            bubble.appendChild(mw);
        }
        if (msg.content) {
            const p = document.createElement('p');
            p.appendChild(createLinkifiedText(msg.content));
            bubble.appendChild(p);
        }
        col.appendChild(bubble);

        // Status (chỉ cho tin của mình)
        if (isMine) {
            const statusEl = createStatusEl(statusOverride || 'received');
            col.appendChild(statusEl);
        }

        wrap.appendChild(col);
        chatBox.appendChild(wrap);
        chatBox.scrollTop = chatBox.scrollHeight;
        if (isMine) lastMsgEl = wrap;
        return wrap;
    }

    // ── Load History ──────────────────────────────────────────────────
    function loadHistory() {
        fetch(CTX + '/api/chat?action=getMessages&sessionId=' + sessionId)
            .then(r => r.json())
            .then(resp => {
                // Remove skeleton
                skeletonLoader && skeletonLoader.remove();
                chatStart && chatStart.classList.remove('hidden');

                if (!resp.success || !resp.data) {
                    console.error('loadHistory: API error', resp.error);
                    return;
                }
                const { messages, currentUserId: apiUid } = resp.data;
                if (!messages || !Array.isArray(messages)) {
                    console.warn('loadHistory: messages is not an array', resp.data);
                    return;
                }
                messages.forEach(msg => {
                    if (!renderedIds.has(msg.messageId)) {
                        renderedIds.add(msg.messageId);
                        const isMine = msg.senderId === (apiUid || currentUserId);
                        appendMessage(msg, isMine, isMine ? 'received' : null);
                        if (msg.mediaUrl) trackMedia(msg);
                    }
                });
                chatBox.scrollTop = chatBox.scrollHeight;
            })
            .catch(err => {
                console.error('loadHistory error', err);
                skeletonLoader && skeletonLoader.remove();
                chatStart && chatStart.classList.remove('hidden');
            });
    }

    // ── Send ──────────────────────────────────────────────────────────
    function handleSend() {
        if (isUploading) return;
        const content = chatInput.value.trim();
        if (!content && !pendingMedia) return;

        const tempId = 'tmp-' + Date.now();
        const isMine = true;
        // Optimistic render
        const fakeMsg = { messageId: tempId, senderId: currentUserId, content, mediaUrl: pendingMedia?.url || null, mediaType: pendingMedia?.type || null, createdAt: new Date().toISOString() };
        const wrap = appendMessage(fakeMsg, true, 'sending');
        renderedIds.add(tempId);

        chatInput.value = '';
        chatInput.style.height = 'auto';

        const mediaUrl = pendingMedia?.url || null;
        const mediaType = pendingMedia?.type || null;
        resetUpload();

        if (wsReady && ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ content, mediaUrl, mediaType }));
            // WS sẽ broadcast lại → khi nhận được messageId thật từ server, thay thế optimistic
            setMsgStatus(wrap, 'sent');
            // Server broadcast sẽ gửi lại → check là của mình → skip vì đã render
        } else {
            // HTTP fallback
            const fd = new URLSearchParams({ action:'sendMessage', sessionId, content: content||'', mediaUrl: mediaUrl||'', mediaType: mediaType||'', _csrf: CSRF_TOKEN });
            fetch(CTX + '/api/chat', { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: fd.toString() })
                .then(r => r.json())
                .then(resp => {
                    if (resp.success) {
                        setMsgStatus(wrap, 'sent');
                        refreshSidebar();
                    } else {
                        alert(resp.error || resp.message || 'Gửi thất bại');
                        wrap.remove(); renderedIds.delete(tempId);
                    }
                })
                .catch(() => { wrap.remove(); renderedIds.delete(tempId); });
        }
        refreshSidebar();
    }

    // ── Media Upload ──────────────────────────────────────────────────
    document.getElementById('btnTriggerUpload').addEventListener('click', () => mediaInput.click());

    mediaInput.addEventListener('change', function() {
        const file = this.files[0];
        if (!file) return;

        const isImg = file.type.startsWith('image/');
        const isVid = file.type.startsWith('video/');

        if (isImg) {
            if (file.size > 5*1024*1024) { alert('Hình ảnh tối đa 5MB.'); this.value=''; return; }
            if (!file.name.toLowerCase().match(/\.(jpg|jpeg|png|gif|webp)$/)) { alert('Định dạng ảnh không hợp lệ.'); this.value=''; return; }
        } else if (isVid) {
            if (file.size > 50*1024*1024) { alert('Video tối đa 50MB.'); this.value=''; return; }
            if (!file.name.toLowerCase().match(/\.(mp4|webm|ogg)$/)) { alert('Định dạng video không hợp lệ.'); this.value=''; return; }
        } else { alert('Chỉ hỗ trợ ảnh và video.'); this.value=''; return; }

        previewFileName.textContent = file.name;
        previewStatus.textContent = 'Đang tải lên...';
        uploadPanel.classList.remove('hidden');
        progressOverlay.style.display = 'flex';
        progressOverlay.textContent = '0%';
        isUploading = true;
        btnSend.disabled = true;

        if (isImg) { imgPreview.src=URL.createObjectURL(file); imgPreview.classList.remove('hidden'); vidPreview.classList.add('hidden'); }
        else { vidPreview.src=URL.createObjectURL(file); vidPreview.classList.remove('hidden'); imgPreview.classList.add('hidden'); }

        const formData = new FormData();
        formData.append('file', file);
        // CSRF: append as form field AND header
        formData.append('_csrf', CSRF_TOKEN);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', CTX + '/api/chat/upload', true);
        xhr.setRequestHeader('X-CSRF-Token', CSRF_TOKEN);

        xhr.upload.onprogress = e => {
            if (e.lengthComputable) progressOverlay.textContent = Math.round(e.loaded/e.total*100) + '%';
        };
        xhr.onload = () => {
            isUploading = false;
            progressOverlay.style.display = 'none';
            if (xhr.status === 200) {
                try {
                    const resp = JSON.parse(xhr.responseText);
                    if (resp.success && resp.data) {
                        pendingMedia = { url: resp.data.url, type: resp.data.type };
                        previewStatus.textContent = 'Sẵn sàng gửi';
                        btnSend.disabled = false;
                    } else {
                        alert('Lỗi tải lên: ' + (resp.error || 'Không rõ'));
                        resetUpload();
                    }
                } catch(e) { alert('Lỗi phân tích phản hồi server.'); resetUpload(); }
            } else {
                alert('Tải lên thất bại (HTTP ' + xhr.status + '). Kiểm tra kích thước file và kết nối.');
                resetUpload();
            }
        };
        xhr.onerror = () => { isUploading=false; alert('Lỗi kết nối khi tải lên.'); resetUpload(); };
        xhr.send(formData);
    });

    function resetUpload() {
        mediaInput.value=''; pendingMedia=null; isUploading=false;
        uploadPanel.classList.add('hidden');
        imgPreview.classList.add('hidden'); imgPreview.src='';
        vidPreview.classList.add('hidden'); vidPreview.src='';
        previewStatus.textContent='';
        progressOverlay.style.display='none';
        if (!isUploading) btnSend.disabled = !wsReady && !pendingMedia;
    }

    document.getElementById('btnCancelUpload').addEventListener('click', resetUpload);
    btnSend.addEventListener('click', handleSend);
    chatInput.addEventListener('keydown', e => { if (e.key==='Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); } });
    chatInput.addEventListener('input', () => { chatInput.style.height='auto'; chatInput.style.height=Math.min(chatInput.scrollHeight,128)+'px'; });

    // ── Shared Media Grid ─────────────────────────────────────────────
    function trackMedia(msg) {
        if (!msg.mediaUrl) return;
        sharedMedia.push(msg);
        renderSharedMedia();
    }
    function renderSharedMedia() {
        if (!sharedMediaGrid) return;
        const safeItems = sharedMedia.filter(m => isSafeMediaUrl(m.mediaUrl));
        if (safeItems.length === 0) { sharedMediaGrid.innerHTML='<p class="col-span-3 text-xs text-slate-400 text-center py-4">Chưa có ảnh/video</p>'; return; }
        sharedMediaGrid.innerHTML='';
        safeItems.slice(-9).reverse().forEach(m => {
            const cell = document.createElement('div');
            cell.className = 'aspect-square rounded-lg overflow-hidden bg-slate-200 cursor-pointer hover:opacity-80 transition-opacity';
            const safeMediaUrl = m.mediaUrl;
            if (m.mediaType === 'IMAGE') {
                const img = document.createElement('img');
                img.src = safeMediaUrl;
                img.className = 'w-full h-full object-cover';
                img.loading = 'lazy';
                img.alt = 'Ảnh chia sẻ';
                img.addEventListener('click', () => window.open(safeMediaUrl, '_blank', 'noopener,noreferrer'));
                cell.appendChild(img);
            } else {
                const videoWrap = document.createElement('div');
                videoWrap.className = 'w-full h-full flex items-center justify-center bg-slate-700 text-white';
                videoWrap.addEventListener('click', () => window.open(safeMediaUrl, '_blank', 'noopener,noreferrer'));
                const icon = document.createElement('span');
                icon.className = 'material-symbols-outlined text-2xl';
                icon.textContent = 'play_circle';
                videoWrap.appendChild(icon);
                cell.appendChild(videoWrap);
            }
            sharedMediaGrid.appendChild(cell);
        });
    }

    // ── Sidebar polling ───────────────────────────────────────────────
    function refreshSidebar() {
        fetch(CTX + '/api/chat?action=getSessions')
            .then(r => r.json())
            .then(resp => {
                if (!resp.success || !resp.data || !resp.data.sessions) return;
                resp.data.sessions.forEach(s => {
                    const badge = document.querySelector('.unread-badge-' + s.sessionId);
                    if (badge) {
                        if (s.sessionId === sessionId) {
                            badge.style.display = 'none';
                        } else if (s.unreadCount > 0) {
                            badge.textContent = s.unreadCount > 99 ? '99+' : s.unreadCount;
                            badge.style.display = '';
                        } else {
                            badge.style.display = 'none';
                        }
                    }
                    const prev = document.querySelector('.last-msg-' + s.sessionId);
                    if (prev && s.lastMessage) {
                        prev.textContent = s.lastMessageType==='IMAGE' ? '📷 Hình ảnh' : s.lastMessageType==='VIDEO' ? '🎥 Video' : (s.lastMessage.length>35 ? s.lastMessage.substring(0,35)+'…' : s.lastMessage);
                    }
                });
            }).catch(() => {});
    }

    // ── Init ──────────────────────────────────────────────────────────
    loadHistory();
    connectWS();
    pollPartnerStatus();
    setInterval(pollPartnerStatus, 15000);
    setInterval(refreshSidebar, 30000);
})();
</script>
</c:if>

<script>
    // Search sidebar
    const _si = document.getElementById('searchSessions');
    if (_si) _si.addEventListener('input', function() {
        const q = this.value.toLowerCase();
        document.querySelectorAll('#sessionList a').forEach(el => {
            el.style.display = (el.dataset.name||'').toLowerCase().includes(q) ? '' : 'none';
        });
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
