# PowerShell Script to generate 32 Feature documentation files for OnlineFruitShopping
# Place and run this in the docs/HaiPT/feature folder

$features = @(
    @{
        Id = 1
        Filename = "auth_sign_up.html"
        Title = "Authentication – Đăng ký (Sign-up)"
        Iteration = 1
        Theme = "cyan"
        Desc = "Hỗ trợ khách hàng và chủ cửa hàng đăng ký tài khoản qua Form truyền thống hoặc Google OAuth, có phân quyền và kích hoạt tài khoản qua Email."
        Flow = @(
            @{ Name = "Người dùng điền form"; Desc = "Khách hàng truy cập <code>/register</code>, chọn vai trò (Customer hoặc Shop Owner), điền thông tin cá nhân và mật khẩu." },
            @{ Name = "Gửi yêu cầu đăng ký"; Desc = "Form POST tới <code>RegisterServlet</code>. Servlet thực hiện xác thực đầu vào (độ dài, định dạng email, mật khẩu khớp)." },
            @{ Name = "Tạo tài khoản tạm thời"; Desc = "Servlet gọi <code>AuthService.register()</code> -> <code>UserDAO.create()</code> để lưu user với mật khẩu băm (<code>HashUtil.hashPassword()</code>) và trạng thái <code>INACTIVE</code>." },
            @{ Name = "Khởi tạo Profile"; Desc = "Nếu vai trò là Shop Owner, hệ thống lưu thêm bản ghi trong <code>shop_owner_profiles</code> với trạng thái phê duyệt <code>PENDING</code>." },
            @{ Name = "Sinh Token kích hoạt"; Desc = "Hệ thống tạo mã kích hoạt bảo mật ngẫu nhiên, lưu mã băm và hạn sử dụng (24h) vào <code>users</code>." },
            @{ Name = "Gửi Mail xác minh"; Desc = "Gọi <code>EmailService</code> gửi mail chứa link kích hoạt (<code>/verify?code=...</code>) tới hòm thư người dùng." },
            @{ Name = "Kích hoạt tài khoản"; Desc = "Người dùng click link -> <code>VerifyEmailServlet</code> kiểm tra token. Nếu hợp lệ, đổi trạng thái user thành <code>ACTIVE</code>." }
        )
        Classes = @(
            @{ Name = "RegisterServlet"; Type = "Servlet"; Desc = "Tiếp nhận dữ liệu đăng ký (email, fullName, phone, password, role) từ form POST. Thực hiện xác thực định dạng đầu vào và gọi AuthService.register() để xử lý đăng ký." },
            @{ Name = "VerifyEmailServlet"; Type = "Servlet"; Desc = "Xác minh token kích hoạt (code) truyền từ URL GET. Đối chiếu hạn sử dụng và kích hoạt trạng thái tài khoản thành ACTIVE." },
            @{ Name = "GoogleLoginServlet"; Type = "Servlet"; Desc = "Khởi tạo tiến trình xác thực Google OAuth 2.0 bằng cách chuyển hướng trình duyệt sang Google Account Selector API." },
            @{ Name = "GoogleCallbackServlet"; Type = "Servlet"; Desc = "Tiếp nhận mã code từ callback Google. Gọi Google API lấy email và full name để thực hiện đăng nhập hoặc tự động tạo tài khoản mới nếu chưa tồn tại." },
            @{ Name = "AuthService"; Type = "Service"; Desc = "Chứa logic lõi của đăng ký, băm mật khẩu qua HashUtil, sinh mã kích hoạt email, thiết lập thời hạn mã (24 giờ), phối hợp giữa UserDAO và ShopProfileDAO trong cùng một phiên đăng ký." },
            @{ Name = "UserService"; Type = "Service"; Desc = "Cung cấp hàm kiểm tra trùng lặp email (isEmailExists()) và cập nhật cờ trạng thái tài khoản." },
            @{ Name = "EmailService"; Type = "Service"; Desc = "Xây dựng nội dung email kích hoạt sử dụng mẫu HTML từ EmailTemplateService, kết nối máy chủ SMTP và thực hiện gửi email xác thực tài khoản." },
            @{ Name = "UserDAO"; Type = "DAO"; Desc = "Thực hiện chèn thông tin tài khoản mới (status = 'INACTIVE') và cập nhật trạng thái ACTIVE sau khi xác minh email thành công." },
            @{ Name = "ShopProfileDAO"; Type = "DAO"; Desc = "Khởi tạo hồ sơ rỗng trong bảng shop_owner_profiles với trạng thái PENDING khi người đăng ký chọn vai trò chủ cửa hàng." },
            @{ Name = "User"; Type = "Entity"; Desc = "Đối tượng Java ánh xạ bảng users lưu trữ trạng thái tài khoản đăng ký." },
            @{ Name = "register.jsp"; Type = "JSP"; Desc = "Trang giao diện đăng ký tài khoản với validator kiểm tra ràng buộc mật khẩu và định dạng phía client." },
            @{ Name = "users"; Type = "DB Table"; Desc = "Bảng cơ sở dữ liệu lưu giữ thông tin tài khoản đăng ký, khóa chính user_id, vai trò và thông tin xác thực email." }
        )
    },
    @{
        Id = 2
        Filename = "auth_login_logout.html"
        Title = "Authentication – Đăng nhập & Đăng xuất (Login/Logout)"
        Iteration = 1
        Theme = "blue"
        Desc = "Quản lý đăng nhập cục bộ (Email/Password), đăng nhập Google OAuth, đăng xuất an toàn và cơ chế bảo mật khóa tài khoản tạm thời (Login Lockout)."
        Flow = @(
            @{ Name = "Nhập thông tin"; Desc = "Người dùng nhập Email/Mật khẩu trên form đăng nhập (<code>/login</code>) hoặc nhấn chọn 'Đăng nhập với Google'." },
            @{ Name = "Xử lý Đăng nhập"; Desc = "Yêu cầu POST tới <code>LoginServlet</code>. Hệ thống truy vấn thông tin user và kiểm tra xem tài khoản có đang bị khóa (<code>locked_until</code>) hay không." },
            @{ Name = "Xác thực mật khẩu"; Desc = "Nếu tài khoản không bị khóa, gọi <code>HashUtil.verifyPassword()</code> để đối chiếu mật khẩu nhập vào với mật khẩu băm trong cơ sở dữ liệu." },
            @{ Name = "Xử lý thất bại (Lockout)"; Desc = "Nếu sai mật khẩu, gọi <code>UserLockManager</code> tăng <code>failed_login_count</code>. Đạt 5 lần sai liên tiếp, khóa tài khoản 15 phút (cập nhật <code>locked_until</code>)." },
            @{ Name = "Xử lý thành công"; Desc = "Nếu đúng mật khẩu, đặt lại số lần sai về 0, tạo Token Session qua <code>TokenUtil</code>, lưu DB <code>user_sessions</code> và ghi cookie trình duyệt." },
            @{ Name = "Đăng xuất (Logout)"; Desc = "Khách hàng click Đăng xuất -> <code>LogoutServlet</code> xóa cookie trình duyệt, hủy session trên Java và xóa bản ghi tương ứng trong <code>user_sessions</code>." }
        )
        Classes = @(
            @{ Name = "LoginServlet"; Type = "Servlet"; Desc = "Đọc email và mật khẩu từ form POST. Kiểm tra xem tài khoản có đang bị khóa (locked_until), đối chiếu thông tin đăng nhập và tạo session khi đăng nhập thành công." },
            @{ Name = "LogoutServlet"; Type = "Servlet"; Desc = "Xóa thông tin session (currentUser) ở client (cookie) và server (xóa token tương ứng trong bảng user_sessions)." },
            @{ Name = "AuthService"; Type = "Service"; Desc = "Xử lý logic xác thực mật khẩu qua HashUtil, cấp token và ghi nhận phiên đăng nhập vào bảng user_sessions." },
            @{ Name = "UserLockManager"; Type = "Utility"; Desc = "Quản lý số lần đăng nhập sai (tối đa 5 lần) và ghi nhận mốc thời gian khóa tài khoản tạm thời (15 phút) vào cột locked_until." },
            @{ Name = "TokenUtil"; Type = "Utility"; Desc = "Sinh chuỗi token ngẫu nhiên, độ an toàn cao để định danh session lưu cookie." },
            @{ Name = "HashUtil"; Type = "Utility"; Desc = "Hỗ trợ băm mật khẩu và đối chiếu mật khẩu an toàn sử dụng các thuật toán băm bảo mật." },
            @{ Name = "UserSessionDAO"; Type = "DAO"; Desc = "Thực hiện các câu lệnh SQL để truy vấn và xóa phiên (DELETE FROM user_sessions WHERE token = ?), hỗ trợ tự động dọn dẹp các phiên đã hết hạn." },
            @{ Name = "UserSession"; Type = "Entity"; Desc = "Entity ánh xạ bảng user_sessions, liên kết khóa ngoại với bảng users để quản lý token." },
            @{ Name = "login.jsp"; Type = "JSP"; Desc = "Giao diện đăng nhập hiển thị thông báo lỗi động và tích hợp nút chọn đăng nhập Google." },
            @{ Name = "user_sessions"; Type = "DB Table"; Desc = "Bảng lưu vết các token đăng nhập còn hiệu lực, kiểm soát trạng thái đăng nhập đồng thời." }
        )
    },
    @{
        Id = 3
        Filename = "user_profile_management.html"
        Title = "User Profile – Quản lý hồ sơ & Địa chỉ"
        Iteration = 1
        Theme = "cyan"
        Desc = "Cho phép người dùng xem, cập nhật thông tin cá nhân (Họ tên, Số điện thoại, Ảnh đại diện) và quản lý danh sách địa chỉ nhận hàng cá nhân."
        Flow = @(
            @{ Name = "Xem trang cá nhân"; Desc = "Truy cập <code>/profile</code> -> <code>UserProfileServlet</code> lấy thông tin người dùng hiện tại từ Session và chuyển hướng đến trang cập nhật." },
            @{ Name = "Cập nhật thông tin"; Desc = "Người dùng thay đổi thông tin cá nhân và nhấn Lưu. Yêu cầu POST lên <code>UserProfileServlet</code> để cập nhật cột tương ứng." },
            @{ Name = "Quản lý địa chỉ"; Desc = "Tại mục địa chỉ, người dùng có thể Thêm mới, Sửa hoặc Xóa địa chỉ. Yêu cầu gửi AJAX tới <code>AddressAPIServlet</code>." },
            @{ Name = "Đặt địa chỉ mặc định"; Desc = "Khi chọn mặc định một địa chỉ mới, API gọi <code>UserAddressDAO.setDefaultAddress()</code> để đặt <code>is_default = 0</code> cho toàn bộ địa chỉ cũ, và đặt <code>is_default = 1</code> cho địa chỉ được chọn." }
        )
        Classes = @(
            @{ Name = "UserProfileServlet"; Type = "Servlet"; Desc = "Hiển thị thông tin cá nhân người dùng và tiếp nhận cập nhật (Họ tên, Số điện thoại, Ảnh đại diện thông qua multipart request)." },
            @{ Name = "AddressAPIServlet"; Type = "Servlet"; Desc = "API endpoint đón nhận các cuộc gọi AJAX (POST/PUT/DELETE) của khách hàng để quản lý danh sách địa chỉ nhận hàng." },
            @{ Name = "UserService"; Type = "Service"; Desc = "Quản lý nghiệp vụ liên quan đến chỉnh sửa thông tin cá nhân người dùng và xác thực tính hợp lệ của số điện thoại." },
            @{ Name = "UserAddressDAO"; Type = "DAO"; Desc = "Chứa các phương thức SQL thực hiện CRUD địa chỉ và điều hành logic đồng bộ cờ is_default (khi một địa chỉ được đặt mặc định, các địa chỉ khác của user đó phải bị hạ cờ mặc định)." },
            @{ Name = "UserAddress"; Type = "Entity"; Desc = "Entity ánh xạ bảng user_addresses, chứa các thông tin liên kết địa chỉ nhận hàng của khách." },
            @{ Name = "profile.jsp"; Type = "JSP"; Desc = "Giao diện cập nhật hồ sơ cá nhân và quản lý danh sách địa chỉ nhận hàng sử dụng AJAX chỉnh sửa nhanh." },
            @{ Name = "user_addresses"; Type = "DB Table"; Desc = "Bảng cơ sở dữ liệu lưu trữ nhiều địa chỉ giao hàng của mỗi người dùng, liên kết khóa ngoại đến bảng users." }
        )
    },
    @{
        Id = 4
        Filename = "security_password.html"
        Title = "Security – Đổi mật khẩu & Khôi phục mật khẩu"
        Iteration = 1
        Theme = "red"
        Desc = "Đảm bảo an toàn thông tin qua chức năng đổi mật khẩu trực tiếp hoặc khôi phục mật khẩu qua liên kết xác thực gửi tới Email khi quên mật khẩu."
        Flow = @(
            @{ Name = "Yêu cầu Đổi mật khẩu"; Desc = "Người dùng đã đăng nhập điền mật khẩu cũ và mới tại <code>/change-password</code>, gửi yêu cầu tới <code>ChangePasswordServlet</code>." },
            @{ Name = "Cập nhật mật khẩu mới"; Desc = "Servlet xác thực mật khẩu cũ. Nếu đúng, băm mật khẩu mới và lưu vào cơ sở dữ liệu." },
            @{ Name = "Quên mật khẩu"; Desc = "Tại trang đăng nhập, người dùng nhấn quên mật khẩu, nhập email. <code>ForgotPasswordServlet</code> kiểm tra email tồn tại." },
            @{ Name = "Gửi mail khôi phục"; Desc = "Hệ thống tạo mã reset token, băm và lưu vào DB với thời gian hết hạn (15 phút), đồng thời gửi mail chứa link khôi phục." },
            @{ Name = "Xác minh Token"; Desc = "Người dùng click link khôi phục -> <code>ForgotVerifyServlet</code> xác thực token chưa hết hạn. Nếu hợp lệ, chuyển hướng sang trang đặt lại mật khẩu." },
            @{ Name = "Đặt lại mật khẩu"; Desc = "Người dùng nhập mật khẩu mới tại <code>/reset-password</code> -> <code>ResetPasswordServlet</code> băm mật khẩu mới, cập nhật vào DB và hủy token." }
        )
        Classes = @(
            @{ Name = "ChangePasswordServlet"; Type = "Servlet"; Desc = "Xử lý đổi mật khẩu cho người dùng đang trực tuyến bằng cách so khớp mật khẩu hiện tại và lưu mật khẩu mới đã băm." },
            @{ Name = "ForgotPasswordServlet"; Type = "Servlet"; Desc = "Tiếp nhận yêu cầu quên mật khẩu, phát sinh mã xác minh, lưu DB và kích hoạt gửi email khôi phục mật khẩu." },
            @{ Name = "ForgotVerifyServlet"; Type = "Servlet"; Desc = "Nhận token khôi phục mật khẩu từ liên kết email, so khớp trong DB và kiểm tra xem mã đã hết hạn chưa." },
            @{ Name = "ResetPasswordServlet"; Type = "Servlet"; Desc = "Cập nhật mật khẩu băm mới sau khi người dùng xác thực liên kết thành công và xóa bỏ token khôi phục." },
            @{ Name = "AuthService"; Type = "Service"; Desc = "Chuyển giao các lệnh đổi mật khẩu và quản lý chu kỳ sống/kiểm duyệt token khôi phục mật khẩu." },
            @{ Name = "EmailService"; Type = "Service"; Desc = "Soạn thảo và gửi thư điện tử chứa liên kết khôi phục mật khẩu đến tài khoản yêu cầu." },
            @{ Name = "UserDAO"; Type = "DAO"; Desc = "Cập nhật mật khẩu băm mới và lưu thông tin token khôi phục mật khẩu tạm thời." },
            @{ Name = "change-password.jsp"; Type = "JSP"; Desc = "Giao diện thay đổi mật khẩu trực tiếp dành cho người dùng trực tuyến." },
            @{ Name = "forgot-password.jsp"; Type = "JSP"; Desc = "Giao diện yêu cầu nhập email khi khách hàng quên mật khẩu." },
            @{ Name = "reset-password.jsp"; Type = "JSP"; Desc = "Giao diện thiết lập lại mật khẩu mới sau khi xác thực liên kết khôi phục thành công." }
        )
    },
    @{
        Id = 5
        Filename = "security_access_control.html"
        Title = "Security – Phân quyền truy cập (RBAC)"
        Iteration = 1
        Theme = "orange"
        Desc = "Kiểm soát truy cập dựa trên vai trò (CUSTOMER, SHOP_OWNER, DELIVERY, ADMIN) thông qua Filter bảo mật và thẻ phân quyền JSTL Tag."
        Flow = @(
            @{ Name = "Gửi yêu cầu URL"; Desc = "Trình duyệt gửi yêu cầu truy cập tài nguyên (Ví dụ: <code>/shop/inventory</code>)." },
            @{ Name = "Kiểm tra đăng nhập"; Desc = "<code>AuthFilter</code> đánh chặn yêu cầu. Nếu chưa đăng nhập và URL không thuộc danh sách công khai (whitelist), chuyển hướng về trang Login." },
            @{ Name = "Kiểm tra quyền hạn"; Desc = "<code>RoleFilter</code> tiếp tục kiểm tra quyền dựa trên tiền tố URL (Ví dụ: đường dẫn <code>/shop/*</code> yêu cầu vai trò <code>SHOP_OWNER</code>)." },
            @{ Name = "Xử lý lỗi chặn quyền"; Desc = "Nếu vai trò của người dùng không phù hợp, filter từ chối yêu cầu và forward sang trang lỗi <code>403.jsp</code> (Forbidden)." },
            @{ Name = "Ẩn/Hiện giao diện"; Desc = "Trong các trang JSP, thẻ tùy chỉnh <code>&lt;f:permission&gt;</code> kiểm tra vai trò người dùng trong session để quyết định hiển thị các nút thao tác tương ứng." }
        )
        Classes = @(
            @{ Name = "AuthFilter"; Type = "Filter"; Desc = "Đánh chặn mọi yêu cầu, kiểm tra session đăng nhập và tự động cookie login để phục hồi trạng thái người dùng." },
            @{ Name = "RoleFilter"; Type = "Filter"; Desc = "Kiểm tra vai trò người dùng (CUSTOMER, SHOP_OWNER, DELIVERY, ADMIN) khớp với đường dẫn phân vùng bảo vệ tương ứng." },
            @{ Name = "CsrfFilter"; Type = "Filter"; Desc = "Xác thực token CSRF trên các phương thức POST/PUT để chống giả mạo yêu cầu từ trang web khác." },
            @{ Name = "RateLimitFilter"; Type = "Filter"; Desc = "Hạn chế số lượng yêu cầu truy cập từ một IP trong một khoảng thời gian nhằm ngăn ngừa spam." },
            @{ Name = "PermissionTag"; Type = "Tag"; Desc = "Thẻ tùy chỉnh trong JSP thực hiện ẩn/hiện giao diện (các nút Sửa, Xóa) dựa trên vai trò của người dùng trong session." },
            @{ Name = "BaseHttpServlet"; Type = "Servlet"; Desc = "Servlet cơ sở hỗ trợ các Servlet con lấy nhanh đối tượng người dùng hiện tại từ session." },
            @{ Name = "403.jsp"; Type = "JSP"; Desc = "Giao diện hiển thị thông báo lỗi chặn quyền truy cập khi người dùng vào vùng cấm." }
        )
    },
    @{
        Id = 6
        Filename = "product_discovery.html"
        Title = "Product Discovery – Khám phá sản phẩm cơ bản"
        Iteration = 1
        Theme = "green"
        Desc = "Hỗ trợ khách hàng tìm kiếm trái cây theo tên, duyệt theo danh mục sản phẩm, hiển thị sản phẩm nổi bật và xem chi tiết thông tin trái cây."
        Flow = @(
            @{ Name = "Truy cập hệ thống"; Desc = "Người dùng truy cập trang chủ (<code>HomeServlet</code>) hoặc danh sách sản phẩm (<code>ProductListServlet</code>)." },
            @{ Name = "Tự động quét hết hạn"; Desc = "Trước khi query, <code>ProductService</code> kích hoạt <code>ProductDAO.autoDeactivateExpiredProducts()</code> để cập nhật các sản phẩm quá hạn sử dụng sang trạng thái <code>OUT_OF_SEASON</code>." },
            @{ Name = "Lọc & Tìm kiếm"; Desc = "Người dùng nhập từ khóa tìm kiếm hoặc bấm chọn danh mục. <code>ProductDAO</code> truy vấn các sản phẩm <code>ACTIVE</code> phù hợp." },
            @{ Name = "Xem chi tiết"; Desc = "Khách hàng click vào sản phẩm -> <code>ProductDetailServlet</code> truy vấn thông tin chi tiết, tăng view count trong DB và hiển thị lên giao diện." }
        )
        Classes = @(
            @{ Name = "HomeServlet"; Type = "Servlet"; Desc = "Lấy dữ liệu danh mục, sản phẩm nổi bật và sản phẩm mới đăng tải để phục vụ giao diện trang chủ." },
            @{ Name = "ProductListServlet"; Type = "Servlet"; Desc = "Tiếp nhận tham số tìm kiếm tên trái cây, lọc theo danh mục và điều phối phân trang hiển thị." },
            @{ Name = "ProductDetailServlet"; Type = "Servlet"; Desc = "Truy xuất chi tiết một sản phẩm, cộng dồn số lượt xem trong DB và chuyển hướng tới trang chi tiết." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Điều phối các nghiệp vụ sản phẩm và kích hoạt tự động quét hết hạn trước khi query." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Thực hiện tìm kiếm sản phẩm theo tên, lọc theo danh mục, tăng view và quét đổi trạng thái hết hạn tự động." },
            @{ Name = "CategoryDAO"; Type = "DAO"; Desc = "Lấy danh sách các danh mục sản phẩm đang hoạt động được sắp xếp theo display_order." },
            @{ Name = "Product"; Type = "Entity"; Desc = "Đối tượng Java ánh xạ bảng products chứa thông tin chi tiết của trái cây." },
            @{ Name = "home.jsp"; Type = "JSP"; Desc = "Giao diện trang chủ hiển thị các nhóm sản phẩm theo mùa và danh mục." },
            @{ Name = "product-list.jsp"; Type = "JSP"; Desc = "Giao diện lưới sản phẩm tích hợp công cụ tìm kiếm và lọc danh mục." },
            @{ Name = "product-detail.jsp"; Type = "JSP"; Desc = "Giao diện hiển thị chi tiết hình ảnh, nguồn gốc, xuất xứ và thời hạn bảo quản của trái cây." }
        )
    },
    @{
        Id = 7
        Filename = "admin_user_management.html"
        Title = "Admin User Management – Quản trị người dùng & Phê duyệt Shop"
        Iteration = 1
        Theme = "red"
        Desc = "Bảng điều khiển dành cho Admin để quản lý danh sách tài khoản khách hàng, chủ cửa hàng, khóa tài khoản vi phạm và phê duyệt hồ sơ mở shop."
        Flow = @(
            @{ Name = "Vào trang quản trị"; Desc = "Admin truy cập đường dẫn <code>/admin/users</code> hoặc <code>/admin/shops</code>." },
            @{ Name = "Truy vấn danh sách"; Desc = "<code>UserManageServlet</code> và <code>AdminShopManageServlet</code> lấy dữ liệu người dùng/hồ sơ shop hiển thị lên bảng quản trị." },
            @{ Name = "Khóa tài khoản"; Desc = "Admin bấm Khóa tài khoản -> AJAX gửi tới <code>AdminUserStatusAPI</code> để cập nhật trạng thái user thành <code>LOCKED</code> trong DB." },
            @{ Name = "Thu hồi phiên làm việc"; Desc = "API gọi tiếp <code>AdminUserRevokeSessionsAPI</code> để xóa sạch token trong <code>user_sessions</code>, ép tài khoản đó đăng xuất ngay lập tức." },
            @{ Name = "Phê duyệt mở shop"; Desc = "Admin xem hồ sơ đăng ký tại <code>ShopApprovalServlet</code>, nhấn Phê duyệt -> <code>ShopApprovalAPI</code> cập nhật trạng thái shop thành <code>APPROVED</code> và đổi role của user thành <code>SHOP_OWNER</code>." }
        )
        Classes = @(
            @{ Name = "UserManageServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách khách hàng, chủ shop phân trang cho Admin kèm theo bộ lọc trạng thái tài khoản." },
            @{ Name = "AdminUserStatusAPI"; Type = "Servlet"; Desc = "API tiếp nhận yêu cầu thay đổi trạng thái hoạt động (ACTIVE/LOCKED) của người dùng từ Admin." },
            @{ Name = "AdminUserRevokeSessionsAPI"; Type = "Servlet"; Desc = "API thực hiện xóa bỏ toàn bộ token phiên đăng nhập trong DB để buộc tài khoản vi phạm đăng xuất ngay." },
            @{ Name = "ShopApprovalServlet"; Type = "Servlet"; Desc = "Xem hồ sơ mở cửa hàng bao gồm tài liệu chứng minh và đơn đăng ký chờ xét duyệt." },
            @{ Name = "ShopApprovalAPI"; Type = "Servlet"; Desc = "API tiếp nhận quyết định duyệt hoặc từ chối đơn đăng ký mở shop từ Admin." },
            @{ Name = "UserService"; Type = "Service"; Desc = "Cung cấp nghiệp vụ khóa tài khoản và thu hồi quyền đăng nhập đồng thời." },
            @{ Name = "ShopService"; Type = "Service"; Desc = "Cung cấp nghiệp vụ duyệt hồ sơ shop và tự động đổi vai trò của người dùng thành SHOP_OWNER." },
            @{ Name = "UserDAO"; Type = "DAO"; Desc = "Truy vấn, lọc danh sách người dùng và cập nhật trạng thái tài khoản trong DB." },
            @{ Name = "ShopProfileDAO"; Type = "DAO"; Desc = "Cập nhật trạng thái duyệt hồ sơ shop và lý do từ chối nếu có." },
            @{ Name = "user-list.jsp"; Type = "JSP"; Desc = "Giao diện danh sách tài khoản người dùng tích hợp nút khóa nhanh bằng AJAX." },
            @{ Name = "shop-approvals.jsp"; Type = "JSP"; Desc = "Giao diện quản lý hồ sơ đăng ký mở cửa hàng đang chờ phê duyệt." }
        )
    },
    @{
        Id = 8
        Filename = "product_catalog_management.html"
        Title = "Product Catalog – Quản lý danh mục & Sản phẩm (CRUD)"
        Iteration = 2
        Theme = "green"
        Desc = "Cung cấp cho Chủ cửa hàng đầy đủ công cụ để thêm mới sản phẩm, thiết lập biến thể hàng hóa, cập nhật thông tin và thực hiện xóa mềm sản phẩm."
        Flow = @(
            @{ Name = "Truy cập kho sản phẩm"; Desc = "Chủ shop mở trang <code>/shop/products</code> để quản lý toàn bộ danh sách trái cây đang bán của mình." },
            @{ Name = "Tạo mới sản phẩm"; Desc = "Chủ shop điền thông tin chi tiết (tên, xuất xứ, hạn sử dụng, các biến thể trọng lượng và giá tiền) và tải lên hình ảnh." },
            @{ Name = "Xử lý hình ảnh"; Desc = "<code>ProductCreateServlet</code> nhận yêu cầu, dùng <code>FileUploadUtil</code> lưu file ảnh vào thư mục máy chủ và trả về đường dẫn tương đối." },
            @{ Name = "Thực thi Transaction tạo"; Desc = "<code>ProductService.createProduct()</code> chạy transaction: Lưu thông tin sản phẩm -> Lưu danh sách biến thể trong <code>product_variants</code> -> Lưu đường dẫn ảnh trong <code>product_images</code>." },
            @{ Name = "Xóa mềm sản phẩm"; Desc = "Khi bấm Xóa, <code>ProductStatusServlet</code> cập nhật trạng thái sản phẩm trong DB thành <code>DELETED</code> thay vì xóa vật lý, giữ toàn vẹn dữ liệu cho các đơn hàng cũ." }
        )
        Classes = @(
            @{ Name = "ProductManageServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách sản phẩm thuộc quyền sở hữu của chủ shop, tích hợp bộ lọc trạng thái và tìm kiếm." },
            @{ Name = "ProductCreateServlet"; Type = "Servlet"; Desc = "Nhận thông tin form và ảnh tải lên để tạo sản phẩm mới cùng các biến thể giá bán tương ứng." },
            @{ Name = "ProductEditServlet"; Type = "Servlet"; Desc = "Nhận thông tin cập nhật thông số sản phẩm và xử lý cập nhật dữ liệu." },
            @{ Name = "ProductStatusServlet"; Type = "Servlet"; Desc = "API tiếp nhận yêu cầu ẩn sản phẩm hoặc xóa mềm (cập nhật trạng thái DELETED)." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Quản lý transaction thêm/sửa sản phẩm đồng bộ các bảng biến thể và hình ảnh an toàn." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Thực hiện CRUD dữ liệu trên bảng products và kiểm tra quyền sở hữu sản phẩm." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Thực hiện thêm mới, cập nhật giá, tồn kho và trạng thái hoạt động của các biến thể sản phẩm." },
            @{ Name = "ProductImageDAO"; Type = "DAO"; Desc = "Lưu trữ thông tin đường dẫn các hình ảnh mô tả sản phẩm và ảnh đại diện." },
            @{ Name = "FileUploadUtil"; Type = "Utility"; Desc = "Kiểm tra định dạng file ảnh và lưu trữ file ảnh lên ổ đĩa của máy chủ." },
            @{ Name = "product-list.jsp"; Type = "JSP"; Desc = "Giao diện làm việc của chủ shop để quản lý sản phẩm." }
        )
    },
    @{
        Id = 9
        Filename = "product_discovery_advanced.html"
        Title = "Product Discovery – Khám phá sản phẩm nâng cao"
        Iteration = 2
        Theme = "cyan"
        Desc = "Trang chi tiết sản phẩm nâng cao tích hợp thư viện ảnh trình chiếu (slideshow) và lựa chọn biến thể sản phẩm động cập nhật giá thời gian thực."
        Flow = @(
            @{ Name = "Yêu cầu chi tiết"; Desc = "Khách hàng bấm chọn một sản phẩm bất kỳ trên storefront." },
            @{ Name = "Tải thông tin tổng hợp"; Desc = "<code>ProductDetailServlet</code> nhận yêu cầu, truy vấn đồng thời thông tin sản phẩm, danh sách hình ảnh trình chiếu và các biến thể phân loại trọng lượng." },
            @{ Name = "Render giao diện"; Desc = "Servlet chuyển tiếp dữ liệu đến <code>product-detail.jsp</code> để vẽ khung chi tiết sản phẩm." },
            @{ Name = "Chuyển đổi biến thể"; Desc = "Khi khách hàng click chọn phân loại khác (Ví dụ: từ Hộp 1kg sang Hộp 500g), mã JavaScript trên client tự động đọc thuộc tính giá và cập nhật hiển thị giá mới cùng số lượng tồn kho tương ứng." }
        )
        Classes = @(
            @{ Name = "ProductDetailServlet"; Type = "Servlet"; Desc = "Tải thông tin sản phẩm, danh sách biến thể còn bán, danh sách hình ảnh để vẽ slideshow cho trang chi tiết." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Cung cấp hàm tích hợp lấy thông tin chi tiết sản phẩm, ảnh mô tả và biến thể giá bán." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Đọc thông tin chi tiết cơ bản của sản phẩm trái cây trong DB." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Lấy toàn bộ các biến thể phân loại trọng lượng còn hoạt động của sản phẩm." },
            @{ Name = "ProductImageDAO"; Type = "DAO"; Desc = "Lấy danh sách ảnh liên quan đến sản phẩm để vẽ thư viện trình chiếu ảnh." },
            @{ Name = "ProductVariant"; Type = "Entity"; Desc = "Entity đại diện cho một SKU biến thể của sản phẩm trái cây." },
            @{ Name = "product-detail.jsp"; Type = "JSP"; Desc = "Trang chi tiết sản phẩm nâng cao hỗ trợ chọn biến thể động bằng JavaScript cập nhật giá và tồn kho." }
        )
    },
    @{
        Id = 10
        Filename = "product_customization.html"
        Title = "Product Customization – Nhãn & Tùy chọn đóng gói sản phẩm"
        Iteration = 2
        Theme = "green"
        Desc = "Quản lý nhãn sản phẩm (Hữu cơ - Organic, Nhập khẩu - Imported), cấu hình mùa vụ bán hàng và các tùy chọn đóng gói đi kèm (như hộp quà gỗ, túi giấy sinh học)."
        Flow = @(
            @{ Name = "Chủ shop thiết lập nhãn"; Desc = "Khi tạo/sửa sản phẩm, chủ shop đánh dấu cờ <code>is_organic</code>, <code>is_imported</code> và chọn các tháng thu hoạch mùa vụ." },
            @{ Name = "Tạo tùy chọn đóng gói"; Desc = "Chủ shop thiết lập danh sách đóng gói tặng kèm với chi phí cộng thêm (Ví dụ: 'Hộp gỗ cao cấp' + 50.000 VNĐ) tại <code>product_packaging_options</code>." },
            @{ Name = "Hiển thị nhãn trên UI"; Desc = "Trang chi tiết sản phẩm hiển thị nổi bật các tag Organic, Imported và hiển thị cảnh báo nếu tháng hiện tại nằm ngoài mùa vụ thu hoạch." },
            @{ Name = "Khách hàng lựa chọn"; Desc = "Khách hàng chọn biến thể đóng gói yêu thích. Giá trị phụ thu đóng gói được hiển thị rõ ràng và cộng dồn vào tổng tiền thanh toán tạm tính." }
        )
        Classes = @(
            @{ Name = "ProductCreateServlet"; Type = "Servlet"; Desc = "Nhận thông tin cờ Organic, Imported, mùa vụ và các tùy chọn đóng gói đi kèm để lưu trữ khi tạo sản phẩm." },
            @{ Name = "ProductEditServlet"; Type = "Servlet"; Desc = "Cập nhật các tùy chọn phụ thu đóng gói và các nhãn phân loại của sản phẩm." },
            @{ Name = "ProductDetailServlet"; Type = "Servlet"; Desc = "Truy xuất danh sách gói đóng gói đi kèm hiển thị lên form chọn lựa mua hàng của khách." },
            @{ Name = "ProductPackagingOptionDAO"; Type = "DAO"; Desc = "Truy vấn và thay đổi các cấu hình đóng gói đi kèm của sản phẩm trong DB." },
            @{ Name = "ProductPackagingOption"; Type = "Entity"; Desc = "Entity ánh xạ bảng product_packaging_options lưu cấu hình phụ thu đóng gói." },
            @{ Name = "product-detail.jsp"; Type = "JSP"; Desc = "Giao diện hiển thị nhãn chứng nhận và hộp thoại chọn lựa tùy chọn đóng gói phụ thu đi kèm." }
        )
    },
    @{
        Id = 11
        Filename = "stock_inventory_control.html"
        Title = "Stock & Inventory – Quản lý kho hàng & Cảnh báo tồn kho"
        Iteration = 2
        Theme = "blue"
        Desc = "Theo dõi số lượng tồn kho của từng biến thể sản phẩm, ghi lại nhật ký biến động kho (Inventory Logs) và đưa ra cảnh báo khi tồn kho chạm ngưỡng tối thiểu."
        Flow = @(
            @{ Name = "Kiểm tra tồn kho"; Desc = "Chủ shop theo dõi danh sách tồn kho tại <code>/shop/inventory</code>. Các biến thể có lượng tồn dưới ngưỡng tối thiểu sẽ hiển thị nhãn đỏ cảnh báo." },
            @{ Name = "Thực hiện Nhập kho"; Desc = "Chủ shop nhập số lượng bổ sung và ngày thu hoạch mới, nhấn Xác nhận." },
            @{ Name = "Thực thi Transaction kho"; Desc = "<code>InventoryService.restock()</code> chạy transaction:<br>1. Cộng số lượng vào bảng <code>product_variants</code>.<br>2. Ghi một dòng nhật ký vào bảng <code>inventory_logs</code> với kiểu <code>MANUAL_ADJUST</code>.<br>3. Tự động chuyển trạng thái sản phẩm từ <code>OUT_OF_SEASON</code> về lại <code>ACTIVE</code> nếu đang bị tạm ẩn do hết mùa." }
        )
        Classes = @(
            @{ Name = "InventoryServlet"; Type = "Servlet"; Desc = "Tiếp nhận thông tin nhập kho mới từ chủ shop và hiển thị danh sách nhật ký biến động kho." },
            @{ Name = "InventoryService"; Type = "Service"; Desc = "Thực thi transaction cập nhật số lượng tồn kho của biến thể, tạo nhật ký biến động kho và tự động kích hoạt lại sản phẩm." },
            @{ Name = "InventoryDAO"; Type = "DAO"; Desc = "Ghi nhận log biến động kho và truy vấn lịch sử nhập xuất kho của cửa hàng." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Cập nhật cộng/trừ số lượng tồn kho trực tiếp của biến thể sản phẩm." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Cập nhật trạng thái hiển thị của sản phẩm khi được nhập hàng mới trong DB." },
            @{ Name = "InventoryLog"; Type = "Entity"; Desc = "Đối tượng ánh xạ lịch sử biến động kho trong hệ thống." },
            @{ Name = "inventory.jsp"; Type = "JSP"; Desc = "Giao diện nhập kho và danh sách lịch sử nhập xuất kho của shop." }
        )
    },
    @{
        Id = 12
        Filename = "pricing_strategy.html"
        Title = "Pricing Strategy – Chiến lược giá & Khuyến mãi theo giờ"
        Iteration = 2
        Theme = "yellow"
        Desc = "Cấu hình giá bán gốc và giá khuyến mãi (Discount Price) có hiệu lực trong một khoảng thời gian định sẵn (tự động khôi phục giá gốc khi hết hạn)."
        Flow = @(
            @{ Name = "Chủ shop cấu hình giá"; Desc = "Khi chỉnh sửa biến thể, chủ shop nhập <code>price</code> và tùy chọn <code>discount_price</code> kèm ngày bắt đầu và kết thúc khuyến mãi." },
            @{ Name = "Lưu trữ dữ liệu"; Desc = "<code>ProductVariantDAO</code> lưu các mốc thời gian khuyến mãi trực tiếp vào bảng <code>product_variants</code>." },
            @{ Name = "Áp dụng giá khuyến mãi"; Desc = "Khi khách hàng duyệt sản phẩm, câu truy vấn SQL kiểm tra mốc thời gian hiện tại: nếu nằm trong khoảng hiệu lực, hệ thống tính toán giá bán thực tế bằng <code>discount_price</code>." },
            @{ Name = "Hiển thị giá gốc bị gạch"; Desc = "Giao diện hiển thị giá trị giảm giá, hiển thị giá khuyến mãi nổi bật và gạch ngang giá gốc để thu hút khách mua hàng." }
        )
        Classes = @(
            @{ Name = "ProductEditServlet"; Type = "Servlet"; Desc = "Tiếp nhận cài đặt giá gốc và giá khuyến mãi kèm thời hạn áp dụng từ form của chủ shop." },
            @{ Name = "ProductDetailServlet"; Type = "Servlet"; Desc = "Lấy thông tin giá bán gốc và giá khuyến mãi của biến thể hiện có để hiển thị." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Truy vấn giá gốc và kiểm tra thời hạn khuyến mãi của biến thể sản phẩm để tính toán giá bán thực tế." },
            @{ Name = "ProductVariant"; Type = "Entity"; Desc = "Entity chứa thông tin giá bán gốc, giá khuyến mãi và thời hạn áp dụng." },
            @{ Name = "product-list.jsp"; Type = "JSP"; Desc = "Hiển thị giá gốc bị gạch ngang và thẻ tag giảm giá động trên giao diện lưới." }
        )
    },
    @{
        Id = 13
        Filename = "advanced_search_filters.html"
        Title = "Advanced Search – Bộ lọc tìm kiếm & Sắp xếp sản phẩm"
        Iteration = 2
        Theme = "green"
        Desc = "Bộ lọc sản phẩm nâng cao cho phép tìm kiếm theo khoảng giá, đánh giá sao trung bình, xuất xứ, trạng thái hàng hóa và sắp xếp linh hoạt."
        Flow = @(
            @{ Name = "Người dùng cấu hình bộ lọc"; Desc = "Khách hàng chọn khoảng giá mong muốn, tích chọn xếp hạng đánh giá tối thiểu (Ví dụ: từ 4 sao trở lên) và chọn tiêu chí sắp xếp (Giá tăng/giảm dần, bán chạy nhất)." },
            @{ Name = "Gửi yêu cầu lọc"; Desc = "Trình duyệt gửi yêu cầu GET chứa các tham số lọc tới <code>ProductListServlet</code>." },
            @{ Name = "Xây dựng câu SQL động"; Desc = "Servlet gọi <code>ProductService</code>. Tại tầng DAO, <code>ProductDAO.searchProducts()</code> phân tích các tham số để ghép nối các mệnh đề <code>WHERE</code> động vào câu truy vấn SQL." },
            @{ Name = "Trả kết quả phân trang"; Desc = "Database thực thi câu lệnh SQL động, trả về danh sách sản phẩm cùng siêu dữ liệu phân trang hiển thị lên lưới sản phẩm." }
        )
        Classes = @(
            @{ Name = "ProductListServlet"; Type = "Servlet"; Desc = "Đọc các tham số tìm kiếm nâng cao (giá, xếp hạng sao, xuất xứ) để điều phối phân trang và lọc." },
            @{ Name = "AiSearchServlet"; Type = "Servlet"; Desc = "Hỗ trợ tìm kiếm thông minh thông qua công nghệ tìm kiếm ngữ nghĩa AI." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Xử lý chuẩn hóa và định dạng dữ liệu đầu vào của bộ lọc tìm kiếm." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Xây dựng truy vấn SQL động dựa trên các tham số lọc tìm kiếm và thực thi lấy kết quả phân trang sản phẩm." },
            @{ Name = "product-list.jsp"; Type = "JSP"; Desc = "Giao diện lưới sản phẩm tích hợp bảng bộ lọc tìm kiếm đa dạng ở sidebar." }
        )
    },
    @{
        Id = 14
        Filename = "smart_recommendations.html"
        Title = "Smart Recommendations – Gợi ý sản phẩm thông minh"
        Iteration = 2
        Theme = "cyan"
        Desc = "Tự động gợi ý các sản phẩm cùng danh mục, danh sách sản phẩm bán chạy nhất (Best Sellers) và danh sách sản phẩm khách hàng vừa xem gần đây."
        Flow = @(
            @{ Name = "Theo dõi sản phẩm vừa xem"; Desc = "Khi khách hàng xem chi tiết một quả, <code>ProductDetailServlet</code> lưu ID sản phẩm đó vào một danh sách hàng đợi trong Session của trình duyệt." },
            @{ Name = "Gợi ý cùng danh mục"; Desc = "Tại trang chi tiết, hệ thống chạy truy vấn lấy ra 4 sản phẩm cùng Category ID (loại trừ sản phẩm hiện tại)." },
            @{ Name = "Lấy danh sách bán chạy"; Desc = "Hệ thống truy vấn các sản phẩm có chỉ số <code>sold_quantity</code> cao nhất làm gợi ý Best Seller." },
            @{ Name = "Hiển thị băng chuyền"; Desc = "Các khu vực gợi ý được vẽ dưới dạng băng chuyền trượt (carousel) phía cuối trang chi tiết để khuyến khích khách hàng bấm xem thêm." }
        )
        Classes = @(
            @{ Name = "ProductDetailServlet"; Type = "Servlet"; Desc = "Tải thông tin sản phẩm và cập nhật lịch sử xem (ID sản phẩm vừa xem) trong session." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Cung cấp hàm lấy danh sách sản phẩm gợi ý cùng danh mục và sản phẩm bán chạy nhất." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Thực hiện truy vấn sản phẩm cùng category_id và sản phẩm có lượng bán chạy (sold_quantity) nhiều nhất." },
            @{ Name = "product-detail.jsp"; Type = "JSP"; Desc = "Hiển thị băng chuyền gợi ý sản phẩm liên quan và danh sách sản phẩm vừa xem." }
        )
    },
    @{
        Id = 15
        Filename = "shopping_cart_system.html"
        Title = "Shopping Cart – Hệ thống giỏ hàng & Đồng bộ giỏ hàng"
        Iteration = 2
        Theme = "purple"
        Desc = "Hỗ trợ khách hàng vãng lai lưu giỏ hàng tại LocalStorage và đồng bộ tự động vào cơ sở dữ liệu ngay sau khi khách hàng đăng nhập tài khoản."
        Flow = @(
            @{ Name = "Khách vãng lai chọn mua"; Desc = "Chưa đăng nhập: JavaScript ghi nhận thông tin biến thể sản phẩm và số lượng vào bộ nhớ <code>LocalStorage</code> của trình duyệt." },
            @{ Name = "Khách đăng nhập chọn mua"; Desc = "Đã đăng nhập: Yêu cầu thêm hàng được POST trực tiếp lên <code>CartServlet</code> để lưu trữ lâu dài trong bảng <code>cart_items</code>." },
            @{ Name = "Đồng bộ giỏ hàng (Sync)"; Desc = "Khi người dùng đăng nhập thành công, client gửi gói dữ liệu LocalStorage lên <code>CartSyncServlet</code> qua AJAX." },
            @{ Name = "Xử lý gộp sản phẩm"; Desc = "Servlet chạy truy vấn gộp: Nếu biến thể đã có trong DB, cộng dồn số lượng. Nếu chưa có, thêm mới dòng sản phẩm và dọn sạch LocalStorage ở client." }
        )
        Classes = @(
            @{ Name = "CartServlet"; Type = "Servlet"; Desc = "Xử lý thêm sản phẩm, sửa đổi số lượng và xóa các dòng sản phẩm trong giỏ hàng DB của khách hàng." },
            @{ Name = "CartSyncServlet"; Type = "Servlet"; Desc = "Tiếp nhận dữ liệu giỏ hàng LocalStorage của khách vãng lai gửi lên để gộp vào giỏ hàng DB sau khi đăng nhập." },
            @{ Name = "GuestCartServlet"; Type = "Servlet"; Desc = "API lấy thông tin chi tiết (tên, hình ảnh, đơn giá) của các biến thể trong giỏ hàng LocalStorage." },
            @{ Name = "CartService"; Type = "Service"; Desc = "Xác minh số lượng tồn kho của biến thể sản phẩm trước khi cho phép cập nhật vào giỏ hàng." },
            @{ Name = "CartDAO"; Type = "DAO"; Desc = "Thực hiện chèn mới, gộp sản phẩm và cập nhật giỏ hàng của người dùng trong DB." },
            @{ Name = "CartItem"; Type = "Entity"; Desc = "Entity đại diện cho một mặt hàng chi tiết trong giỏ hàng." },
            @{ Name = "cart.jsp"; Type = "JSP"; Desc = "Trang giỏ hàng hiển thị danh sách mặt hàng, số lượng và tổng tiền tạm tính." }
        )
    },
    @{
        Id = 16
        Filename = "checkout_process.html"
        Title = "Checkout Process – Quy trình thanh toán & Áp mã giảm giá"
        Iteration = 2
        Theme = "purple"
        Desc = "Trang tổng hợp thanh toán cho phép khách hàng lựa chọn địa chỉ giao nhận mặc định, áp dụng mã giảm giá và tính toán biểu phí giao hàng."
        Flow = @(
            @{ Name = "Vào trang thanh toán"; Desc = "Khách hàng nhấn nút Mua Hàng trong giỏ hàng để chuyển tới trang <code>/checkout</code>." },
            @{ Name = "Tải thông tin thanh toán"; Desc = "<code>CheckoutServlet</code> truy vấn danh sách địa chỉ nhận hàng của khách hàng và thông tin chi tiết các mặt hàng được chọn mua." },
            @{ Name = "Áp dụng mã giảm giá"; Desc = "Khách hàng nhập mã giảm giá -> AJAX gửi tới <code>CouponValidateServlet</code> để kiểm tra điều kiện áp dụng (giá trị đơn tối thiểu, hạn dùng)." },
            @{ Name = "Tính toán tổng hóa đơn"; Desc = "Hệ thống tính toán tổng tiền hàng, cộng phí vận chuyển, cộng phụ thu đóng gói, trừ đi số tiền giảm giá và hiển thị số tiền thanh toán cuối cùng." }
        )
        Classes = @(
            @{ Name = "CheckoutServlet"; Type = "Servlet"; Desc = "Chuẩn bị thông tin địa chỉ giao nhận của khách hàng và danh sách các sản phẩm được chọn mua để thanh toán." },
            @{ Name = "CouponValidateServlet"; Type = "Servlet"; Desc = "API tiếp nhận mã giảm giá, kiểm tra điều kiện (hạn dùng, mức chi tiêu tối thiểu) và trả về giá trị khấu trừ tương ứng." },
            @{ Name = "CheckoutService"; Type = "Service"; Desc = "Tổng hợp thông tin hóa đơn tạm tính và tính toán phí ship cùng phí nền tảng." },
            @{ Name = "PromotionService"; Type = "Service"; Desc = "Xác minh các điều kiện và tính toán mức chiết khấu của mã giảm giá." },
            @{ Name = "UserAddressDAO"; Type = "DAO"; Desc = "Lấy danh sách địa chỉ giao nhận hàng hóa của khách hàng trong DB." },
            @{ Name = "PromotionDAO"; Type = "DAO"; Desc = "Truy vấn thông tin cấu hình của mã giảm giá từ DB." },
            @{ Name = "checkout.jsp"; Type = "JSP"; Desc = "Giao diện trang thanh toán cho phép chọn địa chỉ và áp dụng mã giảm giá." }
        )
    },
    @{
        Id = 17
        Filename = "order_placement.html"
        Title = "Order Placement – Đặt hàng & Giữ chỗ kho (Inventory Reservation)"
        Iteration = 2
        Theme = "purple"
        Desc = "Quy trình đặt đơn hàng chính thức chạy dưới một DB Transaction nhằm đảm bảo trừ tồn kho và ghi nhận log biến động kho một cách nhất quán."
        Flow = @(
            @{ Name = "Xác nhận đặt đơn"; Desc = "Khách hàng chọn phương thức thanh toán (COD hoặc Chuyển khoản) và nhấn 'Đặt hàng'." },
            @{ Name = "Tiếp nhận đơn hàng"; Desc = "Yêu cầu POST tới <code>CheckoutServlet</code>. Servlet kiểm tra lại giỏ hàng và gọi <code>CheckoutService.placeOrder()</code>." },
            @{ Name = "Khởi chạy Transaction"; Desc = "Hệ thống mở kết nối cơ sở dữ liệu và thiết lập <code>setAutoCommit(false)</code> để bắt đầu quy trình giao dịch an toàn." },
            @{ Name = "Tạo đơn hàng"; Desc = "Thêm bản ghi vào bảng <code>orders</code>. Nếu đơn hàng gồm sản phẩm của nhiều shop, hệ thống tự tách thành các đơn hàng con tương ứng." },
            @{ Name = "Giữ chỗ kho"; Desc = "Với mỗi sản phẩm, thực hiện trừ số lượng trong <code>product_variants</code> và ghi một log kiểu <code>ORDER_RESERVE</code> vào <code>inventory_logs</code>." },
            @{ Name = "Áp dụng khuyến mãi"; Desc = "Nếu có sử dụng mã giảm giá, hệ thống ghi nhận vào bảng <code>order_promotions</code> và tăng số lượng lượt sử dụng mã trong <code>promotions</code>." },
            @{ Name = "Commit & Dọn dẹp"; Desc = "Nếu mọi bước thành công, hệ thống commit transaction, dọn sạch sản phẩm đã mua khỏi giỏ hàng DB và chuyển hướng khách hàng sang trang thành công." }
        )
        Classes = @(
            @{ Name = "CheckoutServlet"; Type = "Servlet"; Desc = "Tiếp nhận yêu cầu đặt hàng từ POST request, chuyển hướng người dùng sang trang thành công hoặc báo lỗi." },
            @{ Name = "CheckoutService"; Type = "Service"; Desc = "Thực hiện transaction đặt đơn: Tạo các đơn con phân tách theo shop, trừ tồn kho biến thể, ghi log ORDER_RESERVE và cập nhật khuyến mãi." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Thực hiện thêm đơn hàng mới (orders) với trạng thái mặc định PENDING_PAYMENT." },
            @{ Name = "OrderItemDAO"; Type = "DAO"; Desc = "Lưu chi tiết các sản phẩm đặt mua vào bảng order_items (đóng băng giá tại thời điểm mua)." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Khấu trừ trực tiếp số lượng hàng tồn kho của biến thể sản phẩm." },
            @{ Name = "InventoryDAO"; Type = "DAO"; Desc = "Ghi nhận nhật ký thay đổi kho của biến thể với trạng thái ORDER_RESERVE." },
            @{ Name = "PromotionDAO"; Type = "DAO"; Desc = "Cập nhật tăng lượt sử dụng của mã giảm giá đã dùng." },
            @{ Name = "order-success.jsp"; Type = "JSP"; Desc = "Trang thông báo đặt hàng thành công tích hợp nút dẫn sang thanh toán." }
        )
    },
    @{
        Id = 18
        Filename = "customer_order_tracking.html"
        Title = "Order Tracking – Lịch sử & Theo dõi tiến trình đơn hàng"
        Iteration = 2
        Theme = "blue"
        Desc = "Giao diện dành cho khách hàng để theo dõi chi tiết trạng thái đơn hàng theo thời gian thực (Timeline), xem thông tin shipper và thực hiện nhận hàng."
        Flow = @(
            @{ Name = "Xem lịch sử đơn"; Desc = "Khách hàng mở trang <code>/customer/orders</code> để xem toàn bộ danh sách đơn hàng đã đặt." },
            @{ Name = "Xem chi tiết tiến trình"; Desc = "Bấm vào một đơn hàng -> <code>ProfileOrderDetailServlet</code> tải chi tiết đơn kèm tiến trình giao hàng hiện tại." },
            @{ Name = "Hiển thị Timeline"; Desc = "Trang giao diện vẽ thanh Timeline các mốc thời gian: Đã đặt -> Shop chuẩn bị -> Đang giao -> Đã giao thành công." },
            @{ Name = "Xác nhận nhận hàng"; Desc = "Khi nhận được hàng, khách bấm 'Đã nhận hàng'. Hệ thống cập nhật trạng thái đơn thành <code>DELIVERED</code> và ghi nhận trạng thái nhận hàng <code>RECEIVED</code>." },
            @{ Name = "Mua lại đơn hàng (Reorder)"; Desc = "Khách hàng có thể bấm nút 'Mua lại'. Hệ thống tự động kiểm tra xem các biến thể sản phẩm cũ còn bán hay không, rồi thêm chúng vào giỏ hàng." }
        )
        Classes = @(
            @{ Name = "OrderServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách đơn hàng phân trang theo trạng thái đơn cho khách và tiếp nhận lệnh xác nhận nhận hàng." },
            @{ Name = "ProfileOrderDetailServlet"; Type = "Servlet"; Desc = "Tải thông tin hành trình chi tiết và thông tin giao hàng của đơn." },
            @{ Name = "OrderService"; Type = "Service"; Desc = "Cập nhật trạng thái nhận hàng của khách thành hoàn thành đơn." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Truy vấn danh sách đơn hàng lịch sử của khách hàng và cập nhật trạng thái đơn." },
            @{ Name = "DeliveryDAO"; Type = "DAO"; Desc = "Lấy thông tin người giao hàng đang phụ trách đơn hàng." },
            @{ Name = "orders.jsp"; Type = "JSP"; Desc = "Trang lịch sử đơn hàng phân chia theo các tab trạng thái tiện lợi." },
            @{ Name = "order-detail.jsp"; Type = "JSP"; Desc = "Giao diện theo dõi hành trình đơn hàng chi tiết với timeline trực quan." }
        )
    },
    @{
        Id = 19
        Filename = "delivery_staff_flow.html"
        Title = "Delivery Flow – Quy trình xử lý của Nhân viên giao hàng"
        Iteration = 2
        Theme = "orange"
        Desc = "Hỗ trợ nhân viên giao nhận xem danh sách chuyến đi được chỉ định, cập nhật trạng thái giao hàng và đăng tải ảnh bằng chứng giao hàng thành công."
        Flow = @(
            @{ Name = "Nhận chuyến đi"; Desc = "Shipper đăng nhập hệ thống, truy cập <code>/delivery/dashboard</code> để xem danh sách chuyến đi (trips) được gán quyền phụ trách." },
            @{ Name = "Cập nhật lấy hàng"; Desc = "Khi tới shop nhận hàng, shipper cập nhật trạng thái chuyến đi thành <code>PICKED_UP</code>, sau đó đổi thành <code>IN_TRANSIT</code> khi bắt đầu di chuyển." },
            @{ Name = "Giao hàng & Chụp ảnh"; Desc = "Tới nơi giao hàng, shipper yêu cầu khách xác nhận và chụp lại ảnh gói hàng làm minh chứng." },
            @{ Name = "Hoàn thành chuyến đi"; Desc = "AJAX gửi tới <code>DeliveryUpdateAPI</code> kèm file ảnh. Hệ thống dùng <code>FileUploadUtil</code> lưu ảnh, cập nhật trạng thái giao hàng thành <code>DELIVERED</code> hoặc <code>FAILED</code> (nếu giao thất bại)." }
        )
        Classes = @(
            @{ Name = "DeliveryDashboardServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách chuyến đi và điểm dừng cần xử lý của shipper hiện tại." },
            @{ Name = "DeliveryDetailServlet"; Type = "Servlet"; Desc = "Hiển thị thông tin liên hệ khách hàng và địa chỉ giao hàng của điểm dừng cụ thể." },
            @{ Name = "DeliveryUpdateAPI"; Type = "Servlet"; Desc = "API tiếp nhận cập nhật trạng thái giao hàng kèm ảnh bằng chứng giao hàng thành công." },
            @{ Name = "DeliveryService"; Type = "Service"; Desc = "Cập nhật trạng thái giao hàng của đơn và chuyến đi tương ứng." },
            @{ Name = "DeliveryDAO"; Type = "DAO"; Desc = "Cập nhật mốc thời gian giao nhận và lưu đường dẫn ảnh bằng chứng giao hàng." },
            @{ Name = "DeliveryTripDAO"; Type = "DAO"; Desc = "Cập nhật trạng thái hành trình của toàn bộ chuyến đi của shipper." },
            @{ Name = "FileUploadUtil"; Type = "Utility"; Desc = "Tiện ích kiểm tra định dạng và lưu file ảnh bằng chứng giao hàng lên máy chủ." },
            @{ Name = "dashboard.jsp"; Type = "JSP"; Desc = "Giao diện làm việc chính của shipper hiển thị danh sách nhiệm vụ được giao." }
        )
    },
    @{
        Id = 20
        Filename = "shop_admin_order_management.html"
        Title = "Shop/Admin Order – Quản lý đơn hàng dành cho Shop & Admin"
        Iteration = 2
        Theme = "purple"
        Desc = "Chức năng duyệt đơn hàng dành cho Chủ shop và công cụ giám sát dòng chảy đơn hàng toàn hệ thống dành cho Quản trị viên."
        Flow = @(
            @{ Name = "Nhận đơn hàng mới"; Desc = "Chủ shop xem danh sách đơn hàng đang chờ duyệt tại <code>/shop/orders</code>." },
            @{ Name = "Phê duyệt & Chuẩn bị"; Desc = "Chủ shop bấm Chấp nhận đơn -> Trạng thái đơn đổi sang <code>CONFIRMED</code>. Shop chuẩn bị hàng và đóng gói -> Đổi sang <code>PREPARING</code>." },
            @{ Name = "Giao cho Shipper"; Desc = "Shop bấm Giao hàng -> Trạng thái đổi thành <code>DISPATCHED</code>. Hệ thống tự động tạo bản ghi trong <code>deliveries</code> gửi tới shipper." },
            @{ Name = "Từ chối đơn hàng"; Desc = "Nếu thiếu hàng, shop bấm Từ chối -> Chạy transaction cập nhật đơn thành <code>CANCELLED</code>, đồng thời hoàn kho số lượng hàng đã đặt và ghi log <code>ORDER_RELEASE</code>." },
            @{ Name = "Giám sát của Admin"; Desc = "Admin truy cập <code>/admin/order-monitor</code> để theo dõi thời gian thực mọi biến động đơn hàng trên toàn hệ thống." }
        )
        Classes = @(
            @{ Name = "ShopOrderServlet"; Type = "Servlet"; Desc = "Cung cấp giao diện quản lý đơn hàng bán của chủ shop, tích hợp duyệt đơn và giao hàng." },
            @{ Name = "AdminOrderServlet"; Type = "Servlet"; Desc = "Cho phép Admin tra cứu và xem danh sách đơn hàng toàn sàn giao dịch." },
            @{ Name = "AdminOrderMonitorServlet"; Type = "Servlet"; Desc = "Bảng giám sát luồng đơn hàng trực quan thời gian thực cho Admin." },
            @{ Name = "OrderService"; Type = "Service"; Desc = "Cung cấp nghiệp vụ duyệt đơn, chuẩn bị hàng và xử lý hoàn trả kho khi hủy đơn." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Truy vấn danh sách đơn theo bộ lọc và cập nhật trạng thái đơn hàng trong DB." },
            @{ Name = "InventoryDAO"; Type = "DAO"; Desc = "Thực hiện hoàn trả số lượng tồn kho khi đơn hàng bị shop từ chối hoặc hủy đơn." },
            @{ Name = "orders.jsp"; Type = "JSP"; Desc = "Giao diện quản lý đơn hàng bán của chủ cửa hàng." }
        )
    },
    @{
        Id = 21
        Filename = "content_moderation_categories.html"
        Title = "Content Moderation – Kiểm duyệt sản phẩm & Quản lý danh mục"
        Iteration = 2
        Theme = "red"
        Desc = "Quy trình kiểm duyệt các sản phẩm mới do chủ cửa hàng đăng tải và quản lý danh sách danh mục phân loại trái cây của Admin."
        Flow = @(
            @{ Name = "Sản phẩm chờ kiểm duyệt"; Desc = "Khi shop tạo sản phẩm mới, trạng thái phê duyệt mặc định là <code>PENDING</code>. Sản phẩm chưa hiển thị trên trang chủ." },
            @{ Name = "Admin đánh giá sản phẩm"; Desc = "Admin truy cập <code>/admin/products</code> để kiểm tra danh sách sản phẩm chờ duyệt." },
            @{ Name = "Duyệt hoặc Từ chối"; Desc = "Admin bấm Duyệt (cập nhật <code>APPROVED</code>) giúp sản phẩm chính thức mở bán, hoặc bấm Từ chối (nhập lý do vào <code>rejection_reason</code>)." },
            @{ Name = "Quản lý danh mục"; Desc = "Admin truy cập <code>/admin/categories</code> để thực hiện CRUD các danh mục sản phẩm (thay đổi thứ tự hiển thị, ẩn danh mục vi phạm)." }
        )
        Classes = @(
            @{ Name = "AdminProductServlet"; Type = "Servlet"; Desc = "Trang hiển thị danh sách sản phẩm chờ kiểm duyệt và tiếp nhận quyết định phê duyệt." },
            @{ Name = "CategoryServlet"; Type = "Servlet"; Desc = "CRUD các danh mục sản phẩm, sắp xếp display_order của Admin." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Thay đổi trạng thái phê duyệt của sản phẩm (PENDING/APPROVED/REJECTED)." },
            @{ Name = "CategoryService"; Type = "Service"; Desc = "Quản lý thêm/sửa/xóa danh mục trái cây của Admin." },
            @{ Name = "ProductDAO"; Type = "DAO"; Desc = "Cập nhật cờ phê duyệt sản phẩm và lưu lý do từ chối phê duyệt." },
            @{ Name = "CategoryDAO"; Type = "DAO"; Desc = "Thực thi các câu lệnh SQL CRUD danh mục trái cây trong DB." },
            @{ Name = "admin-products.jsp"; Type = "JSP"; Desc = "Giao diện duyệt sản phẩm mới của Admin." }
        )
    },
    @{
        Id = 22
        Filename = "online_payment_integration.html"
        Title = "Online Payment – Tích hợp cổng thanh toán trực tuyến"
        Iteration = 3
        Theme = "yellow"
        Desc = "Quy trình thanh toán qua chuyển khoản ngân hàng bằng mã VietQR động, nhận kết quả tự động từ SePay Webhook và cập nhật trạng thái đơn hàng."
        Flow = @(
            @{ Name = "Khởi tạo thanh toán"; Desc = "Khi chọn chuyển khoản, hệ thống tạo bản ghi trong <code>payment_transactions</code> với trạng thái <code>pending</code>." },
            @{ Name = "Hiển thị VietQR"; Desc = "Trang <code>order-payment.jsp</code> hiển thị mã QR động chứa số tài khoản nhận, số tiền và mã nội dung chuyển khoản (Ví dụ: <code>DH101</code>)." },
            @{ Name = "Nhận thông tin chuyển khoản"; Desc = "Khách hàng quét mã chuyển khoản. Ngân hàng báo có -> Hệ thống SePay gửi yêu cầu Webhook tới <code>PaymentWebhookServlet</code>." },
            @{ Name = "Kiểm tra chống trùng lặp"; Desc = "Webhook gọi <code>PaymentService</code> kiểm tra mã giao dịch ngân hàng trong <code>sepay_webhook_dedup</code> để tránh xử lý trùng lặp." },
            @{ Name = "Cập nhật trạng thái"; Desc = "Nếu hợp lệ, cập nhật trạng thái giao dịch thanh toán thành <code>completed</code> và đổi trạng thái đơn hàng thành <code>CONFIRMED</code>." },
            @{ Name = "Client tự động chuyển hướng"; Desc = "Mã JavaScript tại trang thanh toán liên tục gửi yêu cầu thăm dò (polling) trạng thái thanh toán. Khi phát hiện giao dịch thành công, tự động chuyển khách hàng sang trang thành công." }
        )
        Classes = @(
            @{ Name = "PaymentWebhookServlet"; Type = "Servlet"; Desc = "Endpoint tiếp nhận thông tin chuyển khoản tự động do SePay Webhook đẩy về khi tài khoản báo có." },
            @{ Name = "UserProfileServlet"; Type = "Servlet"; Desc = "Cung cấp API để client thăm dò (polling) trạng thái thanh toán đơn hàng." },
            @{ Name = "PaymentService"; Type = "Service"; Desc = "Xác thực chữ ký bảo mật webhook, kiểm tra chống trùng lặp giao dịch và cập nhật trạng thái hóa đơn." },
            @{ Name = "PaymentDAO"; Type = "DAO"; Desc = "Ghi nhận thông tin giao dịch thanh toán trực tuyến và cập nhật kết quả xử lý webhook." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Cập nhật trạng thái thanh toán và duyệt đơn hàng thành công." },
            @{ Name = "PaymentTransaction"; Type = "Entity"; Desc = "Entity ánh xạ bảng payment_transactions chứa thông tin giao dịch thanh toán." },
            @{ Name = "order-payment.jsp"; Type = "JSP"; Desc = "Trang hiển thị VietQR động kèm đồng hồ đếm ngược thời gian thanh toán." }
        )
    },
    @{
        Id = 23
        Filename = "discount_promotion_engine.html"
        Title = "Promotion Engine – Công cụ quản lý & Áp dụng mã giảm giá"
        Iteration = 3
        Theme = "yellow"
        Desc = "Hệ thống khuyến mãi cho phép thiết lập mã giảm giá theo phần trăm hoặc số tiền cố định, giới hạn lượt dùng và phạm vi áp dụng (theo shop hoặc toàn sàn)."
        Flow = @(
            @{ Name = "Thiết lập mã"; Desc = "Chủ shop hoặc Admin tạo mã tại <code>/shop/promotion</code>, cài đặt giá trị giảm, hạn mức đơn tối thiểu và số lượng tối đa." },
            @{ Name = "Nhập mã thanh toán"; Desc = "Khách hàng nhập mã giảm giá tại trang checkout." },
            @{ Name = "Xác thực điều kiện"; Desc = "<code>CouponValidateServlet</code> gọi <code>PromotionService.validate()</code> để kiểm tra: mã còn hạn sử dụng, tổng tiền đơn hàng đạt mức tối thiểu, và người dùng chưa dùng quá giới hạn." },
            @{ Name = "Ghi nhận sử dụng"; Desc = "Khi đơn hàng được đặt thành công, hệ thống tăng cờ <code>used_count</code> của mã khyến mãi và ghi nhận lịch sử vào bảng <code>order_promotions</code>." }
        )
        Classes = @(
            @{ Name = "PromotionServlet"; Type = "Servlet"; Desc = "Cung cấp form thiết lập mã giảm giá và quản lý danh sách mã giảm giá của shop." },
            @{ Name = "CouponValidateServlet"; Type = "Servlet"; Desc = "API kiểm tra tính hợp lệ của mã giảm giá khi khách nhập tại trang thanh toán." },
            @{ Name = "PromotionService"; Type = "Service"; Desc = "Xác minh các điều kiện ràng buộc của mã giảm giá và tính toán số tiền được khấu trừ." },
            @{ Name = "PromotionDAO"; Type = "DAO"; Desc = "Thực hiện CRUD mã giảm giá và cập nhật lượt sử dụng mã trong DB." },
            @{ Name = "Promotion"; Type = "Entity"; Desc = "Đối tượng ánh xạ bảng promotions chứa thông tin mã giảm giá." },
            @{ Name = "promotion.jsp"; Type = "JSP"; Desc = "Giao diện quản lý mã giảm giá của chủ cửa hàng." }
        )
    },
    @{
        Id = 24
        Filename = "invoice_billing.html"
        Title = "Invoice & Billing – Hóa đơn bán hàng & Biên lai thanh toán"
        Iteration = 3
        Theme = "blue"
        Desc = "Hệ thống tự động kết xuất hóa đơn chi tiết (Invoice) hiển thị cơ cấu giá, chi phí đóng gói, phí vận chuyển và mức khấu trừ khuyến mãi."
        Flow = @(
            @{ Name = "Yêu cầu xuất hóa đơn"; Desc = "Khách hàng hoặc chủ shop nhấn vào liên kết 'Xem hóa đơn' từ trang quản lý chi tiết đơn hàng." },
            @{ Name = "Tổng hợp dữ liệu hóa đơn"; Desc = "<code>ProfileOrderDetailServlet</code> gọi <code>OrderViewService</code> tải toàn bộ thông tin đơn hàng và ảnh chụp các dòng sản phẩm." },
            @{ Name = "Vẽ giao diện hóa đơn"; Desc = "Dữ liệu được đẩy sang trang <code>invoice.jsp</code> được thiết kế theo phong cách tối giản, hiển thị đầy đủ thông tin của bên mua, bên bán." },
            @{ Name = "Tải xuống hoặc In ấn"; Desc = "Trang hỗ trợ nút In nhanh sử dụng CSS Media Print để in ra giấy hoặc lưu thành file PDF trực tiếp từ trình duyệt." }
        )
        Classes = @(
            @{ Name = "ProfileOrderDetailServlet"; Type = "Servlet"; Desc = "Thu thập thông tin đơn hàng, chi phí và chuyển hướng sang trang giao diện hóa đơn." },
            @{ Name = "OrderViewService"; Type = "Service"; Desc = "Cung cấp hàm tổng hợp thông tin chi tiết hóa đơn thanh toán phục vụ in ấn." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Đọc thông tin tổng tiền và mã hóa đơn của đơn hàng từ DB." },
            @{ Name = "OrderItemDAO"; Type = "DAO"; Desc = "Đọc thông tin các mặt hàng và phụ thu đóng gói đi kèm đơn hàng." },
            @{ Name = "invoice.jsp"; Type = "JSP"; Desc = "Giao diện hiển thị hóa đơn bán hàng tối giản tối ưu cho việc in ấn hoặc lưu PDF." }
        )
    },
    @{
        Id = 25
        Filename = "order_returns_refund.html"
        Title = "Returns & Refund – Đổi trả hàng & Hoàn tiền đơn hàng"
        Iteration = 3
        Theme = "red"
        Desc = "Quy trình khách hàng gửi yêu cầu đổi trả kèm hình ảnh minh chứng, chủ shop phê duyệt và hệ thống thực thi hoàn trả tồn kho tự động."
        Flow = @(
            @{ Name = "Khách gửi yêu cầu"; Desc = "Tại trang chi tiết đơn đã giao, khách bấm Đổi trả hàng, nhập lý do và tải ảnh bằng chứng lên <code>ReturnRequestServlet</code>." },
            @{ Name = "Chủ shop xem xét"; Desc = "Chủ cửa hàng nhận thông báo, truy cập <code>/shop/return-requests</code> xem mô tả và hình ảnh bằng chứng khách gửi." },
            @{ Name = "Thực thi phê duyệt"; Desc = "Chủ shop bấm Chấp nhận -> Hệ thống chạy transaction cập nhật trạng thái đơn hàng thành <code>REFUNDED</code>." },
            @{ Name = "Hoàn kho tự động"; Desc = "Gọi <code>InventoryService</code> thực hiện cộng trả lại số lượng sản phẩm vào kho, ghi nhật ký biến động kho với kiểu <code>RETURN</code>." },
            @{ Name = "Xử lý hoàn tiền"; Desc = "Admin nhận thông tin, tiến hành đối soát và chuyển khoản hoàn lại số tiền tương ứng cho khách hàng." }
        )
        Classes = @(
            @{ Name = "ReturnRequestServlet"; Type = "Servlet"; Desc = "Tiếp nhận đơn yêu cầu đổi trả kèm ảnh bằng chứng của khách và hiển thị danh sách xét duyệt cho chủ shop." },
            @{ Name = "AdminRefundServlet"; Type = "Servlet"; Desc = "Giao diện quản lý và xác nhận chuyển tiền hoàn của Admin." },
            @{ Name = "ReturnService"; Type = "Service"; Desc = "Xử lý nghiệp vụ phê duyệt đơn đổi trả hàng và tự động kích hoạt hoàn kho." },
            @{ Name = "InventoryService"; Type = "Service"; Desc = "Cộng trả lại số lượng tồn kho của biến thể sản phẩm khi khách trả hàng." },
            @{ Name = "ReturnRequestDAO"; Type = "DAO"; Desc = "Truy vấn và cập nhật trạng thái yêu cầu đổi trả hàng trong DB." },
            @{ Name = "return-request.jsp"; Type = "JSP"; Desc = "Giao diện gửi đơn yêu cầu đổi trả hàng kèm tải ảnh bằng chứng của khách." }
        )
    },
    @{
        Id = 26
        Filename = "customer_order_history.html"
        Title = "Order History – Quản lý lịch sử mua hàng của khách hàng"
        Iteration = 3
        Theme = "blue"
        Desc = "Giúp khách hàng quản lý và tra cứu toàn bộ lịch sử các đơn hàng đã mua, tìm kiếm theo trạng thái đơn hàng và tải biên lai nhanh chóng."
        Flow = @(
            @{ Name = "Yêu cầu lịch sử"; Desc = "Khách hàng truy cập trang cá nhân và chọn mục Lịch sử mua hàng." },
            @{ Name = "Truy vấn dữ liệu"; Desc = "<code>OrderServlet</code> lấy mã định danh khách hàng từ Session, truy vấn bảng <code>orders</code> lấy các đơn hàng mới nhất." },
            @{ Name = "Phân trang kết quả"; Desc = "DAO trả về danh sách đơn hàng đã được phân trang và sắp xếp theo ngày mua mới nhất lên đầu." },
            @{ Name = "Thao tác trên đơn cũ"; Desc = "Giao diện hiển thị chi tiết mã đơn, ngày mua, tổng tiền và cung cấp các nút thao tác nhanh: Đánh giá sản phẩm, Mua lại đơn hàng, Xem hóa đơn." }
        )
        Classes = @(
            @{ Name = "OrderServlet"; Type = "Servlet"; Desc = "Phân trang danh sách đơn hàng đã mua và lọc theo trạng thái đơn cho khách." },
            @{ Name = "ProfileOrderDetailServlet"; Type = "Servlet"; Desc = "Tải thông tin hành trình chi tiết của một đơn hàng cũ." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Truy vấn dữ liệu lịch sử đơn hàng của khách hàng theo phân trang." },
            @{ Name = "orders.jsp"; Type = "JSP"; Desc = "Trang hiển thị danh sách đơn hàng lịch sử." }
        )
    },
    @{
        Id = 27
        Filename = "marketing_campaigns.html"
        Title = "Marketing – Chiến dịch tiếp thị & Banner quảng cáo"
        Iteration = 3
        Theme = "yellow"
        Desc = "Tạo các chương trình khuyến mãi theo mùa (Seasonal Campaigns), Flash Sales giới hạn thời gian hiển thị huy hiệu giảm giá nổi bật."
        Flow = @(
            @{ Name = "Tạo chiến dịch"; Desc = "Chủ shop thiết lập chiến dịch giảm giá trực tiếp cho một danh sách sản phẩm." },
            @{ Name = "Áp dụng giá Flash Sale"; Desc = "Khi khách hàng xem danh sách sản phẩm, hệ thống tự động kiểm tra xem sản phẩm có nằm trong chiến dịch đang chạy hay không." },
            @{ Name = "Hiển thị Banner"; Desc = "Trang chủ hiển thị các banner chương trình thu hút khách hàng click xem mua sắm giá hời." }
        )
        Classes = @(
            @{ Name = "PromotionServlet"; Type = "Servlet"; Desc = "Cấu hình chiến dịch tiếp thị, thiết lập thời hạn áp dụng và mã giảm giá của shop." },
            @{ Name = "ProductService"; Type = "Service"; Desc = "Tải sản phẩm đang áp dụng chiến dịch khuyến mãi của hệ thống." },
            @{ Name = "ProductVariantDAO"; Type = "DAO"; Desc = "Cung cấp thông tin giá bán khuyến mãi của biến thể sản phẩm trong chiến dịch." },
            @{ Name = "promotions"; Type = "DB Table"; Desc = "Bảng DB lưu trữ thông tin chiến dịch marketing, liên kết sản phẩm được áp dụng." }
        )
    },
    @{
        Id = 28
        Filename = "product_review_rating.html"
        Title = "Product Review – Đánh giá & Phản hồi sản phẩm"
        Iteration = 3
        Theme = "orange"
        Desc = "Cho phép khách hàng đánh giá xếp hạng sao (1-5★) kèm nhận xét và hình ảnh sau khi nhận hàng, đồng thời tự động cập nhật số sao trung bình của sản phẩm."
        Flow = @(
            @{ Name = "Mở form đánh giá"; Desc = "Sau khi đơn hàng chuyển sang <code>DELIVERED</code>, khách hàng nhấn chọn Đánh giá sản phẩm." },
            @{ Name = "Gửi bài viết đánh giá"; Desc = "Khách chọn số sao (1-5★), nhập nội dung và tải ảnh thực tế lên <code>ReviewServlet</code>." },
            @{ Name = "Lưu đánh giá"; Desc = "Servlet gọi <code>ReviewService.saveReview()</code> để INSERT thông tin vào bảng <code>reviews</code>." },
            @{ Name = "Tái tính toán số sao"; Desc = "Hệ thống tính toán lại xếp hạng sao trung bình của sản phẩm trong bảng <code>products</code> dựa trên công thức trung bình cộng tất cả đánh giá." },
            @{ Name = "Kiểm duyệt nội dung"; Desc = "Admin truy cập <code>/admin/reviews</code> để xem danh sách phản hồi và có thể ẩn (<code>is_hidden = 1</code>) các bình luận vi phạm chính sách." }
        )
        Classes = @(
            @{ Name = "ReviewServlet"; Type = "Servlet"; Desc = "Tiếp nhận bài đánh giá sao (1-5★) kèm nhận xét, ảnh thực tế từ khách sau khi nhận hàng thành công." },
            @{ Name = "AdminReviewServlet"; Type = "Servlet"; Desc = "Trang quản trị và ẩn hiển thị đánh giá vi phạm chính sách của Admin." },
            @{ Name = "AdminReviewAPI"; Type = "Servlet"; Desc = "API cập nhật cờ ẩn/hiện đánh giá của khách hàng trong DB." },
            @{ Name = "ReviewService"; Type = "Service"; Desc = "Lưu đánh giá mới và kích hoạt tự động tính toán lại điểm số sao trung bình của sản phẩm." },
            @{ Name = "ReviewDAO"; Type = "DAO"; Desc = "Lưu đánh giá vào bảng reviews và tính toán điểm trung bình sao của sản phẩm." },
            @{ Name = "StarRatingTag"; Type = "Tag"; Desc = "Thẻ tag tự động vẽ số lượng sao vàng tương ứng với điểm đánh giá của sản phẩm." },
            @{ Name = "review-submit.jsp"; Type = "JSP"; Desc = "Giao diện viết đánh giá tích hợp tải lên ảnh trải nghiệm." }
        )
    },
    @{
        Id = 29
        Filename = "notification_alert_center.html"
        Title = "Notification – Trung tâm thông báo & Cảnh báo Email"
        Iteration = 3
        Theme = "blue"
        Desc = "Hệ thống thông báo đẩy trong ứng dụng (In-app) và gửi Email tự động khi có biến động về đơn hàng, thanh toán, tồn kho hoặc khuyến mãi."
        Flow = @(
            @{ Name = "Trực quan hóa sự kiện"; Desc = "Khi có sự kiện quan trọng xảy ra (Ví dụ: khách đặt hàng thành công, shop gửi hàng, hoặc kho hàng bị chạm ngưỡng tối thiểu)." },
            @{ Name = "Tạo thông báo"; Desc = "gọi <code>NotificationService.createNotification()</code> để ghi một bản ghi mới vào bảng <code>notifications</code>." },
            @{ Name = "Đồng bộ gửi Email"; Desc = "Nếu sự kiện quan trọng (như đơn hàng cần thanh toán hoặc tài khoản bị khóa), gọi tiếp <code>EmailService</code> gửi mail trực tiếp tới người nhận." },
            @{ Name = "AJAX Thăm dò (Polling)"; Desc = "Trình duyệt khách hàng liên tục gửi yêu cầu AJAX đến <code>NotificationAPIServlet</code> để đếm số lượng thông báo chưa đọc, hiển thị huy hiệu đỏ ở thanh navbar." }
        )
        Classes = @(
            @{ Name = "NotificationServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách thông báo in-app cá nhân đầy đủ phân trang cho người dùng." },
            @{ Name = "NotificationAPIServlet"; Type = "Servlet"; Desc = "API hỗ trợ lấy nhanh số lượng thông báo chưa đọc của người dùng để vẽ badge đỏ ở navbar." },
            @{ Name = "NotificationService"; Type = "Service"; Desc = "Tạo thông báo in-app mới trong DB và kích hoạt gửi email alert tương ứng." },
            @{ Name = "EmailService"; Type = "Service"; Desc = "Thực hiện soạn thảo và gửi thư thông báo tới email người dùng." },
            @{ Name = "NotificationDAO"; Type = "DAO"; Desc = "Lưu trữ thông báo và cập nhật trạng thái đã đọc (is_read = 1) trong DB." },
            @{ Name = "Notification"; Type = "Entity"; Desc = "Đối tượng Java đại diện cho một thông báo hệ thống." },
            @{ Name = "notification.jsp"; Type = "JSP"; Desc = "Trang hiển thị hộp thư thông báo in-app của người dùng." }
        )
    },
    @{
        Id = 30
        Filename = "global_admin_monitoring.html"
        Title = "Admin Monitoring – Giám sát toàn hệ thống & Cấu hình"
        Iteration = 3
        Theme = "red"
        Desc = "Trung tâm giám sát dòng tiền, tổng quan đơn hàng và cấu hình các thông số hệ thống của Quản trị viên tối cao."
        Flow = @(
            @{ Name = "Truy cập Dashboard"; Desc = "Admin đăng nhập, truy cập trang tổng quan <code>/admin/dashboard</code>." },
            @{ Name = "Giám sát giao dịch"; Desc = "Admin theo dõi danh sách các giao dịch thanh toán ngân hàng thời gian thực để đối soát." },
            @{ Name = "Thay đổi cấu hình"; Desc = "Admin mở trang <code>/admin/config</code>, điều chỉnh các thông số vận hành (Ví dụ: Tỷ lệ phí nền tảng, ngưỡng cảnh báo kho toàn sàn)." },
            @{ Name = "Lưu và áp dụng"; Desc = "POST yêu cầu lên <code>AdminConfigServlet</code> để cập nhật bảng <code>system_config</code>. Các cài đặt mới có hiệu lực ngay lập tức mà không cần khởi động lại máy chủ." }
        )
        Classes = @(
            @{ Name = "AdminDashboardServlet"; Type = "Servlet"; Desc = "Trang chủ quản trị tổng hợp thông số tài chính, số lượng shop và đơn hàng." },
            @{ Name = "AdminConfigServlet"; Type = "Servlet"; Desc = "Tiếp nhận và thay đổi các cấu hình hệ thống động từ Admin (như platform fee %)." },
            @{ Name = "AdminPaymentServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách lịch sử giao dịch thanh toán ngân hàng phục vụ đối soát." },
            @{ Name = "SystemConfigService"; Type = "Service"; Desc = "Quản lý việc đọc/ghi các cấu hình động an toàn." },
            @{ Name = "SystemConfigDAO"; Type = "DAO"; Desc = "Truy vấn và cập nhật giá trị cấu hình trên bảng system_config." },
            @{ Name = "admin-config.jsp"; Type = "JSP"; Desc = "Giao diện quản lý các tham số cấu hình hệ thống." }
        )
    },
    @{
        Id = 31
        Filename = "analytics_reporting.html"
        Title = "Analytics & Reporting – Báo cáo doanh thu & Thống kê"
        Iteration = 3
        Theme = "blue"
        Desc = "Cung cấp biểu đồ trực quan hóa dữ liệu doanh thu bán hàng, báo cáo sản lượng trái cây bán ra và thống kê hành vi mua sắm của khách hàng."
        Flow = @(
            @{ Name = "Yêu cầu báo cáo"; Desc = "Chủ shop mở trang báo cáo doanh thu <code>/shop/report</code> hoặc Admin mở <code>/admin/report</code>." },
            @{ Name = "Tổng hợp dữ liệu số"; Desc = "Servlet gọi <code>ReportService</code> thực hiện các câu lệnh SQL gom nhóm (<code>GROUP BY</code>) và tính tổng doanh thu theo ngày/tháng/năm từ cơ sở dữ liệu." },
            @{ Name = "Trả kết quả JSON"; Desc = "Hệ thống trả về chuỗi cấu trúc dữ liệu JSON biểu diễn doanh số bán hàng." },
            @{ Name = "Vẽ biểu đồ động"; Desc = "Mã JavaScript tại trang giao diện nhận dữ liệu JSON, gọi thư viện Chart.js để vẽ biểu đồ đường (line chart) hoặc cột (bar chart) trực quan." }
        )
        Classes = @(
            @{ Name = "ShopReportServlet"; Type = "Servlet"; Desc = "Tổng hợp dữ liệu doanh số và mặt hàng bán chạy nhất cho shop." },
            @{ Name = "AdminReportServlet"; Type = "Servlet"; Desc = "Tổng hợp báo cáo doanh số toàn sàn giao dịch cho Admin." },
            @{ Name = "ReportService"; Type = "Service"; Desc = "Tầng nghiệp vụ thực hiện phân tích số liệu tài chính hệ thống." },
            @{ Name = "OrderDAO"; Type = "DAO"; Desc = "Cung cấp các hàm truy vấn tính tổng tiền hóa đơn thành công theo kỳ mốc thời gian." },
            @{ Name = "report.jsp"; Type = "JSP"; Desc = "Giao diện báo cáo doanh thu tích hợp biểu đồ Chart.js động." }
        )
    },
    @{
        Id = 32
        Filename = "shop_settlement_batch_job.html"
        Title = "Settlement – Đối soát & Tự động quyết toán Shop"
        Iteration = 3
        Theme = "orange"
        Desc = "Quy trình đối soát tự động hàng ngày (Batch Job) gom các đơn hàng hoàn thành để tính toán số tiền thực nhận của Shop sau khi trừ phí nền tảng."
        Flow = @(
            @{ Name = "Kích hoạt Batch Job"; Desc = "Một bộ lập lịch tự động (Scheduler) kích hoạt tiến trình quyết toán vào lúc nửa đêm hàng ngày." },
            @{ Name = "Thực thi tính toán"; Desc = "Tiến trình gọi <code>SettlementService.calculateDailySettlements()</code> chạy trong một DB Transaction:" },
            @{ Name = "Gom đơn hàng hợp lệ"; Desc = "Hệ thống truy vấn tất cả đơn hàng đã giao thành công (<code>DELIVERED</code>) trong chu kỳ chưa được quyết toán." },
            @{ Name = "Tính toán khấu trừ"; Desc = "Tính tổng tiền bán (gross), trừ đi phí nền tảng (platform fee), trừ tiền đổi trả/hoàn (refund) để tính ra tiền thực trả (net)." },
            @{ Name = "Lập phiếu quyết toán"; Desc = "Tạo bản ghi trong <code>shop_settlements</code> và liên kết chi tiết đơn hàng tương ứng vào <code>shop_settlement_orders</code>." },
            @{ Name = "Giải ngân tiền"; Desc = "Chủ shop xem bảng quyết toán tại <code>/shop/settlement</code>. Admin duyệt thanh toán và chuyển tiền, đổi trạng thái phiếu quyết toán thành <code>PAID</code>." }
        )
        Classes = @(
            @{ Name = "SettlementServlet"; Type = "Servlet"; Desc = "Hiển thị danh sách phiếu quyết toán doanh thu bán hàng của shop qua các thời kỳ." },
            @{ Name = "AdminSettlementServlet"; Type = "Servlet"; Desc = "Giao diện quản lý danh sách đối soát và duyệt giải ngân tài chính cho shop của Admin." },
            @{ Name = "SettlementService"; Type = "Service"; Desc = "Quản lý luồng đối soát tự động hàng ngày, tính tiền thực nhận (net) và cập nhật giải ngân." },
            @{ Name = "SettlementDAO"; Type = "DAO"; Desc = "Tạo phiếu quyết toán mới và ghi nhận chi tiết đối chiếu các hóa đơn trong DB." },
            @{ Name = "ShopSettlement"; Type = "Entity"; Desc = "Entity ánh xạ bảng shop_settlements lưu trữ thông tin phiếu quyết toán của shop." },
            @{ Name = "settlement.jsp"; Type = "JSP"; Desc = "Trang hiển thị danh sách các đợt quyết toán tài chính và chi tiết đơn đối chiếu của shop." }
        )
    }
)

# Template for generating HTML documents
$htmlTemplate = @"
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Feature {0} — {1}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <style>
        :root {{
            --bg-primary: #0b0e14;
            --bg-secondary: #141820;
            --bg-card: #1e2530;
            --bg-hover: #262f3e;
            --border: #232d3d;
            --border-accent: #34435b;
            --text-primary: #ecf0f6;
            --text-secondary: #a0aec0;
            --text-muted: #5e6b7e;

            /* Accent Colors */
            --accent-cyan: #22d3ee;
            --accent-cyan-bg: rgba(34, 211, 238, 0.08);
            --accent-blue: #60a5fa;
            --accent-blue-bg: rgba(96, 165, 250, 0.08);
            --accent-green: #34d399;
            --accent-green-bg: rgba(52, 211, 153, 0.08);
            --accent-purple: #a78bfa;
            --accent-purple-bg: rgba(167, 139, 250, 0.08);
            --accent-orange: #fb923c;
            --accent-orange-bg: rgba(251, 146, 60, 0.08);
            --accent-yellow: #facc15;
            --accent-yellow-bg: rgba(250, 204, 21, 0.08);
            --accent-red: #f87171;
            --accent-red-bg: rgba(248, 113, 113, 0.08);

            --shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
            --radius: 12px;
            --radius-sm: 8px;
        }}
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{
            font-family: 'Inter', sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            min-height: 100vh;
            line-height: 1.7;
        }}

        /* ── Header ── */
        .header {{
            background: linear-gradient(135deg, #101622 0%, #0b0e14 100%);
            border-bottom: 1px solid var(--border);
            padding: 36px 24px;
            text-align: center;
        }}
        .header h1 {{
            font-size: 1.6rem;
            font-weight: 800;
            background: linear-gradient(135deg, var(--accent-{2}), #ecf0f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: -0.5px;
        }}
        .header p {{
            color: var(--text-secondary);
            font-size: 0.85rem;
            margin-top: 8px;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
        }}
        .iteration-badge {{
            display: inline-block;
            padding: 3px 12px;
            border-radius: 999px;
            font-size: 0.65rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            background: var(--accent-{2}-bg);
            color: var(--accent-{2});
            border: 1px solid rgba(255,255,255,0.05);
            margin-bottom: 10px;
        }}

        /* ── Container ── */
        .container {{
            max-width: 840px;
            margin: 0 auto;
            padding: 40px 20px 80px;
        }}

        /* ── Section Title ── */
        .section-title {{
            font-size: 1.1rem;
            font-weight: 700;
            margin-bottom: 24px;
            padding-bottom: 10px;
            border-bottom: 1px solid var(--border);
            display: flex;
            align-items: center;
            gap: 8px;
        }}
        .section-title::before {{
            content: '';
            display: inline-block;
            width: 4px;
            height: 16px;
            background: var(--accent-{2});
            border-radius: 2px;
        }}

        /* ── Flow Container ── */
        .flow {{
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-bottom: 48px;
        }}

        /* ── Step Card ── */
        .step {{
            width: 100%;
            background: var(--bg-secondary);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 20px 24px;
            position: relative;
            transition: all 0.25s ease;
        }}
        .step:hover {{
            border-color: var(--border-accent);
            transform: translateY(-2px);
            box-shadow: var(--shadow);
        }}
        .step-number {{
            position: absolute;
            top: -12px;
            left: 20px;
            width: 26px;
            height: 26px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.72rem;
            font-weight: 800;
            border: 2px solid var(--bg-primary);
            background: var(--accent-{2});
            color: #0b0e14;
        }}
        .step-header {{
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
        }}
        .step-title {{
            font-size: 0.95rem;
            font-weight: 700;
            color: var(--text-primary);
        }}
        .step-desc {{
            font-size: 0.8rem;
            color: var(--text-secondary);
            line-height: 1.65;
        }}
        .step-desc code {{
            background: rgba(255,255,255,0.06);
            color: var(--accent-blue);
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 0.75rem;
            font-family: 'JetBrains Mono', monospace;
        }}

        /* ── Arrow Connector ── */
        .arrow {{
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 6px 0;
            color: var(--text-muted);
        }}
        .arrow .line {{
            width: 2px;
            height: 20px;
            background: linear-gradient(180deg, var(--border-accent), var(--border));
            border-radius: 1px;
        }}
        .arrow .head {{
            font-size: 0.65rem;
            line-height: 1;
            margin-top: -3px;
        }}

        /* ── Class Card ── */
        .summary-card {{
            background: var(--bg-secondary);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 24px;
            box-shadow: var(--shadow);
        }}
        .summary-card table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 0.78rem;
            text-align: left;
        }}
        .summary-card th, .summary-card td {{
            padding: 10px 12px;
            border-bottom: 1px solid var(--border);
        }}
        .summary-card th {{
            color: var(--text-muted);
            font-weight: 700;
            text-transform: uppercase;
            font-size: 0.68rem;
            letter-spacing: 0.5px;
        }}
        .summary-card td {{
            color: var(--text-secondary);
        }}
        .summary-card td.class-name {{
            font-family: 'JetBrains Mono', monospace;
            font-weight: 600;
            color: var(--accent-{2});
        }}
        .badge-type {{
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.62rem;
            font-weight: 700;
            text-transform: uppercase;
            background: rgba(255,255,255,0.05);
            color: var(--text-secondary);
            border: 1px solid rgba(255,255,255,0.03);
        }}
        .badge-servlet   {{ color: var(--accent-cyan); background: var(--accent-cyan-bg); }}
        .badge-service   {{ color: var(--accent-blue); background: var(--accent-blue-bg); }}
        .badge-dao       {{ color: var(--accent-green); background: var(--accent-green-bg); }}
        .badge-entity    {{ color: var(--accent-purple); background: var(--accent-purple-bg); }}
        .badge-jsp       {{ color: var(--accent-yellow); background: var(--accent-yellow-bg); }}
        .badge-dbtable   {{ color: var(--accent-orange); background: var(--accent-orange-bg); }}
        .badge-utility   {{ color: var(--accent-red); background: var(--accent-red-bg); }}
        .badge-filter    {{ color: var(--accent-cyan); background: var(--accent-cyan-bg); }}
        .badge-tag       {{ color: var(--accent-purple); background: var(--accent-purple-bg); }}

        .footer-nav {{
            margin-top: 40px;
            display: flex;
            justify-content: center;
            gap: 12px;
        }}
        .btn-back {{
            display: inline-block;
            padding: 8px 20px;
            border-radius: 999px;
            font-size: 0.75rem;
            font-weight: 600;
            text-decoration: none;
            color: var(--text-secondary);
            border: 1px solid var(--border);
            transition: all 0.2s;
        }}
        .btn-back:hover {{
            color: var(--text-primary);
            border-color: var(--border-accent);
            background: rgba(255,255,255,0.02);
        }}
    </style>
</head>
<body>

<div class="header">
    <div class="iteration-badge">Iteration {3}</div>
    <h1>📖 Chức năng {0}: {1}</h1>
    <p>{4}</p>
</div>

<div class="container">
    <div class="section-title">Luồng Hoạt Động (Data Flow)</div>
    <div class="flow">
        {5}
    </div>

    <div class="section-title">Các Thành Phần & Lớp Liên Quan</div>
    <div class="summary-card">
        <table>
            <thead>
                <tr>
                    <th>Tên lớp / Tên bảng</th>
                    <th>Loại thành phần</th>
                    <th>Nhiệm vụ trong chức năng</th>
                </tr>
            </thead>
            <tbody>
                {6}
            </tbody>
        </table>
    </div>

    <div class="footer-nav">
        <a href="../Restock_Process_Flow.html" class="btn-back">← Quay lại Process Flow</a>
        <a href="../Restock_Code_Explanation.html" class="btn-back">Xem giải thích code →</a>
    </div>
</div>

</body>
</html>
"@

# Make sure folder feature exists
$currentDir = Get-Location
$featureFolder = Join-Path $currentDir "docs/HaiPT/feature"
if (-not (Test-Path $featureFolder)) {
    New-Item -ItemType Directory -Path $featureFolder -Force
}

# Generate each file
foreach ($f in $features) {
    $stepsHtml = ""
    for ($i = 0; $i -lt $f.Flow.Count; $i++) {
        $step = $f.Flow[$i]
        $stepNum = $i + 1

        $stepsHtml += @"
        <div class="step">
            <div class="step-number">$stepNum</div>
            <div class="step-header">
                <span class="step-title">$($step.Name)</span>
            </div>
            <div class="step-desc">$($step.Desc)</div>
        </div>
"@
        if ($i -lt $f.Flow.Count - 1) {
            $stepsHtml += @"
        <div class="arrow">
            <div class="line"></div>
            <span class="head">▼</span>
        </div>
"@
        }
    }

    $classesHtml = ""
    foreach ($c in $f.Classes) {
        $badgeClass = "badge-type"
        if ($c.Type -eq "Servlet") { $badgeClass += " badge-servlet" }
        elseif ($c.Type -eq "Service") { $badgeClass += " badge-service" }
        elseif ($c.Type -eq "DAO") { $badgeClass += " badge-dao" }
        elseif ($c.Type -eq "Entity") { $badgeClass += " badge-entity" }
        elseif ($c.Type -eq "JSP") { $badgeClass += " badge-jsp" }
        elseif ($c.Type -eq "DB Table") { $badgeClass += " badge-dbtable" }
        elseif ($c.Type -eq "Utility") { $badgeClass += " badge-utility" }
        elseif ($c.Type -eq "Filter") { $badgeClass += " badge-filter" }
        elseif ($c.Type -eq "Tag") { $badgeClass += " badge-tag" }

        $classesHtml += @"
                <tr>
                    <td class="class-name">$($c.Name)</td>
                    <td><span class="$badgeClass">$($c.Type)</span></td>
                    <td>$($c.Desc)</td>
                </tr>
"@
    }

    # Format HTML content
    $content = $htmlTemplate -f $f.Id, $f.Title, $f.Theme, $f.Iteration, $f.Desc, $stepsHtml, $classesHtml

    # Write out file
    $outputPath = Join-Path $featureFolder $f.Filename
    $content | Out-File -FilePath $outputPath -Encoding UTF8 -Force
    Write-Host "Generated: $($f.Filename) at $outputPath"
}

Write-Host "Successfully generated all 32 feature explanation documents!"
