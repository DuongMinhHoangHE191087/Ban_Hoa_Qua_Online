package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * OrderStatusTag — Custom tag: {@code <ft:orderStatus code=""/>}
 *
 * Render badge HTML có màu theo trạng thái đơn.
 * Thêm CSS class .badge-* vào main.css để style.
 * @author fruitmkt-team
 */
public class OrderStatusTag extends SimpleTagSupport {
    private String code;

    @Override
    public void doTag() throws JspException, IOException {
        String cssClass = getCssClass(code);
        String label    = getLabel(code);
        String html = "<span class=\"badge " + cssClass + "\">" + label + "</span>";
        getJspContext().getOut().write(html);
    }

    private String getCssClass(String code) {
        if (code == null) return "badge-secondary";
        return switch (code) {
            case "DELIVERED"       -> "badge-success";
            case "CONFIRMED",
                 "PREPARING"       -> "badge-info";
            case "DISPATCHED"      -> "badge-primary";
            case "PENDING_PAYMENT" -> "badge-warning";
            case "CANCELLED",
                 "PAYMENT_FAILED",
                 "EXPIRED"         -> "badge-danger";
            default                -> "badge-secondary";
        };
    }

    private String getLabel(String code) {
        if (code == null) return "Không rõ";
        return switch (code) {
            case "PENDING_PAYMENT" -> "Chờ thanh toán";
            case "CONFIRMED"       -> "Đã xác nhận";
            case "PREPARING"       -> "Đang chuẩn bị";
            case "DISPATCHED"      -> "Đang giao";
            case "DELIVERED"       -> "Đã giao";
            case "CANCELLED"       -> "Đã hủy";
            case "PAYMENT_FAILED"  -> "Thanh toán lỗi";
            case "EXPIRED"         -> "Hết hạn";
            default                -> code;
        };
    }

    public void setCode(String code) { this.code = code; }
}
