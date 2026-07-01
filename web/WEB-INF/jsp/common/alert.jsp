<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- alert.jsp — Hiển thị flash message từ session (PRG pattern).
     Tự động tích hợp SweetAlert2 nếu có sẵn, hoặc dùng Floating Toast cao cấp tự thiết kế.
     Tự động xóa flash sau khi render.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:if test="${not empty sessionScope.flashMsg}">
    <style>
        .premium-toast-fallback {
            position: fixed;
            top: 1.5rem;
            right: 1.5rem;
            z-index: 99999;
            width: 100%;
            max-width: 380px;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08), 0 1px 3px rgba(0, 0, 0, 0.02);
            padding: 1rem 1.25rem;
            display: flex;
            align-items: flex-start;
            gap: 0.75rem;
            font-family: 'Lexend', sans-serif;
            border: 1px solid rgba(226, 232, 240, 0.8);
            animation: toastSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
            overflow: hidden;
            transition: all 0.3s ease;
        }
        .premium-toast-fallback.hide {
            animation: toastSlideOut 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
            opacity: 0;
        }
        @keyframes toastSlideIn {
            from { transform: translateX(120%) translateY(0); opacity: 0; }
            to { transform: translateX(0) translateY(0); opacity: 1; }
        }
        @keyframes toastSlideOut {
            from { transform: translateX(0) scale(1); opacity: 1; }
            to { transform: translateX(50%) scale(0.9); opacity: 0; }
        }
        .premium-toast-fallback::before {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            height: 3px;
            width: 100%;
            animation: toastProgress 4000ms linear forwards;
        }
        @keyframes toastProgress {
            from { width: 100%; }
            to { width: 0%; }
        }
        
        /* Types styling */
        .toast-success::before { background: #10b981; }
        .toast-success .toast-icon { color: #10b981; background: #ecfdf5; }
        
        .toast-error::before { background: #ef4444; }
        .toast-error .toast-icon { color: #ef4444; background: #fef2f2; }
        
        .toast-warning::before { background: #f59e0b; }
        .toast-warning .toast-icon { color: #f59e0b; background: #fffbeb; }
        
        .toast-info::before { background: #3b82f6; }
        .toast-info .toast-icon { color: #3b82f6; background: #eff6ff; }

        .toast-icon {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            flex-shrink: 0;
        }
        .toast-body {
            flex: 1;
            min-width: 0;
        }
        .toast-title {
            font-size: 13px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 2px 0;
        }
        .toast-message {
            font-size: 12px;
            font-weight: 500;
            color: #475569;
            margin: 0;
            line-height: 1.4;
        }
        .toast-close {
            background: transparent;
            border: 0;
            color: #94a3b8;
            cursor: pointer;
            font-size: 16px;
            padding: 0;
            line-height: 1;
            transition: color 0.15s ease;
            margin-top: 2px;
        }
        .toast-close:hover {
            color: #475569;
        }
    </style>

    <script>
        (function() {
            document.addEventListener("DOMContentLoaded", function() {
                var alertMsg = '<c:out value="${fn:escapeXml(sessionScope.flashMsg)}"/>';
                var alertType = "${sessionScope.flashType}";
                
                if (!alertMsg) return;
    
                if (window.Swal) {
                    // Tích hợp Toast cao cấp bằng SweetAlert2
                    Swal.fire({
                        toast: true,
                        position: 'top-end',
                        icon: alertType === 'error' ? 'error' : (alertType === 'warning' ? 'warning' : (alertType === 'info' ? 'info' : 'success')),
                        title: alertMsg,
                        showConfirmButton: false,
                        timer: 4000,
                        timerProgressBar: true,
                        background: '#ffffff',
                        color: '#0f172a',
                        customClass: {
                            popup: 'premium-toast-swal shadow-lg border border-slate-100 rounded-2xl'
                        }
                    });
                } else {
                    // Trình diễn Floating Toast CSS nếu không có Swal
                    var typeClass = alertType === 'error' ? 'toast-error' : (alertType === 'warning' ? 'toast-warning' : (alertType === 'info' ? 'toast-info' : 'toast-success'));
                    var iconHtml = alertType === 'error' ? '<i class="fa-solid fa-circle-exclamation"></i>' : 
                                   (alertType === 'warning' ? '<i class="fa-solid fa-triangle-exclamation"></i>' : 
                                   (alertType === 'info' ? '<i class="fa-solid fa-circle-info"></i>' : '<i class="fa-solid fa-circle-check"></i>'));
                    
                    var titleText = alertType === 'error' ? 'Thất bại' : 
                                      (alertType === 'warning' ? 'Cảnh báo' : 
                                      (alertType === 'info' ? 'Thông tin' : 'Thành công'));
    
                    var toastHtml = 
                        '<div id="premium-floating-toast" class="premium-toast-fallback ' + typeClass + '">' +
                            '<div class="toast-icon">' + iconHtml + '</div>' +
                            '<div class="toast-body">' +
                                '<h4 class="toast-title">' + titleText + '</h4>' +
                                '<p class="toast-message">' + alertMsg + '</p>' +
                            '</div>' +
                            '<button type="button" class="toast-close" onclick="closePremiumToast()">&times;</button>' +
                        '</div>';
                    
                    document.body.insertAdjacentHTML('beforeend', toastHtml);
                    
                    window.closePremiumToast = function() {
                        var toast = document.getElementById('premium-floating-toast');
                        if (toast) {
                            toast.classList.add('hide');
                            setTimeout(function() { toast.remove(); }, 300);
                        }
                    };
    
                    setTimeout(function() {
                        window.closePremiumToast();
                    }, 4000);
                }
            });
        })();
    </script>
    <c:remove var="flashMsg"  scope="session"/>
    <c:remove var="flashType" scope="session"/>
</c:if>
