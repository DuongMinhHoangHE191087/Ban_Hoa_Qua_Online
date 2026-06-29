package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * PaginationTag - Custom tag:
 * {@code <ft:pagination current="" total="" baseUrl="/products"/>}
 *
 * Render thanh phân trang Prev/Next và các số trang.
 * Style bằng CSS class .pagination, .page-item, .page-link
 *
 * @author fruitmkt-team
 */
public class PaginationTag extends SimpleTagSupport {
    private int current;
    private int total;
    private String baseUrl;

    @Override
    public void doTag() throws JspException, IOException {
        if (total <= 1) {
            return;
        }

        int activeCurrent = Math.max(1, Math.min(current, total));
        String safeBaseUrl = baseUrl == null ? "" : baseUrl;

        StringBuilder sb = new StringBuilder();

        sb.append("<script>\n")
                .append("if (typeof window.promptPageJumpForTag === 'undefined') {\n")
                .append("    window.promptPageJumpForTag = function(totalPages, baseUrl) {\n")
                .append("        if (typeof Swal !== 'undefined') {\n")
                .append("            Swal.fire({\n")
                .append("                title: 'Chuyển đến trang',\n")
                .append("                text: 'Nhập số trang muốn đến (1 - ' + totalPages + '):',\n")
                .append("                input: 'number',\n")
                .append("                inputAttributes: { min: 1, max: totalPages, step: 1 },\n")
                .append("                showCancelButton: true,\n")
                .append("                confirmButtonText: 'Đến',\n")
                .append("                cancelButtonText: 'Hủy',\n")
                .append("                confirmButtonColor: '#14532D',\n")
                .append("                inputValidator: (value) => {\n")
                .append("                    const page = parseInt(value);\n")
                .append("                    if (isNaN(page) || page < 1 || page > totalPages) {\n")
                .append("                        return 'Số trang phải từ 1 đến ' + totalPages + '!';\n")
                .append("                    }\n")
                .append("                }\n")
                .append("            }).then((result) => {\n")
                .append("                if (result.isConfirmed) {\n")
                .append("                    const separator = baseUrl.indexOf('?') !== -1 ? '&' : '?';\n")
                .append("                    window.location.href = baseUrl + separator + 'page=' + result.value;\n")
                .append("                }\n")
                .append("            });\n")
                .append("        } else {\n")
                .append("            const targetPageStr = prompt('Nhập số trang bạn muốn chuyển đến (1 - ' + totalPages + '):');\n")
                .append("            if (targetPageStr) {\n")
                .append("                const targetPage = parseInt(targetPageStr);\n")
                .append("                if (!isNaN(targetPage) && targetPage >= 1 && targetPage <= totalPages) {\n")
                .append("                    const separator = baseUrl.indexOf('?') !== -1 ? '&' : '?';\n")
                .append("                    window.location.href = baseUrl + separator + 'page=' + targetPage;\n")
                .append("                }\n")
                .append("            }\n")
                .append("        }\n")
                .append("    };\n")
                .append("}\n")
                .append("</script>\n");

        sb.append("<nav class=\"pagination-wrapper\" aria-label=\"Phân trang\"><ul class=\"pagination\">");

        if (activeCurrent > 1) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"")
                    .append(escapeHtmlAttr(buildPageHref(safeBaseUrl, activeCurrent - 1)))
                    .append("\" aria-label=\"Trang trước\">&lsaquo;</a></li>");
        } else {
            sb.append("<li class=\"page-item disabled\"><span class=\"page-link is-disabled\" aria-disabled=\"true\" aria-label=\"Trang trước\">&lsaquo;</span></li>");
        }

        java.util.Set<Integer> pagesToShow = new java.util.TreeSet<>();
        pagesToShow.add(1);
        if (total > 1) {
            pagesToShow.add(total);
            pagesToShow.add(total - 1);
        }
        pagesToShow.add(activeCurrent);
        if (activeCurrent > 1) {
            pagesToShow.add(activeCurrent - 1);
        }
        if (activeCurrent < total) {
            pagesToShow.add(activeCurrent + 1);
        }

        java.util.List<Integer> pagesList = new java.util.ArrayList<>(pagesToShow);
        for (int idx = 0; idx < pagesList.size(); idx++) {
            int pageNum = pagesList.get(idx);
            if (idx > 0) {
                int prevPage = pagesList.get(idx - 1);
                if (pageNum - prevPage > 1) {
                    sb.append("<li class=\"page-item\">")
                            .append("<button type=\"button\" class=\"page-link page-link--ellipsis\" ")
                            .append("onclick=\"promptPageJumpForTag(").append(total).append(", '")
                            .append(escapeJsString(safeBaseUrl)).append("')\" ")
                            .append("title=\"Nhảy đến trang...\" aria-label=\"Nhảy đến trang\">...</button>")
                            .append("</li>");
                }
            }

            boolean isCurrent = pageNum == activeCurrent;
            sb.append("<li class=\"page-item").append(isCurrent ? " active" : "").append("\">")
                    .append("<a class=\"page-link").append(isCurrent ? " is-current" : "")
                    .append("\" href=\"").append(escapeHtmlAttr(buildPageHref(safeBaseUrl, pageNum))).append("\"")
                    .append(isCurrent ? " aria-current=\"page\"" : "")
                    .append(">").append(pageNum).append("</a></li>");
        }

        if (activeCurrent < total) {
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"")
                    .append(escapeHtmlAttr(buildPageHref(safeBaseUrl, activeCurrent + 1)))
                    .append("\" aria-label=\"Trang tiếp theo\">&rsaquo;</a></li>");
        } else {
            sb.append("<li class=\"page-item disabled\"><span class=\"page-link is-disabled\" aria-disabled=\"true\" aria-label=\"Trang tiếp theo\">&rsaquo;</span></li>");
        }

        sb.append("</ul></nav>");
        getJspContext().getOut().write(sb.toString());
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    private String escapeHtmlAttr(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeJsString(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    private String buildPageHref(String base, int pageNum) {
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "page=" + pageNum;
    }
}
