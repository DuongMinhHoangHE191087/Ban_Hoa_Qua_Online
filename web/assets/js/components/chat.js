/**
 * chat.js — WebSocket client cho tính năng Chat real-time.
 *
 * ENDPOINT: ws://<host>/<contextPath>/chat-ws
 * QUERY PARAMS: ?sessionId=<int>&userId=<int>
 *
 * SERVER SIDE: Cần tạo ChatWebSocketEndpoint.java với @ServerEndpoint
 * Package: com.fruitmkt.servlet.api.ChatWebSocketEndpoint
 *
 * CÁCH DÙNG TRONG JSP:
 * <script src="${ctx}/assets/js/components/chat.js"></script>
 * <script>
 *   FruitChat.init({
 *     sessionId: ${sessionId},
 *     userId: ${currentUser.userId},
 *     contextPath: '${pageContext.request.contextPath}'
 *   });
 * </script>
 *
 * @author fruitmkt-team
 */
const FruitChat = {
    ws: null,
    config: {},

    /**
     * Khởi tạo WebSocket connection.
     * @param {{ sessionId: number, userId: number, contextPath: string }} cfg
     */
    init(cfg) {
        this.config = cfg;
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const url = `${protocol}//${location.host}${cfg.contextPath}/chat-ws?sessionId=${cfg.sessionId}&userId=${cfg.userId}`;

        this.ws = new WebSocket(url);

        this.ws.onopen    = ()      => this._onOpen();
        this.ws.onmessage = (event) => this._onMessage(event);
        this.ws.onerror   = (err)   => console.error('[Chat] WS Error:', err);
        this.ws.onclose   = ()      => this._onClose();
    },

    /**
     * Gửi tin nhắn.
     * @param {string} content Nội dung tin nhắn
     */
    send(content) {
        if (!content || !content.trim()) return;
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[Chat] WS not connected');
            return;
        }
        // TODO: Định nghĩa message format với backend
        const msg = JSON.stringify({ type: 'MESSAGE', content: content.trim() });
        this.ws.send(msg);
    },

    _onOpen() {
        console.log('[Chat] Connected to session', this.config.sessionId);
        // TODO: Load tin nhắn cũ bằng AJAX nếu cần
    },

    _onMessage(event) {
        const msg = JSON.parse(event.data);
        // TODO: Render tin nhắn vào UI
        // msg format: { type: 'MESSAGE'|'TYPING'|'READ', senderId, content, createdAt }
        this._renderMessage(msg);
    },

    _onClose() {
        console.log('[Chat] Disconnected. Reconnect in 3s...');
        setTimeout(() => this.init(this.config), 3000); // Auto-reconnect
    },

    _renderMessage(msg) {
        const container = document.getElementById('chat-messages');
        if (!container) return;
        const div = document.createElement('div');
        div.className = `chat-message ${msg.senderId === this.config.userId ? 'sent' : 'received'}`;
        div.innerHTML = `<p>${_escapeHtml(msg.content)}</p><small>${msg.createdAt}</small>`;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
    }
};

function _escapeHtml(str) {
    const d = document.createElement('div');
    d.appendChild(document.createTextNode(str));
    return d.innerHTML;
}
