<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Thông báo của bạn" />
</jsp:include>

<style>
    .notif-item { transition: all 0.2s ease; }
    .notif-item.unread { background-color: #f0fdf4; border-left: 4px solid #4d661c; }
    .notif-item.read { background-color: #ffffff; border-left: 4px solid transparent; }
</style>

<div class="max-w-4xl mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
        <div>
            <h1 class="text-2xl font-bold text-slate-800 flex items-center gap-2">
                <i class="fa-solid fa-bell text-[#4d661c]"></i> Trung tâm thông báo
            </h1>
            <p class="text-sm text-slate-500">Cập nhật đơn hàng, tin nhắn và cảnh báo từ hệ thống</p>
        </div>
        <c:if test="${not empty notificationList}">
            <form action="${pageContext.request.contextPath}/notifications" method="post">
                <input type="hidden" name="action" value="markAllRead">
                <button type="submit" class="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 transition shadow-sm">
                    <i class="fa-solid fa-check-double"></i> Đọc tất cả
                </button>
            </form>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty notificationList}">
            <div class="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-sm">
                <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-200">
                    <i class="fa-solid fa-bell-slash text-slate-400 text-3xl"></i>
                </div>
                <h3 class="text-lg font-bold text-slate-600 mb-1">Không có thông báo nào</h3>
                <p class="text-sm text-slate-400">Bạn sẽ nhận được các thông báo cập nhật tại đây.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden divide-y divide-slate-100">
                <c:forEach var="notif" items="${notificationList}">
                    <div class="notif-item p-5 flex items-start gap-4 hover:bg-slate-50 transition-colors ${notif.isRead ? 'read' : 'unread'}">
                        
                        <!-- Notification Icon -->
                        <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 border 
                            ${notif.type == 'ORDER_UPDATE' ? 'bg-amber-50 text-amber-600 border-amber-100' : 
                              notif.type == 'PROMOTION' ? 'bg-pink-50 text-pink-600 border-pink-100' : 
                              notif.type == 'PAYMENT' ? 'bg-blue-50 text-blue-600 border-blue-100' : 
                              'bg-emerald-50 text-emerald-600 border-emerald-100'}">
                            <i class="fa-solid 
                                ${notif.type == 'ORDER_UPDATE' ? 'fa-box-open' : 
                                  notif.type == 'PROMOTION' ? 'fa-gift' : 
                                  notif.type == 'PAYMENT' ? 'fa-credit-card' : 
                                  'fa-bell'}"></i>
                        </div>

                        <!-- Notification Content -->
                        <div class="flex-1 min-w-0">
                            <div class="flex justify-between items-baseline mb-1">
                                <h3 class="text-sm font-semibold text-slate-800 truncate">${notif.title}</h3>
                                <span class="text-[10px] text-slate-400">
                                    <fmt:formatDate value="${notif.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                </span>
                            </div>
                            <p class="text-sm text-slate-600 mb-2 leading-relaxed">${notif.message}</p>
                            
                            <div class="flex gap-2">
                                <!-- Nút hành động đi tới URL -->
                                <c:if test="${not empty notif.actionUrl}">
                                    <a href="${pageContext.request.contextPath}${notif.actionUrl}" 
                                       onclick="markAsRead('${notif.notificationId}')"
                                       class="inline-flex items-center gap-1 text-xs font-semibold text-[#4d661c] hover:text-[#364e03] hover:underline">
                                        <i class="fa-solid fa-arrow-up-right-from-square text-[10px]"></i> Xem chi tiết
                                    </a>
                                </c:if>
                                
                                <!-- Nút đánh dấu đã đọc (nếu chưa đọc) -->
                                <c:if test="${not notif.isRead}">
                                    <form action="${pageContext.request.contextPath}/notifications" method="post" class="inline">
                                        <input type="hidden" name="action" value="markRead">
                                        <input type="hidden" name="notifId" value="${notif.notificationId}">
                                        <button type="submit" class="text-xs text-slate-400 hover:text-slate-600 font-medium">
                                            Đánh dấu đã đọc
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    // Hàm đánh dấu đã đọc nhanh khi người dùng nhấn xem chi tiết
    function markAsRead(notifId) {
        const formData = new URLSearchParams({
            action: 'markRead',
            notifId: notifId
        });
        fetch('${pageContext.request.contextPath}/notifications', {
            method: 'POST',
            headers: {'Content-Type':'application/x-www-form-urlencoded'},
            body: formData.toString()
        }).catch(err => console.error(err));
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
