<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Quản lý Tin nhắn (Shop)" />
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms"></script>
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

<style>
    body { font-family: 'Lexend', sans-serif; background: #f8fafc; }
    .chat-container { height: calc(100vh - 180px); min-height: 500px; }
    .scrollbar-hide::-webkit-scrollbar { display: none; }
    .scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
    .msg-bubble { max-width: 75%; border-radius: 1.25rem; padding: 10px 16px; word-wrap: break-word; }
    .msg-mine { background: #4d661c; color: white; border-bottom-right-radius: 4px; }
    .msg-other { background: #e2ece7; color: #0f172a; border-bottom-left-radius: 4px; }
</style>

<main class="max-w-7xl mx-auto px-4 py-8">
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden flex chat-container">
        
        <!-- Sidebar -->
        <div class="w-1/3 border-r border-slate-200 bg-slate-50 flex flex-col hidden md:flex">
            <div class="p-4 border-b border-slate-200 bg-white">
                <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                    <span class="material-symbols-outlined text-[#4d661c]">forum</span> Khách hàng
                </h2>
            </div>
            <div class="flex-1 overflow-y-auto scrollbar-hide">
                <c:choose>
                    <c:when test="${empty chatSessions}">
                        <div class="p-8 text-center text-slate-400">
                            <span class="material-symbols-outlined text-4xl mb-2">inbox</span>
                            <p class="text-sm">Chưa có khách hàng nào nhắn tin.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="session" items="${chatSessions}">
                            <a href="${pageContext.request.contextPath}/shop/chat?sessionId=${session.sessionId}" 
                               class="flex items-center gap-4 p-4 border-b border-slate-100 transition-colors ${session.sessionId == activeSessionId ? 'bg-emerald-50 border-emerald-100' : 'hover:bg-slate-100 bg-white'}">
                                <div class="w-12 h-12 rounded-full bg-slate-200 flex items-center justify-center shrink-0 border border-slate-300">
                                    <span class="material-symbols-outlined text-slate-500">person</span>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-center mb-1">
                                        <h3 class="font-bold text-slate-800 truncate">Khách #${session.customerId}</h3>
                                        <span class="text-[10px] text-slate-400"><fmt:formatDate value="${session.updatedAt}" pattern="dd/MM HH:mm"/></span>
                                    </div>
                                    <p class="text-xs text-slate-500 truncate">Bấm để xem tin nhắn</p>
                                </div>
                            </a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Chat Area -->
        <div class="flex-1 flex flex-col bg-[#f4fbf7]">
            <c:choose>
                <c:when test="${activeSessionId > 0}">
                    <!-- Chat Header -->
                    <div class="p-4 bg-white border-b border-slate-200 flex items-center justify-between shadow-sm z-10">
                        <div class="flex items-center gap-3">
                            <div class="md:hidden">
                                <a href="${pageContext.request.contextPath}/shop/chat" class="p-2 text-slate-500 hover:bg-slate-100 rounded-full flex items-center justify-center">
                                    <span class="material-symbols-outlined">arrow_back</span>
                                </a>
                            </div>
                            <div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600 border border-emerald-200">
                                <span class="material-symbols-outlined">person</span>
                            </div>
                            <div>
                                <h3 class="font-bold text-slate-800">Trò chuyện với khách hàng</h3>
                                <div class="flex items-center gap-1 text-xs text-emerald-600 font-medium">
                                    <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span> Đang trực tuyến
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chat Messages -->
                    <div id="chatBox" class="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
                        <div class="text-center my-4">
                            <span class="text-xs font-semibold text-slate-400 bg-slate-100 px-3 py-1 rounded-full border border-slate-200">Bắt đầu cuộc trò chuyện</span>
                        </div>
                        <!-- Messages loaded via AJAX -->
                    </div>

                    <!-- Chat Input -->
                    <div class="p-4 bg-white border-t border-slate-200">
                        <form id="chatForm" class="flex gap-2">
                            <input type="text" id="chatInput" autocomplete="off" placeholder="Nhập tin nhắn hỗ trợ khách hàng..." 
                                   class="flex-1 bg-slate-50 border border-slate-300 text-sm rounded-full px-5 py-3 focus:outline-none focus:border-[#4d661c] focus:ring-1 focus:ring-[#4d661c] transition-all">
                            <button type="submit" class="bg-[#4d661c] hover:bg-[#364e03] text-white w-12 h-12 rounded-full flex items-center justify-center shadow transition-transform active:scale-95">
                                <span class="material-symbols-outlined text-xl ml-1">send</span>
                            </button>
                        </form>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="flex-1 flex flex-col items-center justify-center text-slate-400">
                        <div class="w-24 h-24 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                            <span class="material-symbols-outlined text-4xl text-slate-300">chat_bubble</span>
                        </div>
                        <h3 class="text-lg font-bold text-slate-500 mb-2">Chưa chọn đoạn chat</h3>
                        <p class="text-sm">Chọn một đoạn chat của khách hàng để trả lời</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<c:if test="${activeSessionId > 0}">
<script>
    const sessionId = ${activeSessionId};
    const chatBox = document.getElementById('chatBox');
    const chatForm = document.getElementById('chatForm');
    const chatInput = document.getElementById('chatInput');
    let lastMessageCount = 0;
    
    function formatTime(dateStr) {
        if (!dateStr) return '';
        let date;
        if (Array.isArray(dateStr)) {
            date = new Date(dateStr[0], dateStr[1]-1, dateStr[2], dateStr[3], dateStr[4], dateStr[5] || 0);
        } else {
            date = new Date(dateStr);
        }
        return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }

    function renderMessage(msg, isMine) {
        const div = document.createElement('div');
        div.className = 'flex flex-col ' + (isMine ? 'items-end' : 'items-start');
        
        const bubble = document.createElement('div');
        bubble.className = 'msg-bubble text-sm shadow-sm ' + (isMine ? 'msg-mine' : 'msg-other');
        bubble.textContent = msg.content;
        
        const time = document.createElement('span');
        time.className = 'text-[10px] text-slate-400 mt-1 mx-1 font-medium';
        time.textContent = formatTime(msg.createdAt);
        
        div.appendChild(bubble);
        div.appendChild(time);
        return div;
    }

    function fetchMessages() {
        fetch('${pageContext.request.contextPath}/api/chat?action=getMessages&sessionId=' + sessionId)
            .then(res => res.json())
            .then(data => {
                if (data.success && data.messages.length > lastMessageCount) {
                    chatBox.innerHTML = '<div class="text-center my-4"><span class="text-xs font-semibold text-slate-400 bg-slate-100 px-3 py-1 rounded-full border border-slate-200">Bắt đầu cuộc trò chuyện</span></div>';
                    
                    data.messages.forEach(msg => {
                        const isMine = (msg.senderId === data.currentUserId);
                        chatBox.appendChild(renderMessage(msg, isMine));
                    });
                    
                    chatBox.scrollTop = chatBox.scrollHeight;
                    lastMessageCount = data.messages.length;
                }
            })
            .catch(err => console.error("Chat polling error", err));
    }

    chatForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const content = chatInput.value.trim();
        if (!content) return;
        
        chatInput.value = '';
        
        // Optimistic UI update
        const tempMsg = { content: content, createdAt: new Date(), senderId: -1 };
        chatBox.appendChild(renderMessage(tempMsg, true));
        chatBox.scrollTop = chatBox.scrollHeight;

        const formData = new URLSearchParams();
        formData.append('action', 'sendMessage');
        formData.append('sessionId', sessionId);
        formData.append('content', content);

        fetch('${pageContext.request.contextPath}/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        }).then(res => res.json()).then(data => {
            if (data.success) {
                lastMessageCount = 0; // force re-render on next poll
                fetchMessages();
            } else {
                alert(data.message);
            }
        });
    });

    // Initial fetch and polling
    fetchMessages();
    setInterval(fetchMessages, 3000); // Poll every 3 seconds
</script>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
