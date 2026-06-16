<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Quản lý Chat (Admin)" />
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

<style>
    body { font-family: 'Lexend', sans-serif; }
    .glass-panel {
        background-color: rgba(255, 255, 255, 0.7);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.4);
    }
    .chat-layout { height: calc(100vh - 120px); min-height: 550px; }
    ::-webkit-scrollbar { width: 6px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
    ::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
    .session-item.active { background: rgba(224, 231, 255, 0.7); border-color: #3730a3; }
    
    .ws-status { display: inline-flex; align-items: center; gap: 4px; font-size: 11px; font-weight: 500; }
    .ws-dot { width: 8px; height: 8px; border-radius: 50%; }
    .ws-dot.connected { background: #22c55e; animation: pulse 2s infinite; }
    .ws-dot.connecting { background: #f59e0b; animation: pulse 1s infinite; }
    .ws-dot.disconnected { background: #ef4444; }
    @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.4} }
    
    .badge-shop  { background: #dcfce7; color: #166534; }
    .badge-admin { background: #e0e7ff; color: #3730a3; }
</style>

<main class="flex-1 overflow-hidden bg-slate-50 text-slate-800 flex chat-layout relative" style="background-image: radial-gradient(circle at top right, rgba(99, 102, 241, 0.08), transparent 40%);">
    
    <!-- Left Column: Chat List -->
    <aside class="w-full md:w-[320px] lg:w-[360px] flex-shrink-0 flex flex-col border-r border-slate-200 glass-panel bg-white/40 relative z-10 hidden md:flex">
        <!-- Header & Search -->
        <div class="p-4 border-b border-slate-200">
            <h1 class="font-headline-md text-xl font-bold text-indigo-700 mb-3 flex items-center gap-2">
                <span class="material-symbols-outlined">support_agent</span> Quản lý Hỗ trợ
            </h1>
            <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">search</span>
                <input id="searchSessions" class="w-full pl-10 pr-4 py-2 bg-white/50 border border-slate-200 rounded-xl focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 text-sm transition-colors placeholder:text-slate-400 shadow-sm" placeholder="Tìm kiếm phiên hỗ trợ..." type="text">
            </div>
        </div>
        
        <!-- Chat List Items -->
        <div id="sessionList" class="flex-1 overflow-y-auto p-2 space-y-1">
            <c:choose>
                <c:when test="${empty chatSessions}">
                    <div class="p-8 text-center text-slate-400">
                        <span class="material-symbols-outlined text-4xl mb-2">forum</span>
                        <p class="text-sm">Chưa có phiên chat hỗ trợ nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="session" items="${chatSessions}">
                        <a href="${pageContext.request.contextPath}/admin/chat?sessionId=${session.sessionId}"
                           class="session-item flex items-start gap-3 p-3.5 rounded-xl hover:bg-white/45 border border-transparent transition-all ${session.sessionId == activeSessionId ? 'active shadow-sm' : 'bg-white/20'}"
                           data-name="${session.partnerName}">
                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center shrink-0 text-indigo-600 border border-indigo-200 shadow-sm">
                                <c:choose>
                                    <c:when test="${session.sessionType == 'ADMIN'}">
                                        <span class="material-symbols-outlined text-base">support_agent</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="material-symbols-outlined text-base">storefront</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex justify-between items-start mb-1">
                                    <h3 class="font-semibold text-slate-800 text-sm truncate pr-2">
                                        <c:choose>
                                            <c:when test="${not empty session.partnerName}">${session.partnerName}</c:when>
                                            <c:otherwise>Phiên #${session.sessionId}</c:otherwise>
                                        </c:choose>
                                    </h3>
                                    <span class="text-[9px] text-slate-400 shrink-0">
                                        <fmt:formatDate value="${session.updatedAtAsDate}" pattern="dd/MM HH:mm"/>
                                    </span>
                                </div>
                                <span class="text-[9px] px-2 py-0.5 rounded-full font-semibold ${session.sessionType == 'ADMIN' ? 'badge-admin' : 'badge-shop'}">
                                    ${session.sessionType == 'ADMIN' ? 'Hỗ trợ Admin' : 'Shop'}
                                </span>
                            </div>
                        </a>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </aside>
    
    <!-- Right Column: Active Chat Window -->
    <section class="flex-1 flex flex-col relative bg-white bg-opacity-40">
        <c:choose>
            <c:when test="${activeSessionId > 0}">
                <!-- Chat Header -->
                <div class="px-4 py-3 border-b border-slate-200 glass-panel bg-white/70 flex justify-between items-center sticky top-0 z-10 shadow-sm">
                    <div class="flex items-center gap-3">
                        <div class="md:hidden">
                            <a href="${pageContext.request.contextPath}/admin/chat" class="p-2 text-slate-500 hover:bg-slate-100 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">arrow_back</span>
                            </a>
                        </div>
                        <div class="w-9 h-9 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 border border-indigo-200 shadow-sm">
                            <span class="material-symbols-outlined text-base">support_agent</span>
                        </div>
                        <div>
                            <h2 class="font-label-md text-sm font-bold text-slate-800">Phiên hỗ trợ #${activeSessionId}</h2>
                            <div id="wsStatusBadge" class="ws-status text-amber-600">
                                <span class="ws-dot connecting"></span> Đang kết nối...
                            </div>
                        </div>
                    </div>
                    <div class="text-xs text-indigo-600 bg-indigo-50 px-3 py-1 rounded-full font-medium border border-indigo-100">
                        🔐 Chế độ Admin — Toàn quyền hỗ trợ
                    </div>
                </div>
                
                <!-- Messages Area -->
                <div id="chatBox" class="flex-1 overflow-y-auto p-4 space-y-4 flex flex-col">
                    <div class="flex justify-center">
                        <span class="text-[11px] font-label-sm bg-white border border-slate-200 text-slate-400 px-3 py-1 rounded-full shadow-sm">Lịch sử cuộc trò chuyện</span>
                    </div>
                </div>

                <!-- Media Preview Panel -->
                <div id="uploadPreviewPanel" class="hidden px-4 py-2 bg-slate-50 border-t border-slate-200 flex items-center gap-3">
                    <div class="relative w-16 h-16 rounded-lg border border-slate-300 overflow-hidden bg-black flex items-center justify-center">
                        <img id="imagePreview" class="hidden w-full h-full object-cover" />
                        <video id="videoPreview" class="hidden w-full h-full object-cover" /></video>
                        <div id="uploadProgressOverlay" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white text-xs font-bold">0%</div>
                    </div>
                    <div class="flex-1 min-w-0">
                        <p class="text-xs font-semibold text-slate-700 truncate" id="previewFileName">filename.jpg</p>
                        <p class="text-[10px] text-slate-400">Đang chuẩn bị gửi...</p>
                    </div>
                    <button id="btnCancelUpload" class="p-1 hover:bg-slate-200 text-slate-500 rounded-full transition-colors">
                        <span class="material-symbols-outlined text-lg">close</span>
                    </button>
                </div>
                
                <!-- Input Area -->
                <div class="p-3 bg-white/60 backdrop-blur-lg border-t border-slate-200 shadow-sm z-10">
                    <div class="flex items-end gap-2 bg-slate-50 border border-slate-200 p-2 rounded-2xl shadow-inner focus-within:border-indigo-500">
                        <!-- Input file ẩn -->
                        <input type="file" id="mediaInput" accept="image/*,video/*" class="hidden">
                        
                        <button type="button" id="btnTriggerUpload" class="p-2 text-slate-500 hover:text-indigo-600 transition-colors rounded-full hover:bg-white/40 flex items-center justify-center flex-shrink-0" title="Tải ảnh/video">
                            <span class="material-symbols-outlined text-xl">image</span>
                        </button>
                        
                        <textarea id="chatInput" class="flex-1 bg-transparent border-none resize-none focus:ring-0 text-slate-800 placeholder:text-slate-400 font-body-md text-sm py-2 px-1 max-h-32 min-h-[40px] border-transparent focus:border-transparent focus:outline-none" placeholder="Nhập câu trả lời hỗ trợ..." rows="1"></textarea>
                        
                        <button type="button" id="btnSendMessage" class="p-2 bg-indigo-600 text-white hover:bg-indigo-700 transition-colors rounded-full flex items-center justify-center shadow-sm flex-shrink-0 mb-0.5">
                            <span class="material-symbols-outlined text-xl" style="font-variation-settings: 'FILL' 1;">send</span>
                        </button>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-50 p-8">
                    <div class="w-24 h-24 bg-indigo-50 rounded-full flex items-center justify-center mb-4 border border-indigo-100 shadow-inner">
                        <span class="material-symbols-outlined text-5xl text-indigo-400/60">support_agent</span>
                    </div>
                    <h3 class="text-base font-bold text-indigo-700 mb-1">Chọn phiên hỗ trợ</h3>
                    <p class="text-xs text-slate-500">Chọn một cuộc trò chuyện hỗ trợ khách hàng ở danh sách bên trái để bắt đầu</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<c:if test="${activeSessionId > 0}">
<script>
    const CTX = '${pageContext.request.contextPath}';
    const sessionId = parseInt('${activeSessionId}');
    const currentAdminId = parseInt('${adminId}');
    const CSRF_TOKEN = '${sessionScope._csrfToken}';
    
    const chatBox = document.getElementById('chatBox');
    const chatInput = document.getElementById('chatInput');
    const btnSendMessage = document.getElementById('btnSendMessage');
    const wsBadge = document.getElementById('wsStatusBadge');
    
    // Tải tệp lên
    const btnTriggerUpload = document.getElementById('btnTriggerUpload');
    const mediaInput = document.getElementById('mediaInput');
    const uploadPreviewPanel = document.getElementById('uploadPreviewPanel');
    const imagePreview = document.getElementById('imagePreview');
    const videoPreview = document.getElementById('videoPreview');
    const previewFileName = document.getElementById('previewFileName');
    const uploadProgressOverlay = document.getElementById('uploadProgressOverlay');
    const btnCancelUpload = document.getElementById('btnCancelUpload');
    
    let ws = null;
    let wsReady = false;
    let renderedIds = new Set();
    let pendingMediaUrl = null;
    let pendingMediaType = null;
    let isUploading = false;

    function setStatus(state, label) {
        if (!wsBadge) return;
        wsBadge.innerHTML = '<span class="ws-dot ' + state + '"></span> ' + label;
        wsBadge.className = 'ws-status ' + (state === 'connected' ? 'text-emerald-600' : state === 'connecting' ? 'text-amber-600' : 'text-red-500');
    }

    function connectWS() {
        const proto = location.protocol === 'https:' ? 'wss' : 'ws';
        const wsUrl = proto + '://' + location.host + CTX + '/ws/chat/' + sessionId;
        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            wsReady = true;
            setStatus('connected', 'Kết nối thành công');
        };

        ws.onmessage = (e) => {
            try {
                const msg = JSON.parse(e.data);
                if (msg.error) { console.warn('WS error:', msg.error); return; }
                if (msg.messageId && !renderedIds.has(msg.messageId)) {
                    renderedIds.add(msg.messageId);
                    const isMine = (msg.senderId === currentAdminId);
                    chatBox.appendChild(renderMessage(msg, isMine));
                    chatBox.scrollTop = chatBox.scrollHeight;
                }
            } catch(err) { console.error('WS parse error', err); }
        };

        ws.onclose = () => {
            wsReady = false;
            setStatus('disconnected', 'Mất kết nối — đang thử lại...');
            setTimeout(connectWS, 3000);
        };

        ws.onerror = () => {
            setStatus('disconnected', 'Lỗi kết nối');
        };
    }

    function formatTime(val) {
        if (!val) return '';
        let d = Array.isArray(val)
            ? new Date(val[0], val[1]-1, val[2], val[3]||0, val[4]||0, val[5]||0)
            : new Date(val);
        return d.toLocaleTimeString('vi-VN', { hour:'2-digit', minute:'2-digit' });
    }

    function renderMessage(msg, isMine) {
        const wrap = document.createElement('div');
        wrap.className = 'flex gap-3 max-w-[80%] ' + (isMine ? 'self-end flex-row-reverse' : 'items-start');

        // Avatar
        if (!isMine) {
            const avatar = document.createElement('div');
            avatar.className = 'w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 border border-white/50 shadow-sm shrink-0 mt-auto';
            avatar.innerHTML = '<span class="material-symbols-outlined text-sm">person</span>';
            wrap.appendChild(avatar);
        }

        const container = document.createElement('div');
        container.className = 'flex flex-col gap-1 ' + (isMine ? 'items-end' : 'items-start');

        // Thời gian gửi
        const info = document.createElement('span');
        info.className = 'text-[10px] text-slate-400 ' + (isMine ? 'mr-1' : 'ml-1');
        info.textContent = (isMine ? 'Bạn (Admin)' : 'Đối tác') + ', ' + formatTime(msg.createdAt);
        container.appendChild(info);

        // Nội dung tin nhắn
        const contentBox = document.createElement('div');
        
        if (isMine) {
            contentBox.className = 'bg-indigo-600 text-white p-3 rounded-2xl rounded-br-sm shadow-md text-sm border border-white/10';
        } else {
            contentBox.className = 'glass-panel bg-white/90 p-3 rounded-2xl rounded-bl-sm shadow-sm text-slate-800 text-sm border border-white/60';
        }

        if (msg.mediaUrl) {
            const mediaWrap = document.createElement('div');
            mediaWrap.className = 'mb-1';
            
            if (msg.mediaType === 'IMAGE') {
                const img = document.createElement('img');
                img.src = msg.mediaUrl;
                img.className = 'max-w-xs max-h-48 rounded-lg shadow-sm cursor-zoom-in';
                img.onclick = () => window.open(msg.mediaUrl, '_blank');
                mediaWrap.appendChild(img);
            } else if (msg.mediaType === 'VIDEO') {
                const vid = document.createElement('video');
                vid.src = msg.mediaUrl;
                vid.controls = true;
                vid.className = 'max-w-xs max-h-48 rounded-lg shadow-sm';
                mediaWrap.appendChild(vid);
            }
            contentBox.appendChild(mediaWrap);
        }

        if (msg.content) {
            const textNode = document.createElement('p');
            textNode.textContent = msg.content;
            contentBox.appendChild(textNode);
        }

        container.appendChild(contentBox);
        wrap.appendChild(container);
        return wrap;
    }

    function loadHistory() {
        fetch(CTX + '/api/chat?action=getMessages&sessionId=' + sessionId)
            .then(r => r.json())
            .then(data => {
                if (!data.success) return;
                data.messages.forEach(msg => {
                    if (!renderedIds.has(msg.messageId)) {
                        renderedIds.add(msg.messageId);
                        chatBox.appendChild(renderMessage(msg, msg.senderId === currentAdminId));
                    }
                });
                chatBox.scrollTop = chatBox.scrollHeight;
            })
            .catch(err => console.error('loadHistory error', err));
    }

    function handleSend() {
        if (isUploading) return;
        const content = chatInput.value.trim();
        if (!content && !pendingMediaUrl) return;

        chatInput.value = '';

        if (wsReady && ws && ws.readyState === WebSocket.OPEN) {
            const payload = {
                content: content,
                mediaUrl: pendingMediaUrl,
                mediaType: pendingMediaType
            };
            ws.send(JSON.stringify(payload));
        } else {
            const fd = new URLSearchParams({
                action: 'sendMessage',
                sessionId: sessionId,
                content: content,
                mediaUrl: pendingMediaUrl || '',
                mediaType: pendingMediaType || '',
                _csrf: CSRF_TOKEN
            });
            fetch(CTX + '/api/chat', { 
                method: 'POST', 
                headers: {'Content-Type':'application/x-www-form-urlencoded'}, 
                body: fd.toString() 
            }).then(r => r.json()).then(data => {
                if (!data.success) alert(data.message);
                else loadHistory();
            });
        }
        resetUpload();
    }

    btnTriggerUpload.addEventListener('click', () => mediaInput.click());
    
    mediaInput.addEventListener('change', function() {
        const file = this.files[0];
        if (!file) return;

        const isImage = file.type.startsWith('image/');
        const isVideo = file.type.startsWith('video/');

        // Client-side file size and format validation
        if (isImage) {
            if (file.size > 5 * 1024 * 1024) {
                alert('Hình ảnh không được vượt quá 5MB.');
                this.value = '';
                return;
            }
            if (!file.name.toLowerCase().match(/\.(jpg|jpeg|png|gif|webp)$/)) {
                alert('Định dạng ảnh không hợp lệ (chỉ cho phép JPG, PNG, GIF, WEBP).');
                this.value = '';
                return;
            }
        } else if (isVideo) {
            if (file.size > 50 * 1024 * 1024) {
                alert('Video không được vượt quá 50MB.');
                this.value = '';
                return;
            }
            if (!file.name.toLowerCase().match(/\.(mp4|webm|ogg)$/)) {
                alert('Định dạng video không hợp lệ (chỉ cho phép MP4, WEBM, OGG).');
                this.value = '';
                return;
            }
        } else {
            alert('Định dạng không được hỗ trợ. Chỉ cho phép tải lên hình ảnh và video.');
            this.value = '';
            return;
        }

        previewFileName.textContent = file.name;
        uploadPreviewPanel.classList.remove('hidden');
        uploadProgressOverlay.classList.remove('hidden');
        uploadProgressOverlay.textContent = '0%';
        isUploading = true;

        if (isImage) {
            imagePreview.src = URL.createObjectURL(file);
            imagePreview.classList.remove('hidden');
            videoPreview.classList.add('hidden');
        } else if (isVideo) {
            videoPreview.src = URL.createObjectURL(file);
            videoPreview.classList.remove('hidden');
            imagePreview.classList.add('hidden');
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('_csrf', CSRF_TOKEN);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', CTX + '/api/chat/upload', true);
        xhr.setRequestHeader('X-CSRF-Token', CSRF_TOKEN);

        xhr.upload.onprogress = function(e) {
            if (e.lengthComputable) {
                const percent = Math.round((e.loaded / e.total) * 100);
                uploadProgressOverlay.textContent = percent + '%';
            }
        };

        xhr.onload = function() {
            isUploading = false;
            uploadProgressOverlay.classList.add('hidden');
            if (xhr.status === 200) {
                try {
                    const resp = JSON.parse(xhr.responseText);
                    if (resp.success) {
                        pendingMediaUrl = resp.url;
                        pendingMediaType = resp.type;
                    } else {
                        alert('Lỗi tải tệp: ' + resp.message);
                        resetUpload();
                    }
                } catch(e) {
                    alert('Lỗi phản hồi từ server.');
                    resetUpload();
                }
            } else {
                alert('Tải lên thất bại.');
                resetUpload();
            }
        };

        xhr.onerror = function() {
            isUploading = false;
            alert('Lỗi kết nối khi tải lên.');
            resetUpload();
        };

        xhr.send(formData);
    });

    function resetUpload() {
        mediaInput.value = '';
        pendingMediaUrl = null;
        pendingMediaType = null;
        isUploading = false;
        uploadPreviewPanel.classList.add('hidden');
        imagePreview.classList.add('hidden');
        imagePreview.src = '';
        videoPreview.classList.add('hidden');
        videoPreview.src = '';
    }

    btnCancelUpload.addEventListener('click', resetUpload);

    btnSendMessage.addEventListener('click', handleSend);
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    });

    loadHistory();
    connectWS();
</script>
</c:if>

<script>
    // Search filter cho Sidebar
    const searchInput = document.getElementById('searchSessions');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const q = this.value.toLowerCase();
            document.querySelectorAll('#sessionList a').forEach(el => {
                const name = (el.dataset.name || '').toLowerCase();
                el.style.display = name.includes(q) ? '' : 'none';
            });
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
