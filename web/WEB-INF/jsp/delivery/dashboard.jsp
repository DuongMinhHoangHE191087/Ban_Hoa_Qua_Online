<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Bảng Điều Khiển Giao Hàng" />
</jsp:include>

<!-- Load Tailwind CSS dynamically -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    primary: '#4d661c',
                    'primary-hover': '#364e03',
                    'primary-light': '#d9f99d',
                },
                fontFamily: { sans: ['Lexend', 'sans-serif'] }
            }
        }
    }
</script>

<div class="max-w-7xl mx-auto my-10 p-6 bg-white/70 backdrop-blur-md border border-[#e2ece7] rounded-3xl shadow-lg min-h-[70vh]">
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-[#e2ece7] pb-5 mb-8 gap-4">
        <div>
            <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] flex items-center gap-2">
                <i class="fa-solid fa-truck-fast text-primary"></i> Nhiệm Vụ Giao Hàng
            </h1>
            <p class="text-xs md:text-sm text-[#475569] mt-1">Quản lý các đơn hàng được phân công cho bạn</p>
        </div>
        <a href="${pageContext.request.contextPath}/auth/logout" class="bg-white border border-[#4d661c] hover:bg-[#f0f7e6] text-[#4d661c] font-bold text-xs px-4 py-2 rounded-xl transition-all shadow-sm flex items-center gap-2">
            <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
        </a>
    </div>

    <c:if test="${empty deliveryList}">
        <div class="text-center py-16 px-6 text-[#94a3b8]">
            <i class="fa-solid fa-box-open text-5xl mb-4 text-[#cbd5e1]"></i>
            <h2 class="text-lg font-bold text-[#475569] mb-1">Chưa có đơn hàng nào</h2>
            <p class="text-xs text-[#94a3b8] max-w-sm mx-auto">Hiện tại bạn chưa được phân công giao đơn hàng nào. Hãy chờ đợi hoặc liên hệ Quản lý.</p>
        </div>
    </c:if>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <c:forEach var="deliv" items="${deliveryList}">
            <div class="bg-white/95 border border-[#e2ece7] rounded-3xl overflow-hidden flex flex-col hover:-translate-y-1 hover:shadow-md transition-all duration-300">
                <div class="p-5 border-b border-[#e2ece7] bg-[#f9fdf9] flex justify-between items-center">
                    <h3 class="font-bold text-sm text-[#1e293b]">Đơn hàng #${deliv.orderId}</h3>
                    <span class="px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider
                        <c:choose>
                            <c:when test="${deliv.status == 'ASSIGNED'}">bg-blue-50 text-blue-700 border border-blue-200</c:when>
                            <c:when test="${deliv.status == 'PICKED_UP'}">bg-amber-50 text-amber-700 border border-amber-200</c:when>
                            <c:when test="${deliv.status == 'IN_TRANSIT'}">bg-purple-50 text-purple-700 border border-purple-200</c:when>
                            <c:when test="${deliv.status == 'DELIVERED'}">bg-emerald-50 text-emerald-700 border border-emerald-200</c:when>
                            <c:when test="${deliv.status == 'FAILED'}">bg-red-50 text-red-700 border border-red-200</c:when>
                        </c:choose>">
                        <c:choose>
                            <c:when test="${deliv.status == 'ASSIGNED'}">Mới nhận</c:when>
                            <c:when test="${deliv.status == 'PICKED_UP'}">Đã lấy hàng</c:when>
                            <c:when test="${deliv.status == 'IN_TRANSIT'}">Đang giao</c:when>
                            <c:when test="${deliv.status == 'DELIVERED'}">Thành công</c:when>
                            <c:when test="${deliv.status == 'FAILED'}">Thất bại</c:when>
                        </c:choose>
                    </span>
                </div>
                
                <div class="p-5 flex-grow">
                    <div class="flex items-start gap-3 mb-3 text-xs md:text-sm">
                        <i class="fa-solid fa-clock text-slate-400 mt-1"></i>
                        <div>
                            <strong class="block text-xs text-[#475569]">Dự kiến giao:</strong>
                            <c:choose>
                                <c:when test="${not empty deliv.estimatedDeliveryTime}">
                                    <fmt:parseDate value="${deliv.estimatedDeliveryTime}" pattern="yyyy-MM-dd'T'HH:mm" var="estDate" type="both" />
                                    <b class="text-primary font-bold"><fmt:formatDate value="${estDate}" pattern="dd/MM/yyyy HH:mm" /></b>
                                </c:when>
                                <c:otherwise><span class="text-slate-400 italic">Chưa thiết lập</span></c:otherwise>
                            </c:choose>
                            <button type="button" onclick="openEstimateModal('${deliv.deliveryId}')" class="bg-none border-none text-primary hover:text-primary-hover underline cursor-pointer text-[11px] font-bold ml-2">Cập nhật</button>
                        </div>
                    </div>
                    
                    <c:if test="${deliv.status == 'FAILED'}">
                        <div class="bg-red-50 text-red-700 p-3.5 rounded-2xl text-xs font-semibold mt-4 border border-red-100">
                            <strong>Lý do thất bại:</strong> ${deliv.failureReason}
                        </div>
                    </c:if>
                </div>
                
                <div class="p-5 bg-white border-t border-[#e2ece7] mt-auto">
                    <c:choose>
                        <c:when test="${deliv.status == 'ASSIGNED'}">
                            <button onclick="updateStatus('${deliv.deliveryId}', 'PICKED_UP')" class="w-full bg-primary hover:bg-primary-hover text-white font-bold py-2.5 rounded-xl transition-all shadow-sm active:scale-95 cursor-pointer border-0">Xác nhận Đã Lấy Hàng</button>
                        </c:when>
                        <c:when test="${deliv.status == 'PICKED_UP'}">
                            <button onclick="updateStatus('${deliv.deliveryId}', 'IN_TRANSIT')" class="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold py-2.5 rounded-xl transition-all shadow-sm active:scale-95 cursor-pointer border-0">Bắt đầu Đi Giao</button>
                        </c:when>
                        <c:when test="${deliv.status == 'IN_TRANSIT'}">
                            <div class="flex gap-3">
                                <button onclick="openProofModal('${deliv.deliveryId}')" class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 rounded-xl transition-all shadow-sm active:scale-95 cursor-pointer border-0 flex items-center justify-center gap-1.5"><i class="fa-solid fa-check"></i> Thành công</button>
                                <button onclick="openFailModal('${deliv.deliveryId}')" class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-2.5 rounded-xl transition-all shadow-sm active:scale-95 cursor-pointer border-0 flex items-center justify-center gap-1.5"><i class="fa-solid fa-xmark"></i> Thất bại</button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <button disabled class="w-full bg-slate-100 text-slate-400 font-bold py-2.5 rounded-xl cursor-not-allowed border-0 opacity-60">Đã hoàn tất xử lý</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<!-- Modal Bằng Chứng Giao Hàng -->
<div id="proofModal" class="hidden fixed inset-0 bg-black/50 z-[1000] flex items-center justify-center p-4">
    <div class="bg-white w-full max-w-md p-6 rounded-3xl shadow-xl">
        <h3 class="text-emerald-600 font-bold text-lg flex items-center gap-2 mb-2"><i class="fa-solid fa-check-circle"></i> Giao Thành Công</h3>
        <p class="text-xs text-[#64748b] mb-4">Cung cấp hình ảnh bằng chứng giao hàng thành công.</p>
        <input type="hidden" id="proofDeliveryId">
        <div class="mb-4">
            <label class="block text-xs font-bold text-[#475569] mb-1.5">URL Ảnh Bằng Chứng *</label>
            <input type="text" id="proofImageUrl" class="w-full px-4 py-2 border border-[#cbd5e1] rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10" placeholder="https://example.com/image.jpg">
        </div>
        <div class="flex justify-end gap-3 mt-6">
            <button onclick="closeModal('proofModal')" class="bg-slate-100 hover:bg-slate-200 text-[#475569] font-bold text-xs px-4 py-2.5 rounded-xl transition-all">Hủy</button>
            <button onclick="submitSuccess()" class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs px-4 py-2.5 rounded-xl transition-all shadow-md">Xác nhận</button>
        </div>
    </div>
</div>

<!-- Modal Giao Thất Bại -->
<div id="failModal" class="hidden fixed inset-0 bg-black/50 z-[1000] flex items-center justify-center p-4">
    <div class="bg-white w-full max-w-md p-6 rounded-3xl shadow-xl">
        <h3 class="text-red-600 font-bold text-lg flex items-center gap-2 mb-2"><i class="fa-solid fa-times-circle"></i> Báo Cáo Thất Bại</h3>
        <input type="hidden" id="failDeliveryId">
        <div class="mb-4">
            <label class="block text-xs font-bold text-[#475569] mb-1.5">Lý do thất bại *</label>
            <textarea id="failureReason" rows="3" class="w-full px-4 py-2 border border-[#cbd5e1] rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10" placeholder="Ví dụ: Khách không nghe máy, Sai địa chỉ..."></textarea>
        </div>
        <div class="flex justify-end gap-3 mt-6">
            <button onclick="closeModal('failModal')" class="bg-slate-100 hover:bg-slate-200 text-[#475569] font-bold text-xs px-4 py-2.5 rounded-xl transition-all">Hủy</button>
            <button onclick="submitFail()" class="bg-red-600 hover:bg-red-700 text-white font-bold text-xs px-4 py-2.5 rounded-xl transition-all shadow-md">Xác nhận Thất bại</button>
        </div>
    </div>
</div>

<!-- Modal Thời gian dự kiến -->
<div id="estimateModal" class="hidden fixed inset-0 bg-black/50 z-[1000] flex items-center justify-center p-4">
    <div class="bg-white w-full max-w-md p-6 rounded-3xl shadow-xl">
        <h3 class="text-[#364e03] font-bold text-lg flex items-center gap-2 mb-2"><i class="fa-solid fa-clock"></i> Thời Gian Dự Kiến</h3>
        <input type="hidden" id="estDeliveryId">
        <div class="mb-4">
            <label class="block text-xs font-bold text-[#475569] mb-1.5">Thời gian giao hàng dự kiến *</label>
            <input type="datetime-local" id="estimatedTime" class="w-full px-4 py-2 border border-[#cbd5e1] rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10">
        </div>
        <div class="flex justify-end gap-3 mt-6">
            <button onclick="closeModal('estimateModal')" class="bg-slate-100 hover:bg-slate-200 text-[#475569] font-bold text-xs px-4 py-2.5 rounded-xl transition-all">Hủy</button>
            <button onclick="submitEstimate()" class="bg-primary hover:bg-primary-hover text-white font-bold text-xs px-4 py-2.5 rounded-xl transition-all shadow-md">Lưu</button>
        </div>
    </div>
</div>

<script>
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }
function openProofModal(id) { document.getElementById('proofDeliveryId').value = id; document.getElementById('proofImageUrl').value = ''; document.getElementById('proofModal').classList.remove('hidden'); }
function openFailModal(id) { document.getElementById('failDeliveryId').value = id; document.getElementById('failureReason').value = ''; document.getElementById('failModal').classList.remove('hidden'); }
function openEstimateModal(id) { document.getElementById('estDeliveryId').value = id; document.getElementById('estimateModal').classList.remove('hidden'); }

async function apiCall(data) {
    try {
        const res = await fetch('${pageContext.request.contextPath}/delivery/api/update', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'X-CSRF-Token': window.csrfToken || '',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        });
        const contentType = res.headers.get("content-type");
        if (!res.ok || !contentType || contentType.indexOf("application/json") === -1) {
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errData = await res.json();
                throw new Error(errData.message || errData.error || 'Lỗi hệ thống (Mã: ' + res.status + ')');
            }
            throw new Error('Lỗi hệ thống (Mã: ' + res.status + ')');
        }
        const result = await res.json();
        if (result.success) {
            location.reload();
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (e) { alert(e.message || "Lỗi mạng!"); }
}

function updateStatus(id, status) {
    if(confirm("Xác nhận chuyển trạng thái?")) {
        apiCall({ action: "STATUS", deliveryId: id, status: status });
    }
}
function submitSuccess() {
    const id = document.getElementById('proofDeliveryId').value;
    const url = document.getElementById('proofImageUrl').value;
    if(!url) { alert("Vui lòng nhập link ảnh!"); return; }
    apiCall({ action: "STATUS", deliveryId: id, status: "DELIVERED", proofImageUrl: url });
}
function submitFail() {
    const id = document.getElementById('failDeliveryId').value;
    const reason = document.getElementById('failureReason').value;
    if(!reason) { alert("Vui lòng nhập lý do!"); return; }
    apiCall({ action: "STATUS", deliveryId: id, status: "FAILED", failureReason: reason });
}
function submitEstimate() {
    const id = document.getElementById('estDeliveryId').value;
    const time = document.getElementById('estimatedTime').value;
    if(!time) { alert("Vui lòng chọn thời gian!"); return; }
    apiCall({ action: "ESTIMATE", deliveryId: id, estimatedTime: time });
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
