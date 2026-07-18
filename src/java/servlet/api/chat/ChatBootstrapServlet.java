package servlet.api.chat;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.entity.auth.User;
import model.response.ApiResponse;
import service.chat.ChatReadService;
import util.JsonUtil;
import util.SessionUtil;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/api/chat/bootstrap")
public class ChatBootstrapServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ChatBootstrapServlet.class.getName());

    private final ChatReadService chatReadService = new ChatReadService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            User currentUser = SessionUtil.getCurrentUser(request.getSession());
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
                return;
            }

            Integer requestedSessionId = parseOptionalInt(request.getParameter("sessionId"));
            Integer requestedLimit = parseOptionalInt(request.getParameter("limit"));
            Map<String, Object> payload = chatReadService.loadBootstrap(currentUser, requestedSessionId, requestedLimit);

            Object messages = payload.get("messages");
            if (messages instanceof java.util.List<?> list) {
                request.setAttribute("requestMetricsItemCount", list.size());
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok(payload));
        } catch (ChatReadService.ChatApiException e) {
            response.setStatus(e.getStatusCode());
            JsonUtil.writeJson(response, ApiResponse.fail(e.getStatusCode(), e.getPublicMessage()));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ"));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    request,
                    response,
                    log,
                    "ChatBootstrapServlet#doGet",
                    "Lỗi hệ thống khi tải dữ liệu chat.",
                    e);
        }
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }
}
