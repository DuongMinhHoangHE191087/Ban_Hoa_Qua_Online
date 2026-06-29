
<%-- header.jsp — Include vào đầu mỗi JSP page.
     Khai báo taglib, set content type, include alert và navbar.

     CÁCH DÙNG:
       <jsp:include page="/WEB-INF/jsp/common/header.jsp">
           <jsp:param name="pageTitle" value="Tên trang"/>
       </jsp:include>
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${param.pageTitle}"/> — MetaFruit Premium</title>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">
    
    <!-- Google Fonts: Lexend -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- FontAwesome Icons & Material Symbols -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">

    <!-- Core CSS (navbar, legacy components) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

    <%-- ── Tailwind CSS — Global shared design system ────────────────────────── --%>
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
    <script>
        /**
         * MetaFruit Global Tailwind Design System
         * Shared across ALL pages. Pages may call tailwind.config = { theme: { extend: {...} } }
         * after this header to add page-specific tokens without re-declaring the base palette.
         */
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        /* Brand */
                        "primary":               "#4d661c",
                        "primary-hover":         "#364e03",
                        "primary-light":         "#d9f99d",
                        "primary-dark":          "#364e03",
                        "primary-dk":            "#364e03",
                        "primary-lt":            "#f0f7e6",
                        "primary-fixed":         "#ceee93",
                        "primary-fixed-dim":     "#b3d17a",
                        "primary-container":     "#d9f99d",
                        "on-primary":            "#ffffff",
                        "on-primary-container":  "#597428",
                        "on-primary-fixed":      "#131f00",
                        "on-primary-fixed-variant": "#364e03",
                        "secondary":             "#31694b",
                        "secondary-container":   "#b4f0c9",
                        "secondary-fixed":       "#b4f0c9",
                        "secondary-fixed-dim":   "#99d4ae",
                        "on-secondary":          "#ffffff",
                        "on-secondary-container":"#386f50",
                        "on-secondary-fixed":    "#002111",
                        "on-secondary-fixed-variant": "#175034",
                        "tertiary":              "#486554",
                        "tertiary-container":    "#d5f5e0",
                        "tertiary-fixed":        "#caead6",
                        "tertiary-fixed-dim":    "#afceba",
                        "on-tertiary":           "#ffffff",
                        "on-tertiary-container": "#557161",
                        "on-tertiary-fixed":     "#042014",
                        "on-tertiary-fixed-variant": "#314d3e",
                        /* Surfaces */
                        "background":            "#eaffea",
                        "surface":               "#eaffea",
                        "surface-bright":        "#eaffea",
                        "surface-dim":           "#a9e9b6",
                        "surface-variant":       "#b1f2be",
                        "surface-container":     "#bcfdc9",
                        "surface-container-lowest": "#ffffff",
                        "surface-container-low": "#d1ffd8",
                        "surface-container-high": "#b7f7c3",
                        "surface-container-highest": "#b1f2be",
                        "inverse-surface":       "#00391a",
                        "inverse-on-surface":    "#c3ffce",
                        "inverse-primary":       "#b3d17a",
                        /* Text */
                        "on-background":         "#00210d",
                        "on-surface":            "#00210d",
                        "on-surface-variant":    "#44483b",
                        "on-error":              "#ffffff",
                        "on-error-container":    "#93000a",
                        /* Outline */
                        "outline":               "#75796a",
                        "outline-variant":       "#c5c8b7",
                        /* Semantic */
                        "error":                 "#ba1a1a",
                        "error-container":       "#ffdad6",
                        /* Neutral helpers used by existing JSPs */
                        "surface-2":             "#f8fafc",
                        "txt":                   "#0f172a",
                        "txt-2":                 "#475569",
                        "txt-3":                 "#94a3b8"
                    },
                    fontFamily: {
                        sans: ["Lexend", "Segoe UI", "sans-serif"]
                    },
                    spacing: {
                        "xs": "4px",
                        "sm": "12px",
                        "md": "24px",
                        "lg": "40px",
                        "xl": "64px",
                        "gutter": "24px",
                        "margin-mobile": "16px",
                        "margin-desktop": "48px"
                    },
                    borderRadius: {
                        "2xl": "1rem",
                        "3xl": "1.5rem",
                        "4xl": "2rem",
                        "pill": "9999px"
                    },
                    boxShadow: {
                        "card": "0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)"
                    }
                }
            }
        }
    </script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

    <!-- SweetAlert2 — premium modals -->
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <script>
        window.alert = function(message) {
            Swal.fire({
                icon: 'info', title: 'Thông báo', text: message,
                confirmButtonText: 'Đồng ý',
                confirmButtonColor: '#4D661C',
                background: '#ffffff',
                customClass: {
                    popup: 'premium-swal-popup',
                    title: 'premium-swal-title',
                    confirmButton: 'premium-swal-button'
                }
            });
        };
    </script>

    <!-- Global app state for AJAX / cart -->
    <script>
        window.isLoggedIn  = "${sessionScope.currentUser != null}" === "true";
        window.contextPath = '${pageContext.request.contextPath}';
        window.csrfToken   = '${sessionScope._csrfToken}';
        window.getCsrfToken = function() {
            const hidden = document.querySelector('input[name="_csrf"]');
            return (window.csrfToken || (hidden ? hidden.value : '') || '').trim();
        };
    </script>
</head>
<body>
    <%-- Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp"/>

    <%-- Flash alert (PRG pattern) --%>
    <jsp:include page="/WEB-INF/jsp/common/alert.jsp"/>

    <main class="main-content">
