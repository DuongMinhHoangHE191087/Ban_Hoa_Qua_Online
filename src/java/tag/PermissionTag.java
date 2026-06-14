package tag;

import config.AppConfig;
import model.entity.auth.User;
import util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * PermissionTag — Custom tag: {@code <ft:allow role="ADMIN">...</ft:allow>}
 *
 * Chỉ render body nếu user hiện tại có role khớp.
 * Hỗ trợ nhiều role: role="ADMIN,SHOP_OWNER"
 * @author fruitmkt-team
 */
public class PermissionTag extends SimpleTagSupport {
    private String role;

    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        HttpSession session = pageContext.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user != null && role != null) {
            for (String r : role.split(",")) {
                if (r.trim().equals(user.getRole())) {
                    getJspBody().invoke(null);
                    return;
                }
            }
        }
        // Không có quyền → không render body
    }

    public void setRole(String role) { this.role = role; }
}
