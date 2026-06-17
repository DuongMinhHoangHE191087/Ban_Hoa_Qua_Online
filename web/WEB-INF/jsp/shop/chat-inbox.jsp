<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Chat Inbox"/></jsp:include>

<div class="container" style="padding: var(--space-6) 0;">
    <h1 style="margin-bottom: var(--space-4);">Chat Inbox</h1>

    <c:if test="${not empty sessionScope.flashMsg}">
        <div style="padding: var(--space-3); margin-bottom: var(--space-4); border-radius: var(--radius-md); background: ${sessionScope.flashType == 'success' ? '#dcfce7' : '#fee2e2'}; color: ${sessionScope.flashType == 'success' ? '#166534' : '#991b1b'}; border: 1px solid ${sessionScope.flashType == 'success' ? '#86efac' : '#fca5a5'};">
            <strong>${sessionScope.flashMsg}</strong>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <div class="card">
        <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between; align-items: center;">
            <h2 style="margin: 0; font-size: var(--font-size-lg);">Danh sách cuộc hội thoại</h2>
            <span style="background: var(--color-primary); color: white; padding: 4px 12px; border-radius: var(--radius-full); font-size: 0.9rem; font-weight: 600;">
                ${not empty sessions ? sessions.size() : 0}
            </span>
        </div>

        <div style="padding: var(--space-4);">
            <c:choose>
                <c:when test="${empty sessions}">
                    <div style="text-align: center; padding: var(--space-6); color: var(--color-muted);">
                        <i class="fa-solid fa-comments" style="font-size: 3rem; margin-bottom: var(--space-3); opacity: 0.5; display: block;"></i>
                        <p style="margin: 0;">Hiện không có cuộc hội thoại nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: var(--space-2);">
                        <c:forEach var="session" items="${sessions}">
                            <a href="${pageContext.request.contextPath}/shop/chat?sessionId=${session.sessionId}"
                               style="padding: var(--space-3); border: 1px solid var(--color-border); border-radius: var(--radius-md); text-decoration: none; color: inherit; transition: all 0.2s; display: flex; justify-content: space-between; align-items: center;">
                                <div style="display: flex; align-items: center; gap: var(--space-3); flex: 1;">
                                    <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; font-size: 1.2rem;">
                                        ${session.participantName.charAt(0)}
                                    </div>
                                    <div style="flex: 1;">
                                        <h3 style="margin: 0; font-weight: 600; font-size: 0.95rem;">${session.participantName}</h3>
                                        <p style="margin: 4px 0 0 0; color: var(--color-muted); font-size: 0.9rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                            ${session.lastMessage}
                                        </p>
                                    </div>
                                </div>
                                <div style="text-align: right;">
                                    <span style="font-size: 0.85rem; color: var(--color-muted);">
                                        <fmt:formatDate value="${session.lastMessageTime}" pattern="HH:mm"/>
                                    </span>
                                    <c:if test="${session.unreadCount > 0}">
                                        <div style="background: var(--color-danger); color: white; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700; margin-top: 4px;">
                                            ${session.unreadCount}
                                        </div>
                                    </c:if>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
