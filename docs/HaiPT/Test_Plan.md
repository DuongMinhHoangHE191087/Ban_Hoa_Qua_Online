# Ban Hoa Qua Online - Test Plan (SWT301)

**Project Code:** BHQO
**Document Code:** BHQO_Test_Plan_v2.0
**Date:** July 2026

## RECORD OF CHANGE
| Effective Date | Changed Items | A, M, D | Change Description | New Version |
| -------------- | ------------- | ------- | ------------------ | ----------- |
| 01/07/2026     | All           | A       | Initial Creation   | 1.0         |
| 06/07/2026     | Multiple      | M       | Update for SWT301 Final Report (Phần 1-5) | 2.0         |

---

## 1. INTRODUCTION

### 1.1 Purpose
This is the comprehensive test plan of the Ban Hoa Qua Online project, specifically tailored for the **SWT301/SWP391 Testing Report**. The purpose of this document is to describe the scope of tests, the testing strategy, resources, milestones, and deliverables for the testing phase (01/07/2026 - 15/07/2026).

### 1.2 Background information
Ban Hoa Qua Online is a Java 17 web application built using Jakarta Servlet 6, JSP, JSTL, and Tomcat 10. The platform serves as an online marketplace connecting fruit shop owners with customers.

### 1.3 Scope of testing
The scope of testing for this phase focuses on critical components using specific software testing techniques:
1. **Unit Testing**: Validation logic (`ValidationUtil`) using Black-box (EP, BVA) and White-box (Statement/Decision Coverage).
2. **System Testing (Decision Table)**: Registration flow.
3. **System Testing (Use Case)**: Customer Checkout flow (UC-07).
4. **Automation Testing**: Login and Registration flows using Selenium WebDriver.

### 1.4 Constraints
- Time constraint: Testing must be completed within the 2-week period (01/07/2026 - 15/07/2026).
- All team members must participate in executing and reporting the tests.

---

## 2. REQUIREMENTS FOR TEST

### 2.1 Test Items

**A. Unit Test (Phần 2)**
- `ValidationUtil.isValidPassword()`
- `ValidationUtil.isValidEmail()`
- `ValidationUtil.isValidPhone()`

**B. System Test - Decision Table (Phần 3)**
- Customer Registration (`register.jsp` and `AuthService.register()`)

**C. System Test - Use Case (Phần 4)**
- Customer Checkout Flow (UC-07)

**D. Testing Tools (Phần 5)**
- Automated UI tests for Login (`login.jsp`) and Register (`register.jsp`) using Selenium WebDriver.

### 2.2 Acceptance Test Criteria
- **Unit Test**: 100% Statement and Decision coverage on selected methods.
- **System Test**: All test cases defined in Decision Tables and Use Cases are documented and executed.
- **Automation**: Selenium scripts run without errors and successfully interact with the web elements.

---

## 3. TEST STRATEGY

### 3.1 Test Types

**A. Unit Testing**
- **Technique**: Equivalence Partitioning (EP), Boundary Value Analysis (BVA), Statement Coverage, Decision Coverage.
- **Tool**: JUnit 4.13.2

**B. System Testing**
- **Technique**: Decision Table Testing, Use Case Testing.
- **Execution**: Manual execution and Automation.

**C. Automation Testing**
- **Technique**: GUI Automation.
- **Tool**: Selenium WebDriver (Java).

### 3.2 Tools
| Purpose | Tool | Vendor/In-house |
|---------|------|-----------------|
| Unit Testing | JUnit 4.13.2 | Open Source |
| Automation Testing | Selenium WebDriver | Software Freedom Conservancy |
| Build/Execution | Apache Ant / NetBeans | Open Source |

---

## 4. RESOURCE

### 4.1 Human Resource
*(Phân công báo cáo SWT301)*

| Worker/Role | Specific Responsibilities |
|-------------|---------------------------|
| **Thành viên 1** | Phần 1: Test Plan (Trình bày 5 phút) |
| **Thành viên 2** | Phần 2: Unit Test (Trình bày + Demo JUnit 5 phút) |
| **Thành viên 3** | Phần 3: System Test - Decision Table (Trình bày 5 phút) |
| **Thành viên 4** | Phần 4: System Test - Use Case (Trình bày 5 phút) |
| **Thành viên 5** | Phần 5: Testing Tools - Selenium (Trình bày + Demo 5 phút) |

### 4.2 System
- **Development**: NetBeans IDE, Java 17, Tomcat 10.
- **Testing**: Chrome Browser, ChromeDriver.

---

## 5. TEST MILESTONES

| Milestone | Task Effort | Start Date | End Date |
|-----------|-------------|------------|----------|
| M1 | Lập kế hoạch kiểm thử (Test Plan) | 01/07/2026 | 02/07/2026 |
| M2 | Thiết kế & Chạy Unit Test (EP, BVA, White-box) | 03/07/2026 | 06/07/2026 |
| M3 | Thiết kế System Test (Decision Table, Use Case) | 07/07/2026 | 10/07/2026 |
| M4 | Viết script & Chạy Automation (Selenium) | 11/07/2026 | 13/07/2026 |
| M5 | Tổng hợp báo cáo & Rehearsal | 14/07/2026 | 15/07/2026 |

---

## 6. DELIVERABLES

| No | Deliverables | Format |
|----|--------------|--------|
| 1 | Test Plan (This document) | Markdown/PDF |
| 2 | Unit Test Report (Coverage metrics, CFG) | Markdown/PDF |
| 3 | System Test Report (Decision Tables, Use Cases) | Markdown/PDF |
| 4 | Test Scripts (`.java` files for JUnit and Selenium) | Source Code |
| 5 | Presentation Slides | PowerPoint |
