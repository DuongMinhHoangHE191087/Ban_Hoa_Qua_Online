package exception;

/**
 * BusinessException — Exception nghiệp vụ dùng trong Service layer.
 *
 * Thay thế cho việc ném nhiều loại exception khác nhau (IllegalArgumentException,
 * IllegalStateException, RuntimeException...) từ Service. Servlet chỉ cần catch
 * BusinessException để hiển thị message thân thiện với người dùng.
 *
 * CÁCH DÙNG TRONG SERVICE:
 * <pre>
 *   public void cancelOrder(int orderId, int customerId) {
 *       Order order = orderDAO.findOneById(orderId);
 *       if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "Đơn hàng không tồn tại.");
 *       if (!canCancel(order.getStatus())) throw new BusinessException("INVALID_STATUS", "Không thể hủy đơn ở trạng thái này.");
 *   }
 * </pre>
 *
 * CÁCH DÙNG TRONG SERVLET:
 * <pre>
 *   try {
 *       orderService.cancelOrder(orderId, user.getUserId());
 *       SessionUtil.flashSuccess(session, "Đã hủy đơn hàng.");
 *   } catch (BusinessException e) {
 *       SessionUtil.flashError(session, e.getPublicMessage());
 *   } catch (SQLException e) {
 *       SessionUtil.flashError(session, "Lỗi hệ thống, vui lòng thử lại.");
 *   }
 * </pre>
 *
 * @author fruitmkt-team
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /** Mã lỗi để phân loại (ORDER_NOT_FOUND, INVALID_STATUS, STOCK_INSUFFICIENT...) */
    public String getErrorCode() {
        return errorCode;
    }

    /** Message public cho user-facing flow. */
    public String getPublicMessage() {
        return super.getMessage();
    }
}
