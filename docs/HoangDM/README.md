# 📁 Phân Hệ Tài Liệu Kỹ Thuật (HoangDM - HE191087)

> **Dự án**: Hệ Thống Bán Nông Sản Trực Tuyến - **MetaFruit**
> Thư mục này lưu trữ toàn bộ báo cáo triển khai thực tế, đặc tả hệ thống và kết quả nghiệm thu theo tuần của nhà phát triển Dương Minh Hoàng (HE191087).

---

## 🗺️ Bản Đồ Tài Liệu Kỹ Thuật (Documentation Index)

### 1. 📋 Báo Cáo Triển Khai Tính Năng (Sprint Reports)
Báo cáo chi tiết quá trình lập trình, sửa lỗi và thiết kế giao diện qua các giai đoạn phát triển:

*   **Authentication & Security**:
    *   [01_Authentication_Implementation_Guide.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/01_Authentication_Implementation_Guide.html) - Hướng dẫn đăng ký, đăng nhập và phân quyền.
    *   [02_Auth_Code_Implementation_Guide.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/02_Auth_Code_Implementation_Guide.html) - Cấu trúc mã nguồn xác thực.
    *   [03_Validation_Auth_Implementation_Guide.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/03_Validation_Auth_Implementation_Guide.html) - Bộ thư viện validation dữ liệu đầu vào.
    *   [04_Secure_Auth_System_Implementation_Report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/04_Secure_Auth_System_Implementation_Report.html) - Báo cáo bảo mật HttpOnly & Lockout.
    *   [05_Email_Verification_And_Google_OAuth_Plan.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/05_Email_Verification_And_Google_OAuth_Plan.html) - Thiết kế luồng Google OAuth 2.0 & Verify Email.
*   **Checkout & Shop Profile**:
    *   [06_Checkout_ShopProfile_System_Implementation_Report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/06_Checkout_ShopProfile_System_Implementation_Report.html) - Báo cáo luồng Checkout ACID, lưu địa chỉ và thông tin Shop.
*   **Delivery & Shipper Flow**:
    *   [07_Delivery_And_Shipper_Operations_Implementation_Report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/07_Delivery_And_Shipper_Operations_Implementation_Report.html) - **[HTML]** Phân hệ giao vận tự động, shipper tự chọn đơn, timeline Shopee Style.
    *   [07_Delivery_And_Shipper_Operations_Implementation_Report.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/sprint_reports/07_Delivery_And_Shipper_Operations_Implementation_Report.md) - **[Markdown]** Nhật ký triển khai chi tiết backend & frontend giao hàng.

---

### 2. 🛡️ Bảo Mật & Quản Trị Quyền Hạn (RBAC Systems)
*   [rbac_matrix_report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/security/rbac_matrix_report.html) - Ma trận phân quyền 5 vai trò hệ thống.
*   [rbac_security_report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/security/rbac_security_report.html) - Báo cáo kiểm định an toàn phân hệ RBAC Filter.
*   [auth-security-flow.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/security/auth-security-flow.html) - Sơ đồ luồng bảo mật xác thực access/refresh token.

---

### 3. ⚙️ Hướng Dẫn Vận Hành & Khắc Phục Lỗi (Deployment & Ops)
*   [README-STRUCTURE.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/README-STRUCTURE.md) - Sơ đồ cây thư mục và quy tắc viết code (Coding Rules).
*   [NETBEANS_INTEGRATION.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/NETBEANS_INTEGRATION.md) - Tích hợp môi trường phát triển NetBeans IDE.
*   [BUILD_AND_DEPLOY_GUIDE.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/BUILD_AND_DEPLOY_GUIDE.md) - Quy trình build dự án bằng Ant và deploy lên Tomcat Server.
*   [TROUBLESHOOTING.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/TROUBLESHOOTING.md) - Nhật ký sửa đổi các lỗi biên dịch và runtime phổ biến.
*   [README_DATABASE_ENRICHMENT.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/README_DATABASE_ENRICHMENT.md) - Đặc tả làm giàu cơ sở dữ liệu.
*   [Bussines_Rule.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/Bussines_Rule.md) - Các quy tắc kinh doanh cốt lõi của dự án.
*   [use_case_example.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/ops_config/use_case_example.md) - Mẫu mô tả kịch bản Use Case chuẩn.

---

### 4. 📈 Báo Cáo Tiến Độ Tuần (Weekly Progress Reports)
Báo cáo chi tiết công việc thực hiện theo chu kỳ tuần:

*   **Báo cáo Tiếng Việt**:
    *   [Weekly_Report.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/weekly_reports/Weekly_Report.md) - Cập nhật tiến trình dự án, bugs, giải pháp và kế hoạch.
    *   [Weekly_Report.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/weekly_reports/Weekly_Report.html) - Giao diện dashboard theo dõi tiến độ trực quan.
*   **Báo cáo Tiếng Anh (English Reports)**:
    *   [Weekly_Report_English.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/weekly_reports/Weekly_Report_English.md) - English documentation progress.
    *   [Weekly_Report_English.html](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/weekly_reports/Weekly_Report_English.html) - Interactive English dashboard.

---

### 5. 🔬 Phân Tích Hiệu Năng & Tác Vụ AI (Analytics & AI Logs)
*   [PERFORMANCE_COMPARISON.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/analytics/PERFORMANCE_COMPARISON.md) - So sánh hiệu năng các mức Transaction Isolation Level trong SQL Server.
*   [AI_Usage_Report.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/analytics/AI_Usage_Report.md) - Nhật ký sử dụng các công cụ trợ lý AI để tăng tốc phát triển dự án.

---

### 6. 🗄️ Lưu Trữ Lịch Sử (Archive)
*   [Thư mục Lưu trữ](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/archive/) - Chứa 19 báo cáo cũ hơn, bản thảo nháp và stylesheet phục vụ việc lưu trữ lịch sử phát triển.

---
*Mọi thay đổi tài liệu đều được đồng bộ hóa trực tiếp với Bảng Theo Dõi Tiến Độ Master của dự án.*
