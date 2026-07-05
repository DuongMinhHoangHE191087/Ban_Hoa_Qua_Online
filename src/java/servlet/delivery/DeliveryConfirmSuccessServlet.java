package servlet.delivery;

import config.AppConfig;
import model.entity.auth.User;
import service.order.DeliveryService;
import util.FileUploadUtil;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/delivery/confirm-success")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 20    // 20MB
)
public class DeliveryConfirmSuccessServlet extends HttpServlet {
    private static final Logger log = LoggerUtil.getLogger(DeliveryConfirmSuccessServlet.class);
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession(false));
        if (currentUser == null || !"DELIVERY".equals(currentUser.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Truy cập bị từ chối");
            return;
        }

        try {
            int deliveryId = Integer.parseInt(req.getParameter("deliveryId"));
            Part proofImage = req.getPart("proofImage");
            
            if (proofImage == null || proofImage.getSize() == 0) {
                SessionUtil.flashError(req.getSession(), "Vui lòng chọn ảnh bằng chứng!");
                resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
                return;
            }

            String uploadDir = getServletContext().getRealPath("");
            String relativePath = FileUploadUtil.save(proofImage, uploadDir);

            if (relativePath == null) {
                SessionUtil.flashError(req.getSession(), "Tải ảnh lên thất bại! Định dạng không hỗ trợ hoặc lỗi hệ thống.");
                resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
                return;
            }

            // Construct full absolute URL for the proof image so it can be viewed correctly
            String fullUrl = req.getContextPath() + "/" + relativePath;
            deliveryService.markAsDelivered(currentUser.getUserId(), deliveryId, fullUrl);

            SessionUtil.flashSuccess(req.getSession(), "Xác nhận giao hàng thành công!");
            resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");

        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(req.getSession(), util.ErrorMessageUtil.getUserMessage(e));
            resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi upload ảnh bằng chứng", e);
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra, vui lòng thử lại sau.");
            resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
        }
    }
}
