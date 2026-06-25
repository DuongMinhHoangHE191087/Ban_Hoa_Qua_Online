package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * PaginationTag — Custom tag:
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
        if (total <= 1)
            return;

        // Clamp current to valid range
        int activeCurrent = Math.max(1, Math.min(current, total));

        StringBuilder sb = new StringBuilder();

        // Inject self-contained JavaScript for ellipsis jumping (only once per page
        // check)
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

        sb.append("<nav class=\"pagination-wrapper\"><ul class=\"pagination\">");

        // Prev
        if (activeCurrent > 1) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"").append(baseUrl)
                    .append(separator).append("page=").append(activeCurrent - 1).append("\"> < </a></li>");
        } else {
            sb.append(
                    "<li class=\"page-item disabled\"><span class=\"page-link text-gray-400 cursor-not-allowed\"> < </span></li>");
        }

        // Collect page numbers to show in a sorted unique set
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

        // Render page list with gaps handled by ellipsis
        java.util.List<Integer> pagesList = new java.util.ArrayList<>(pagesToShow);
        for (int idx = 0; idx < pagesList.size(); idx++) {
            int pageNum = pagesList.get(idx);
            if (idx > 0) {
                int prevPage = pagesList.get(idx - 1);
                if (pageNum - prevPage > 1) {
                    String escapedBaseUrl = baseUrl.replace("'", "\\'");
                    sb.append(
                            "<li class=\"page-item\"><a class=\"page-link\" href=\"javascript:void(0)\" onclick=\"promptPageJumpForTag(")
                            .append(total).append(", '").append(escapedBaseUrl)
                            .append("')\" title=\"Nhảy đến trang...\">...</a></li>");
                }
            }

            String active = (pageNum == activeCurrent) ? " active" : "";
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item").append(active).append("\">")
                    .append("<a class=\"page-link\" href=\"").append(baseUrl)
                    .append(separator).append("page=").append(pageNum).append("\">").append(pageNum)
                    .append("</a></li>");
        }

        // Next
        if (activeCurrent < total) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"").append(baseUrl)
                    .append(separator).append("page=").append(activeCurrent + 1).append("\"> > </a></li>");
        } else {
            sb.append(
                    "<li class=\"page-item disabled\"><span class=\"page-link text-gray-400 cursor-not-allowed\"> > </span></li>");
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
}
