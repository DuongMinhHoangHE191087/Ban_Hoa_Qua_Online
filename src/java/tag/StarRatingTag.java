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
        StringBuilder sb = new StringBuilder("<span class=\"star-rating\">");
        for (int i = 1; i <= 5; i++) {
            if (r >= i)       sb.append("<span class=\"star-filled\">★</span>");
            else if (r >= i - 0.5) sb.append("<span class=\"star-half\">★</span>");
            else              sb.append("<span class=\"star-empty\">☆</span>");
        }
        if (showValue) sb.append(" <small>(").append(String.format("%.1f", r)).append(")</small>");
        sb.append("</span>");
        getJspContext().getOut().write(sb.toString());
    }

    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setShowValue(boolean showValue) { this.showValue = showValue; }
}
