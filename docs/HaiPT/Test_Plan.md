# Ban Hoa Qua Online - Test Plan

**Project Code:** BHQO
**Document Code:** BHQO_Test_Plan_v1.0
**Date:** July 2026

## RECORD OF CHANGE
| Effective Date | Changed Items | A, M, D | Change Description | New Version |
| -------------- | ------------- | ------- | ------------------ | ----------- |
| 07/01/2026     | All           | A       | Initial Creation   | 1.0         |

---

## 1. INTRODUCTION

### 1.1 Purpose
This is the comprehensive test plan of the Ban Hoa Qua Online project. The purpose of this document is to describe the scope of tests, the testing strategy, resources, milestones, and deliverables for the testing phase of the online fruit marketplace.

### 1.2 Background information
Ban Hoa Qua Online is a Java 17 web application built using Jakarta Servlet 6, JSP, JSTL, and Tomcat 10. The platform serves as an online marketplace connecting fruit shop owners with customers seeking high-quality fresh fruits. It supports distinct user roles:
- **Customers**: Browse products, manage carts, and checkout.
- **Shop Owners**: Manage inventory, weight variants, and fulfill orders.
- **Delivery Staff**: Track and update delivery statuses.
- **Admin**: Moderate shops and handle system-wide settlements.

### 1.3 Scope of testing
The scope of testing will be limited to testing the web application via end-to-end (E2E), integration, and unit tests.

**A. Target of Test**
Functional and Non-functional requirements will be verified by the QA team and validated by the stakeholders. The primary areas include:
- Authentication & Authorization
- Shopping Cart & Checkout
- Order Placement & Tracking
- Shop Admin Dashboard & Product Customization
- Delivery Flow

**B. Test Stage**
1. **Unit Test**: Performed by the Development Team (JUnit).
2. **Integration Test**: Performed by QA. Focuses on specific areas when all component requirements are met.
3. **System Test**: Executed by QA team. Full end-to-end system testing in a pre-production environment.
4. **Acceptance Test**: Conducted by Stakeholders/Product Owner.

**C. Test Assumption**
- Product Owners will validate and approve final procedures.
- Requirements for testing are limited to those specified in Section 2.
- Testing will be executed on the specified hardware/software in Section 4.2.

### 1.4 Constraints
- Playwright E2E tests require a Chromium headless browser setup.
- Testing must occur alongside active development sprints; deadlines depend on feature completion.
- Full system testing requires an isolated pre-production database to avoid corrupting development data.

### 1.5 Risk list
| # | Description | Probability | Impact | Mitigation |
|---|-------------|-------------|--------|------------|
| 1 | Unstable third-party payment integrations during test | Medium | High | Use payment sandbox/mock APIs |
| 2 | Changes in database schema breaking regression scripts | High | Medium | Keep Playwright tests isolated and robust |

---

## 2. REQUIREMENTS FOR TEST

### 2.1 Test Items

**A. Functional Items**
1. **Authentication**
   - Customer Sign Up / Login
   - Shop Owner Registration
   - Admin Login
2. **Product Discovery**
   - Search & Advanced Filters
   - Smart Recommendations
   - Product Review & Rating
3. **Cart & Checkout**
   - Add to Cart (including Weight Variants)
   - Checkout Process
   - Online Payment Integration
4. **Order Management**
   - Customer Order Tracking
   - Shop Admin Order Management
   - Order Returns & Refunds
5. **Logistics & Admin**
   - Delivery Staff Flow
   - Shop Settlement Batch Job
   - Global Admin Monitoring

**B. Non-functional Items**
1. Data Integrity: SQL injection prevention via PreparedStatements and proper validation.
2. Performance: Pages should load within 3 seconds under normal load.
3. Scalability: System must handle up to 500 concurrent connections via Tomcat connection pooling.
4. Responsiveness: UI must display correctly on mobile and desktop viewports.

### 2.2 Acceptance Test Criteria
- **Unit Test**: 100% of unit tests pass.
- **Integration Test**: Core flows (Cart -> Checkout -> Order Creation) pass without critical defects.
- **System Test**: All E2E Playwright test scenarios pass successfully.

---

## 3. TEST STRATEGY

### 3.1 Test Types

**A. Function Testing**
- **Test Objective**: Ensure the system properly handles navigation, data entry, processing, and retrieval.
- **Technique**: Black-box testing via Playwright E2E scripts and manual test cases. Test valid/invalid data flows (e.g., placing an order with out-of-stock items).
- **Completion Criteria**: All planned test scripts execute successfully. No Severity 1/2 defects.

**B. User Interface Testing**
- **Test Objective**: Ensure responsive design rules and accessibility (e.g., tap targets > 44px).
- **Technique**: Cross-device testing (Chrome, Safari, Mobile emulators).

**C. Data and Database Integrity Testing**
- **Test Objective**: Ensure correct transaction isolation (e.g., inventory deduction during checkout).
- **Technique**: Inspect database states directly using SQL after executing backend actions.

**D. Performance and Load Testing**
- **Test Objective**: Verify response times under anticipated loads.
- **Technique**: Use JMeter to simulate 100-500 concurrent users accessing the product catalog.

**E. Security Testing**
- **Test Objective**: Application-level access control.
- **Technique**: Attempt to access Shop Owner pages via Customer accounts to verify PRG pattern and Servlet Filter protections.

**F. Regression Testing**
- **Test Objective**: Validate that new code merges do not break existing functionality.
- **Technique**: Automated Playwright test suite execution on every pull request.

### 3.2 Test Stage
| Type of Tests | Unit | Integration | System | Acceptance |
|---------------|------|-------------|--------|------------|
| Functional Tests | | X | X | X |
| Performance Tests | | | X | |
| Security Tests | | X | X | |

### 3.3 Tools
| Purpose | Tool | Vendor/In-house |
|---------|------|-----------------|
| Unit Testing | JUnit | Open Source |
| E2E / System Testing | Playwright | Microsoft |
| Load Testing | JMeter | Apache |
| CI / Automation | GitHub Actions | GitHub |

---

## 4. RESOURCE

### 4.1 Human Resource
| Worker/Role | Specific Responsibilities |
|-------------|---------------------------|
| Developers | Write JUnit tests; Fix reported defects. |
| QA Engineer | Write Playwright scripts; Manual UI testing; Performance testing. |
| Project Manager | Oversee milestones; Final acceptance testing. |

### 4.2 System
- **Development**: NetBeans IDE, Java 17, Tomcat 10.
- **Testing Node**: Node.js for Playwright.
- **Database**: SQL Server / MySQL (Testing Schema).

---

## 5. TEST MILESTONES

| Milestone | Task Effort | Start Date | End Date |
|-----------|-------------|------------|----------|
| M1 | E2E Framework Setup (Playwright) | TBD | TBD |
| M2 | Core Functional Testing (Auth & Checkout) | TBD | TBD |
| M3 | Security & Database Integrity Testing | TBD | TBD |
| M4 | UAT & Final Bug Fixing | TBD | TBD |

---

## 6. DELIVERABLES

| No | Deliverables | Delivered by | Delivered to |
|----|--------------|--------------|--------------|
| 1 | Test Plan (This document) | QA Lead | Project Manager |
| 2 | Automated E2E Scripts | QA Automation | Git Repository |
| 3 | Test Execution Report | QA Team | Project Manager |
| 4 | Defect Log | QA Team | Development Team |
