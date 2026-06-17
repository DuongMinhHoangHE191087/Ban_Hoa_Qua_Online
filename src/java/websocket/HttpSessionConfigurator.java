package websocket;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * HttpSessionConfigurator — Truyền HttpSession từ HTTP handshake vào WebSocket Session.
 *
 * Tomcat 10 hỗ trợ truy cập HttpSession trong WS handshake qua HandshakeRequest.
 * Sau đó lưu vào userProperties của ServerEndpointConfig để ChatEndpoint đọc.
 *
 * @author fruitmkt-team
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    public static final String HTTP_SESSION_KEY = "httpSession";

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
            config.getUserProperties().put(HTTP_SESSION_KEY, httpSession);
        }
    }
}
