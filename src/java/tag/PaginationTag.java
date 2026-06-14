package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * PaginationTag — Custom tag: {@code <ft:pagination current="" total="" baseUrl="/products"/>}
 *
 * Render thanh phân trang Prev/Next và các số trang.
 * Style bằng CSS class .pagination, .page-item, .page-link
 * @author fruitmkt-team
 */
public class PaginationTag extends SimpleTagSupport {
    private int current;
    private int total;
    private String baseUrl;

    @Override
    public void doTag() throws JspException, IOException {
        if (total <= 1) return;
        StringBuilder sb = new StringBuilder("<nav class=\"pagination-wrapper\"><ul class=\"pagination\">");

        // Prev
        if (current > 1) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"").append(baseUrl)
              .append(separator).append("page=").append(current - 1).append("\">‹ Trước</a></li>");
        }

        // Pages
        int from = Math.max(1, current - 2);
        int to   = Math.min(total, current + 2);
        for (int i = from; i <= to; i++) {
            String active = (i == current) ? " active" : "";
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item").append(active).append("\">")
              .append("<a class=\"page-link\" href=\"").append(baseUrl)
              .append(separator).append("page=").append(i).append("\">").append(i).append("</a></li>");
        }

        // Next
        if (current < total) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"").append(baseUrl)
              .append(separator).append("page=").append(current + 1).append("\">Sau ›</a></li>");
        }

        sb.append("</ul></nav>");
        getJspContext().getOut().write(sb.toString());
    }

    public void setCurrent(int current) { this.current = current; }
    public void setTotal(int total)     { this.total = total; }
    public void setBaseUrl(String url)  { this.baseUrl = url; }
}
