package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * StarRatingTag — Custom tag: {@code <ft:stars rating=""/>}
 *
 * Render sao HTML từ rating 0.0 đến 5.0
 * Dùng CSS class .star-filled, .star-half, .star-empty để style
 * @author fruitmkt-team
 */
public class StarRatingTag extends SimpleTagSupport {
    private BigDecimal rating;
    private boolean showValue = false;

    @Override
    public void doTag() throws JspException, IOException {
        double r = rating == null ? 0.0 : rating.doubleValue();
        StringBuilder sb = new StringBuilder("<span class=\"star-rating\" style=\"display:inline-flex;align-items:center;gap:2px;\">");
        for (int i = 1; i <= 5; i++) {
            if (r >= i) {
                sb.append("<span class=\"star-filled\" style=\"color:#F59E0B;\">★</span>");
            } else if (r >= i - 0.5) {
                sb.append("<span class=\"star-half\" style=\"color:#F59E0B;\">★</span>");
            } else {
                sb.append("<span class=\"star-empty\" style=\"color:#CBD5E1;\">☆</span>");
            }
        }
        if (showValue) {
            if (r <= 0.0) {
                sb.append(" <small style=\"color:#94A3B8;font-weight:600;\">Chưa có đánh giá</small>");
            } else {
                sb.append(" <small style=\"color:#64748B;font-weight:600;\">(").append(String.format("%.1f", r)).append(")</small>");
            }
        }
        sb.append("</span>");
        getJspContext().getOut().write(sb.toString());
    }

    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setShowValue(boolean showValue) { this.showValue = showValue; }
}
