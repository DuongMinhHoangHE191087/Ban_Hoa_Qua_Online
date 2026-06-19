<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Lỗi máy chủ | MetaFruit</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;800&family=Plus+Jakarta+Sans:wght@400;500;700&display=swap" rel="stylesheet">
    
    <style>
        :root {
            --color-primary: #E8662B;
            --color-primary-glow: rgba(232, 102, 43, 0.4);
            --color-bg: #0B0F19;
            --color-card-bg: rgba(255, 255, 255, 0.03);
            --color-card-border: rgba(255, 255, 255, 0.08);
            --font-display: 'Outfit', 'Plus Jakarta Sans', sans-serif;
            --font-body: 'Plus Jakarta Sans', sans-serif;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: var(--font-body);
            background-color: var(--color-bg);
            background-image: 
                radial-gradient(circle at 10% 20%, rgba(232, 102, 43, 0.15) 0%, transparent 40%),
                radial-gradient(circle at 90% 80%, rgba(245, 124, 0, 0.12) 0%, transparent 40%);
            background-attachment: fixed;
            color: #FFFFFF;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow-x: hidden;
            padding: 24px;
        }

        .container {
            max-width: 580px;
            width: 100%;
            text-align: center;
            z-index: 2;
        }

        /* Glassmorphism Card */
        .error-card {
            background: var(--color-card-bg);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid var(--color-card-border);
            border-radius: 28px;
            padding: 48px 32px;
            box-shadow: 0 24px 60px rgba(0, 0, 0, 0.4),
                        inset 0 1px 0 rgba(255, 255, 255, 0.1);
            position: relative;
            overflow: hidden;
        }

        .error-card::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(232, 102, 43, 0.05) 0%, transparent 70%);
            pointer-events: none;
        }

        /* Sad / Broken Fruit Animation */
        .fruit-container {
            position: relative;
            width: 120px;
            height: 120px;
            margin: 0 auto 32px;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .sad-orange {
            width: 100px;
            height: 100px;
            background: linear-gradient(135deg, #FF9E2C 0%, #E8662B 100%);
            border-radius: 50%;
            position: relative;
            box-shadow: 0 10px 25px rgba(232, 102, 43, 0.3),
                        inset -5px -5px 15px rgba(0, 0, 0, 0.15),
                        inset 5px 5px 15px rgba(255, 255, 255, 0.2);
            animation: bob 3s ease-in-out infinite;
        }

        /* Split crack for 500 error */
        .sad-orange::after {
            content: '';
            position: absolute;
            width: 8px;
            height: 80px;
            background: var(--color-bg);
            left: calc(50% - 4px);
            top: 10px;
            border-radius: 4px;
            box-shadow: 0 0 8px rgba(232, 102, 43, 0.5);
            animation: crack-pulse 2s infinite alternate;
        }

        .orange-leaf {
            position: absolute;
            top: -12px;
            left: calc(50% - 15px);
            width: 30px;
            height: 16px;
            background: linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%);
            border-radius: 16px 0 16px 0;
            transform: rotate(-15deg);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }

        .sad-eyes {
            position: absolute;
            top: 40px;
            width: 100%;
            display: flex;
            justify-content: space-around;
            padding: 0 20px;
            box-sizing: border-box;
        }

        .eye {
            width: 12px;
            height: 12px;
            background: #2C2C2C;
            border-radius: 50%;
            position: relative;
        }

        .eye::after {
            content: 'x';
            color: #FFF;
            position: absolute;
            font-size: 14px;
            font-weight: 800;
            top: -6px;
            left: -1px;
            line-height: 1;
        }

        .sad-mouth {
            position: absolute;
            bottom: 22px;
            left: calc(50% - 12px);
            width: 24px;
            height: 12px;
            border: 3px solid #2C2C2C;
            border-bottom: none;
            border-radius: 20px 20px 0 0;
            transform: rotate(180deg);
        }

        /* Typography */
        h1 {
            font-family: var(--font-display);
            font-size: 2.5rem;
            font-weight: 800;
            background: linear-gradient(135deg, #FFFFFF 0%, #B0BAC9 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 16px;
            letter-spacing: -0.5px;
        }

        p {
            font-size: 1.1rem;
            color: #A0AEC0;
            line-height: 1.6;
            margin-bottom: 40px;
            max-width: 440px;
            margin-left: auto;
            margin-right: auto;
        }

        .error-meta {
            display: grid;
            gap: 10px;
            margin: -12px auto 32px;
            max-width: 440px;
            padding: 16px 18px;
            text-align: left;
            border-radius: 16px;
            background: rgba(0, 0, 0, 0.22);
            border: 1px solid rgba(255, 255, 255, 0.08);
            color: #E2E8F0;
            font-size: 0.95rem;
        }

        .error-meta strong {
            color: #FFFFFF;
        }

        /* Glow Button */
        .btn-home {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: linear-gradient(135deg, #E8662B 0%, #F57C00 100%);
            color: #FFFFFF;
            font-size: 1rem;
            font-weight: 700;
            text-decoration: none;
            padding: 16px 36px;
            border-radius: 16px;
            box-shadow: 0 4px 24px var(--color-primary-glow),
                        inset 0 1px 0 rgba(255, 255, 255, 0.2);
            transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
            cursor: pointer;
            border: none;
        }

        .btn-home:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 30px rgba(232, 102, 43, 0.6);
        }

        .btn-home:active {
            transform: translateY(-1px);
        }

        .btn-home svg {
            width: 20px;
            height: 20px;
            fill: currentColor;
            transition: transform 0.2s ease;
        }

        .btn-home:hover svg {
            transform: translateX(-4px);
        }

        /* Collapsible Debug Panel for dev */
        .debug-panel {
            margin-top: 32px;
            text-align: left;
            border-top: 1px solid rgba(255, 255, 255, 0.08);
            padding-top: 20px;
        }

        .debug-title {
            font-size: 0.85rem;
            color: #718096;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            user-select: none;
            transition: color 0.2s;
        }

        .debug-title:hover {
            color: #A0AEC0;
        }

        .debug-content {
            display: none;
            margin-top: 12px;
            background: rgba(0, 0, 0, 0.4);
            border: 1px solid rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            padding: 16px;
            font-family: 'Courier New', Courier, monospace;
            font-size: 0.8rem;
            color: #E2E8F0;
            overflow-x: auto;
            max-height: 200px;
        }

        .debug-content.show {
            display: block;
        }

        /* Animations */
        @keyframes bob {
            0%, 100% {
                transform: translateY(0) rotate(0deg);
            }
            50% {
                transform: translateY(-12px) rotate(-3deg);
            }
        }

        @keyframes crack-pulse {
            0% {
                opacity: 0.5;
                box-shadow: 0 0 4px rgba(232, 102, 43, 0.3);
            }
            100% {
                opacity: 1;
                box-shadow: 0 0 16px rgba(232, 102, 43, 0.8);
            }
        }

        @keyframes float-bg {
            0%, 100% {
                transform: translateY(0);
            }
            50% {
                transform: translateY(-20px);
            }
        }

        /* Decorative glowing particles */
        .particle {
            position: absolute;
            background: rgba(232, 102, 43, 0.2);
            border-radius: 50%;
            filter: blur(8px);
            z-index: 1;
            pointer-events: none;
            animation: float-bg 8s ease-in-out infinite;
        }

        .p1 { width: 120px; height: 120px; top: 15%; left: 10%; animation-delay: 0s; }
        .p2 { width: 180px; height: 180px; bottom: 10%; right: 5%; animation-delay: 2s; }

        @media (max-width: 480px) {
            .error-card {
                padding: 36px 20px;
                border-radius: 20px;
            }
            h1 {
                font-size: 2rem;
            }
            p {
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>
    <div class="particle p1"></div>
    <div class="particle p2"></div>

    <div class="container">
        <div class="error-card">
            <!-- Animated sad cracked fruit graphics -->
            <div class="fruit-container">
                <div class="orange-leaf"></div>
                <div class="sad-orange">
                    <div class="sad-eyes">
                        <div class="eye"></div>
                        <div class="eye"></div>
                    </div>
                    <div class="sad-mouth"></div>
                </div>
            </div>

            <!-- Custom User-requested Text -->
            <h1>500 — Lỗi máy chủ</h1>
            <p>Xin lỗi, đã có lỗi xảy ra. Vui lòng quay về trang chủ.</p>

            <c:choose>
                <c:when test="${not empty requestScope.logFilePath}">
                    <c:set var="resolvedLogFilePath" value="${requestScope.logFilePath}" />
                </c:when>
                <c:otherwise>
                    <c:set var="resolvedLogFilePath" value="${applicationScope.appLogFilePath}" />
                </c:otherwise>
            </c:choose>
            <div class="error-meta">
                <c:if test="${not empty requestScope.errorId}">
                    <div><strong>Mã tham chiếu:</strong> ${requestScope.errorId}</div>
                </c:if>
                <c:if test="${not empty resolvedLogFilePath}">
                    <div><strong>Log chi tiết:</strong> ${resolvedLogFilePath}</div>
                </c:if>
            </div>

            <a href="${pageContext.request.contextPath}/" class="btn-home">
                <svg viewBox="0 0 24 24">
                    <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                </svg>
                Quay về trang chủ
            </a>

            <!-- Gỡ lỗi chi tiết (chỉ hiển thị khi có Exception) -->
            <c:if test="${not empty applicationScope.appEnv and applicationScope.appEnv ne 'production' and (not empty pageContext.exception or not empty requestScope['jakarta.servlet.error.exception'])}">
                <div class="debug-panel">
                    <div class="debug-title" onclick="toggleDebug()">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 12 15 18 9"></polyline>
                        </svg>
                        Thông tin kỹ thuật (Developer Only)
                    </div>
                    <div id="debug-content" class="debug-content">
                        <strong>Thông điệp:</strong> ${requestScope['jakarta.servlet.error.message']}<br>
                        <strong>Mã tham chiếu:</strong> ${requestScope.errorId}<br>
                        <strong>Log file:</strong> ${resolvedLogFilePath}<br>
                        <strong>Exception:</strong> ${requestScope['jakarta.servlet.error.exception']}<br>
                        <strong>URI yêu cầu:</strong> ${requestScope['jakarta.servlet.error.request_uri']}<br>
                    </div>
                </div>
                <script>
                    function toggleDebug() {
                        var panel = document.getElementById('debug-content');
                        panel.classList.toggle('show');
                    }
                </script>
            </c:if>
        </div>
    </div>
</body>
</html>
