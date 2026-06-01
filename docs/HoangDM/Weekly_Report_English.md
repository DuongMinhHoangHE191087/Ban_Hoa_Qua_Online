# WEEKLY PROGRESS REPORTS (W1 - W2 - W3)
## Project: Online Fruit Shop System (MetaFruit)
### Developer: Duong Minh Hoang (HE191087) | Group: 1
---

> [!NOTE]
> This unified progress report covers **Week 1**, **Week 2**, and **Week 3** of the SDLC life cycle. It details the transition from initial **SRS** elicitation to detailed **SDS** architectural design and the initial coding phase.

---

## 📊 WEEK 1: REQUIREMENTS ANALYSIS & SRS INITIALIZATION
**Period: 03-05-2026 to 09-05-2026**

### I. Status Report
| # | Project Task | In-Charge | Status | Notes (Work Item in Details) |
|---|---|---|---|---|
| 1 | **Overall Context Elicitation** | HaiPT | **Completed** | Defined project boundaries and mapped system actors (Customer, Shop Owner, Shipper, Admin). |
| 2 | **Context Diagram Design** | HaiPT | **Completed** | Drafted high-level context diagram representing data flows between actors and the system. |
| 3 | **Core Use Case Identification** | HaiPT / HoangDM | **Completed** | Identified and documented the master list of 103 functional requirements in English. |
| 4 | **Business Rules Elicitation** | HoangDM | **Completed** | Mapped business constraints for fruit inventory handling, discount thresholds, and refunds. |
| 5 | **Conceptual Database Schema Draft** | All Team | **Completed** | Sketched initial Entity-Relationship diagram highlighting main entities (Users, Products, Orders). |

### II. Project Issues
| # | Project Issue | Owner | Status | Notes / Solutions |
|---|---|---|---|---|
| 1 | Ambiguity in Shop Owner verification workflow | All Team | **Resolved** | *Solution:* Added a "PENDING" account status specifically for Shop Owners, requiring explicit Admin activation. |
| 2 | Designing cart persistency for guest users | HoangDM | **Resolved** | *Solution:* Opted for browser LocalStorage for guest cart states, which will synchronize with the DB upon login. |

### III. Next Week Plan
| # | Project Task | In-Charge | Deadline | Notes / Details |
|---|---|---|---|---|
| 1 | Draft Non-Functional Requirements & NFR Matrix | KhangCB | 16-05-2026 | Define performance targets, response time, and security constraints. |
| 2 | Define System Messages & App Error Codes | DuongNT | 16-05-2026 | Standardize messaging feedback for user inputs. |
| 3 | Formalize Detailed ERD Database Schema | HoangDM | 16-05-2026 | Normalize entities up to 3NF and build the physical schema matrix. |
| 4 | Design UI Wireframes & Mockups | HoangDM | 16-05-2026 | Build interactive mockups for the login, signup, and home landing pages. |

---

## 📊 WEEK 2: SRS COMPLETION & SYSTEM SPECIFICATIONS
**Period: 10-05-2026 to 16-05-2026**

### I. Status Report
| # | Project Task | In-Charge | Status | Notes (Work Item in Details) |
|---|---|---|---|---|
| 1 | **Non-Functional Requirements Specification** | KhangCB | **Completed** | Specified security (BCrypt hashing, SQL Injection guards) and performance (response time < 200ms). |
| 2 | **System Message Appendix** | DuongNT | **Completed** | Finalized mapping of user feedback errors and validation alerts. |
| 3 | **UI Prototyping & Mockups** | HoangDM | **Completed** | Built premium interactive visual prototypes for all primary pages. |
| 4 | **Detailed ERD & Database Specs** | HoangDM | **Completed** | Finalized logical design with normalized entities, data types, and primary-foreign keys. |
| 5 | **Detailed Use Case Specifications (9-50)** | Duong / Khang / Hoang | **Completed** | Fully documented normal, alternative, and exceptional flows for core use cases. |

### II. Project Issues
| # | Project Issue | Owner | Status | Notes / Solutions |
|---|---|---|---|---|
| 1 | Complexities in mapping weight variants for fresh produce | HoangDM | **Resolved** | *Solution:* Introduced a distinct `product_variants` table linked to `products` to handle SKU-level weight, pricing, and stock. |
| 2 | Concurrency issues on multi-actor checkout actions | Quan | **Resolved** | *Solution:* Engineered a dynamic checkout sequence mapping that triggers transactional rollback if items are locked. |

### III. Next Week Plan
| # | Project Task | In-Charge | Deadline | Notes / Details |
|---|---|---|---|---|
| 1 | High-Level Software Architecture Design (SDS) | HoangDM | 23-05-2026 | Formulate MVC layered pattern (Servlet -> Service -> DAO -> DB). |
| 2 | Design Package Diagrams & Folder Structures | HoangDM | 23-05-2026 | Organize packages under Jakarta Servlet 6 structure. |
| 3 | Order Life Cycle State Transition Diagram | HoangDM | 23-05-2026 | Map states (PENDING, PAID, SHIPPING, DELIVERED, CANCELLED). |
| 4 | Implement Initial DB Tables & Setup Scripts | HoangDM | 23-05-2026 | Write physical SQL Server execution scripts. |
| 5 | Code Registration, BCrypt Hashing, Local Login | HoangDM | 23-05-2026 | Start coding authentication modules. |

---

## 📊 WEEK 3: SDS ARCHITECTURE DESIGN & SECURITY IMPLEMENTATION
**Period: 17-05-2026 to 23-05-2026**

### I. Status Report
| # | Project Task | In-Charge | Status | Notes (Work Item in Details) |
|---|---|---|---|---|
| 1 | **Layered MVC Software Architecture (SDS)** | HoangDM | **Completed** | Established a strictly decopled tiered servlet/service/DAO architecture under Jakarta Servlet 6. |
| 2 | **Package Diagram & Structure Mapping** | HoangDM | **Completed** | Mapped clean package structures for separation of concerns (`dao`, `service`, `servlet`, `filter`, `util`). |
| 3 | **Database Schema Setup (`Schema.sql`)** | HoangDM | **Completed** | Wrote physical SQL scripts modeling optimized tables, constraints, indexes, and primary assets. |
| 4 | **State Transition Life Cycle Mapping** | HoangDM | **Completed** | Formulated robust state machines for transactional states (Payment and Shipping). |
| 5 | **User Registration Local Form [I.1, I.3]** | HoangDM | **Completed** | Coded standard registration backend validating email/phone inputs and hashing with BCrypt. |
| 6 | **Google OAuth 2.0 Integration [I.2, I.5]** | HoangDM | **Completed** | Integrated Google OAuth flow mapping callback tokens directly to verified accounts. |
| 7 | **Access Control Filters [I.12]** | HoangDM | **Completed** | Wrote `AuthFilter` and `RoleFilter` securing routes for `/admin/*`, `/shop/*`, and `/customer/*`. |

### II. Project Issues
| # | Project Issue | Owner | Status | Notes / Solutions |
|---|---|---|---|---|
| 1 | High risk of XSS & Session Hijacking on basic Cookie authentication | HoangDM | **Resolved** | *Solution:* Upgraded to a Dual-Token authentication mechanism using HttpOnly, Secure, and SameSite strict cookies. |
| 2 | Character encoding issues on Vietnamese servlet responses | HoangDM | **Resolved** | *Solution:* Integrated a global `EncodingFilter` positioned at the start of the `web.xml` filter chain. |

### III. Next Week Plan
| # | Project Task | In-Charge | Deadline | Notes / Details |
|---|---|---|---|---|
| 1 | Implement Dynamic Homepage & Product Details | HoangDM | 30-05-2026 | Code dynamic lists showing product images, details, and weight variants. |
| 2 | Build Interactive Hybrid Cart with AJAX | HoangDM | 30-05-2026 | Program `cart.js` AJAX triggers and DB synchronization logic. |
| 3 | Develop Product Catalog CRUD for Shop Owners | KhangCB | 30-05-2026 | Construct dashboard templates for adding and editing listings. |
| 4 | Design Checkout UI & Secure Payment Webhooks | HoangDM / Quan | 30-05-2026 | Formulate transactional checkout and prepare VietQR integration. |

---
*Verified against the repository's git commits and database schema specifications.*
**Authorized by Representative: Duong Minh Hoang (HE191087)**
