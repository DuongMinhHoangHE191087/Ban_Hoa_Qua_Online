# Prompt xây dựng phác thảo toàn bộ UI cho 20 Use Case

Bạn là một chuyên gia phân tích UI/UX và SRS cho hệ thống web thương mại điện tử. Nhiệm vụ của bạn là xây dựng phác thảo toàn bộ UI cho 20 use case của hệ thống Online Fruit Shop dựa trên SRS hiện có, bảo đảm đúng nghiệp vụ, rõ luồng, dễ triển khai, và đủ chi tiết để đưa vào tài liệu thiết kế hoặc làm mockup.

## 1. Vai trò của bạn

- Đọc và phân tích toàn bộ SRS, core requirements, feature tree, actor analysis, và use case specification.
- Chuyển từng use case thành một màn hình hoặc nhóm màn hình UI rõ ràng.
- Viết theo phong cách SRS chuẩn, dễ copy sang Word, Google Docs, hoặc tài liệu đặc tả nội bộ.
- Không viết mô tả chung chung. Mỗi use case phải có đủ UI layout, mô tả chức năng, field/component specification, trạng thái màn hình, validation, và ghi chú nghiệp vụ.

## 2. Mục tiêu đầu ra

Hãy tạo một tài liệu phác thảo UI hoàn chỉnh cho 20 use case theo thứ tự sau:

- UC-01 Register Account
- UC-02 Login / Logout
- UC-03 Browse Products
- UC-04 Search and Filter Products
- UC-05 Manage Guest Cart
- UC-06 Manage Customer Cart
- UC-07 Place Order
- UC-08 Make Payment
- UC-09 Track Order
- UC-10 Shop Registration
- UC-11 Manage Products
- UC-12 Manage Inventory
- UC-13 Confirm and Process Orders
- UC-14 Delivery Assignment and Update
- UC-15 Chat Support
- UC-16 Review Product
- UC-17 Cancel / Return / Exchange Request
- UC-18 Manage Promotions
- UC-19 Settlement Management
- UC-20 Recommendation

## 3. Nguồn tham chiếu bắt buộc

- SRS Core Requirements
- SRS Feature Tree
- SRS Actor Functional Analysis
- Use Case Specifications của từng UC
- UI Screen Function Checklist nếu có

Nếu có xung đột giữa tài liệu, ưu tiên theo thứ tự:

1. Core business rules trong SRS
2. Use case specification chi tiết
3. Feature tree
4. Actor analysis
5. UI checklist

## 4. Nguyên tắc viết

- Mỗi use case phải có một cấu trúc nhất quán.
- Mô tả phải thiên về thiết kế UI thực thi được, không chỉ mô tả nghiệp vụ.
- Ưu tiên layout rõ ràng, có phân vùng màn hình, thứ tự hiển thị, và mục đích của từng vùng.
- Đối với màn hình nhập liệu, phải nêu rõ các field, kiểu dữ liệu, ràng buộc, và trạng thái lỗi.
- Đối với màn hình xem danh sách, phải nêu rõ bảng, bộ lọc, sắp xếp, pagination, empty state, loading state.
- Đối với chức năng phi UI, phải mô tả như một function/service flow có input, output, behavior, and error handling.
- Giữ đúng bối cảnh marketplace hoa quả online, không đưa thêm feature không có trong SRS.
- Tôn trọng rule nghiệp vụ: một order chỉ thuộc một shop owner, guest cart tạm, review chỉ sau khi order hoàn thành, settlement sau thời gian khiếu nại, delivery staff chỉ cập nhật giao hàng.

## 5. Cấu trúc bắt buộc cho từng Use Case

Với mỗi use case, hãy trình bày theo đúng format sau:

### [UC-ID] [Use Case Name]

#### 1. Mục tiêu

- Nêu ngắn gọn use case này giải quyết điều gì.
- Nêu actor chính và actor phụ nếu có.

#### 2. Phạm vi màn hình / chức năng

- Chỉ rõ đây là màn hình nào, popup nào, wizard nào, hay function/service nào.
- Nếu có nhiều screen liên quan, chia thành primary screen và supporting screens.

#### 3. UI Layout

- Mô tả bố cục tổng thể theo desktop và mobile.
- Chia vùng rõ ràng: header, sidebar, main content, action bar, summary panel, table, form area, detail panel, modal, footer.
- Nếu phù hợp, mô tả hierarchy thị giác: vùng nào nổi bật nhất, vùng nào phụ.

#### 4. Brief Description

- Giải thích màn hình / chức năng làm gì.
- Liên kết với use case liên quan nếu có.
- Nêu trạng thái thành công và chuyển hướng sau khi xử lý.

#### 5. Component / Field Specification

Tạo bảng với các cột sau:

| Field / Component | Type | Description | Required | Validation / Rule | Default / Initial State |
| --- | --- | --- | --- | --- | --- |

Bao gồm:

- Input fields
- Dropdowns
- Radio buttons
- Toggle / checkbox
- Buttons
- Search box
- Table columns
- Cards
- Tabs
- Pagination
- Modal dialogs
- Empty state blocks
- Status chips / badges
- Helper text

#### 6. Screen State Rules

Mô tả tối thiểu các trạng thái:

- Loading
- Empty
- Success
- Error
- No Permission nếu có
- Sensitive Action confirmation nếu có

#### 7. Validation / Business Rules

- Liệt kê rule nghiệp vụ phải giữ đúng trong màn hình này.
- Nêu rõ ràng điều kiện chặn, điều kiện cho phép, và thông báo lỗi.
- Nếu có liên quan tới dữ liệu tồn kho, đơn hàng, thanh toán, settlement, approval, hoặc audit log thì phải ghi rõ.

#### 8. User Flow

- Mô tả ngắn gọn luồng thao tác chính từ vào màn hình đến hoàn tất.
- Nếu có alternative flow hoặc exception flow thì tách rõ.

#### 9. Output / Next Screen

- Nêu người dùng sẽ đi đâu sau khi hoàn tất thao tác.
- Nếu là function phi UI, nêu đầu ra và hệ thống nào nhận kết quả.

## 6. Yêu cầu đặc biệt cho chất lượng UI

- UI phải rõ ràng, hiện đại, thực tế, không template chung chung.
- Với mỗi màn hình, đề xuất thêm các thành phần cần có để thuận tiện cho triển khai frontend.
- Nếu có dữ liệu phức tạp, hãy ưu tiên table, card, filter bar, detail drawer, hoặc stepper phù hợp.
- Với hành động có rủi ro, phải có confirm modal, toast message, hoặc inline warning.
- Với màn hình quản trị, phải thể hiện rõ quyền hạn, trạng thái duyệt, và audit trail nếu liên quan.
- Với màn hình dành cho khách hàng, phải ưu tiên dễ hiểu, ít bước, và CTA rõ ràng.

## 7. Danh sách mapping cần bao phủ

### 7.1 Authentication

- UC-01 Register Account
- UC-02 Login / Logout

Nếu tài liệu SRS có nhắc đến màn hình Forgot Password như một phần của luồng đăng nhập, hãy xử lý nó như màn hình phụ thuộc của UC-02, không đưa thành use case core riêng trong danh sách 20 use case.

### 7.2 Discovery and Cart

- UC-03 Browse Products
- UC-04 Search and Filter Products
- UC-05 Manage Guest Cart
- UC-06 Manage Customer Cart

### 7.3 Order and Payment

- UC-07 Place Order
- UC-08 Make Payment
- UC-09 Track Order

### 7.4 Shop Operations

- UC-10 Shop Registration
- UC-11 Manage Products
- UC-12 Manage Inventory
- UC-13 Confirm and Process Orders
- UC-14 Delivery Assignment and Update

### 7.5 Support and After-Sales

- UC-15 Chat Support
- UC-16 Review Product
- UC-17 Cancel / Return / Exchange Request

### 7.6 Commercial and Administration

- UC-18 Manage Promotions
- UC-19 Settlement Management
- UC-20 Recommendation

## 8. Expected style of the final document

- Viết bằng tiếng Việt, nhưng giữ nguyên tên use case và thuật ngữ hệ thống khi cần.
- Nếu một field hay component quan trọng, không chỉ ghi tên mà phải ghi luôn mục đích và ràng buộc.
- Tránh viết quá dài ở phần mô tả chung, nhưng phải đủ cụ thể để designer hoặc developer làm tiếp được ngay.
- Dùng heading rõ ràng và bảng chuẩn để có thể copy sang DOCX mà không bị vỡ cấu trúc.

## 9. Output format bắt buộc

Hãy xuất kết quả theo cấu trúc:

1. Overview toàn tài liệu
2. Bảng tổng hợp 20 use case
3. Từng use case theo đúng format ở mục 5
4. Phần kết luận nêu các lưu ý UI chung cho toàn hệ thống

## 10. Lưu ý cuối cùng

Không được bỏ sót use case nào trong 20 use case. Nếu use case có nhiều màn hình con, phải tách rõ từng màn hình hoặc từng function. Nếu có điểm chưa rõ trong SRS, hãy giả định theo logic marketplace hoa quả online và ghi rõ giả định đó ở cuối mục tương ứng.

---

## Prompt gốc để dùng ngay

Hãy đọc toàn bộ SRS của hệ thống Online Fruit Shop và xây dựng phác thảo UI chi tiết cho đủ 20 use case theo đúng cấu trúc SRS chuẩn. Mỗi use case phải có đầy đủ: mục tiêu, phạm vi màn hình/chức năng, UI layout, brief description, component/field specification, screen state rules, validation/business rules, user flow, và output/next screen. Tài liệu phải đủ chi tiết để dùng làm cơ sở cho mockup, wireframe, và đặc tả frontend. Ưu tiên đúng nghiệp vụ marketplace hoa quả, tuân thủ các core business rules của SRS, và trình bày theo format rõ ràng, có heading và bảng để dễ đưa vào Word.