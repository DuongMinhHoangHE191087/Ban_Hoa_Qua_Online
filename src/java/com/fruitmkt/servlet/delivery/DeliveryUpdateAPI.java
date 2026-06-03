package com.fruitmkt.servlet.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.DeliveryService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/delivery/api/update")
public class DeliveryUpdateAPI extends HttpServlet {
    private final DeliveryService deliveryService = new DeliveryService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = SessionUtil.getCurrentUser(req.getSession(false));
            if (currentUser == null || !"DELIVERY".equals(currentUser.getRole())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.put("success", false);
                response.put("message", "Truy cập bị từ chối");
                mapper.writeValue(resp.getWriter(), response);
                return;
            }

            Map<String, String> data = mapper.readValue(req.getInputStream(), Map.class);
            int deliveryId = Integer.parseInt(data.get("deliveryId"));
            String action = data.get("action"); // "STATUS" or "ESTIMATE"

            if ("STATUS".equals(action)) {
                String status = data.get("status");
                String reason = data.get("failureReason");
                String proofUrl = data.get("proofImageUrl");
                deliveryService.updateStatusAndProof(currentUser.getUserId(), deliveryId, status, reason, proofUrl);
            } else if ("ESTIMATE".equals(action)) {
                String estTimeStr = data.get("estimatedTime");
                if (estTimeStr != null && !estTimeStr.isEmpty()) {
                    LocalDateTime est = LocalDateTime.parse(estTimeStr); // Expects ISO format like 2026-05-31T14:30
                    deliveryService.updateEstimatedTime(currentUser.getUserId(), deliveryId, est);
                }
            }

            response.put("success", true);
            mapper.writeValue(resp.getWriter(), response);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.put("success", false);
            response.put("message", e.getMessage());
            mapper.writeValue(resp.getWriter(), response);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "Lỗi máy chủ");
            mapper.writeValue(resp.getWriter(), response);
        }
    }
}
