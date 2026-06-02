<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Đăng bán sản phẩm mới"/>
</jsp:include>

<!-- Google Fonts & Tailwind isolated engine to match Premium HomeUI -->
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    "primary": "#4d661c",          // Emerald Green
                    "primary-hover": "#364e03",
                    "primary-light": "#d9f99d",
                    "secondary": "#31694b",        // Deep Forest Green
                    "surface-bright": "#eaffea",   // Warm Mint Light
                    "surface-container-low": "#d1ffd8",
                    "on-surface": "#00210d",
                    "on-surface-variant": "#44483b",
                    "tertiary": "#486554",
                    "tertiary-container": "#d5f5e0"
                },
                fontFamily: {
                    sans: ["Lexend", "sans-serif"]
                }
            }
        }
    }
</script>

<style>
    .glass-panel {
        background: rgba(255, 255, 255, 0.75);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.4);
    }
    .ambient-shadow {
        box-shadow: 0 10px 40px rgba(20, 83, 45, 0.05);
    }
    .navbar {
        font-family: 'Lexend', sans-serif !important;
    }
</style>

<!-- Main Wrapper -->
<div class="bg-gradient-to-br from-surface-bright via-white to-surface-container-low min-h-screen text-on-surface antialiased font-sans pt-24 pb-20">
    <div class="max-w-4xl mx-auto px-4 sm:px-6">
        
        <!-- Header & Back Button -->
        <div class="flex items-center gap-4 mb-8">
            <a href="${pageContext.request.contextPath}/shop/products" 
               class="w-10 h-10 rounded-2xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 flex items-center justify-center cursor-pointer">
                <span class="material-symbols-outlined text-[20px] font-bold">arrow_back</span>
            </a>
            <div>
                <h1 class="text-2xl font-extrabold tracking-tight">🍎 ĐĂNG BÁN TRÁI CÂY MỚI</h1>
                <p class="text-xs text-on-surface-variant font-light mt-0.5">Điền các thông tin sản phẩm và tải lên các bức hình thật tươi ngon bắt mắt.</p>
            </div>
        </div>

        <!-- Validation Errors Notification -->
        <c:if test="${not empty errors}">
            <div class="mb-6 p-4 rounded-3xl glass-panel border-l-4 border-red-500 bg-red-50/50 text-red-700 shadow-sm animate-pulse">
                <div class="flex gap-2 items-center mb-2">
                    <span class="material-symbols-outlined text-[22px]">warning</span>
                    <span class="font-bold text-sm">Vui lòng sửa các lỗi sau để tiếp tục:</span>
                </div>
                <ul class="list-disc list-inside text-xs space-y-1 pl-2 font-medium text-red-600">
                    <c:forEach var="err" items="${errors}">
                        <li><c:out value="${err}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <!-- Form container -->
        <form action="${pageContext.request.contextPath}/shop/product-create" method="post" enctype="multipart/form-data"
              class="glass-panel rounded-3xl p-6 md:p-8 ambient-shadow space-y-6 bg-white/70">
            
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

            <!-- 1. Basic Information Component -->
            <div>
                <h2 class="text-base font-bold text-primary border-b border-primary/10 pb-2 mb-4 flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[18px]">info</span>
                    Thông tin cơ bản
                </h2>
                
                <div class="space-y-4">
                    <!-- Product Name -->
                    <div>
                        <label for="name" class="block text-xs font-semibold text-on-surface mb-1">Tên sản phẩm hoa quả <span class="text-red-500">*</span></label>
                        <input type="text" name="name" id="name" required placeholder="Ví dụ: Sầu Riêng Ri6 Chín Hóa Thơm Lừng"
                               value="<c:out value="${oldName}"/>"
                               class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                    </div>

                    <!-- Description -->
                    <div>
                        <label for="description" class="block text-xs font-semibold text-on-surface mb-1">Mô tả sản phẩm</label>
                        <textarea name="description" id="description" rows="4" placeholder="Nhập câu chuyện sản phẩm, hương vị béo ngậy ngọt ngào, chứng chỉ VietGAP..."
                                  class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm"><c:out value="${oldDescription}"/></textarea>
                    </div>

                    <!-- Category & Unit -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label for="categoryId" class="block text-xs font-semibold text-on-surface mb-1">Danh mục sản phẩm <span class="text-red-500">*</span></label>
                            <select name="categoryId" id="categoryId" required
                                    class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                                <option value="">-- Chọn danh mục --</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.categoryId}" ${oldCategoryId == cat.categoryId ? 'selected' : ''}>
                                        <c:out value="${cat.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div>
                            <label for="unit" class="block text-xs font-semibold text-on-surface mb-1">Đơn vị tính bán lẻ <span class="text-red-500">*</span></label>
                            <input type="text" name="unit" id="unit" required placeholder="Ví dụ: kg, Hộp 500g, Quả..."
                                   value="<c:out value="${oldUnit != null ? oldUnit : 'kg'}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                        </div>
                    </div>
                </div>
            </div>

            <!-- 2. Price and Inventory Component -->
            <div>
                <h2 class="text-base font-bold text-primary border-b border-primary/10 pb-2 mb-4 flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[18px]">payments</span>
                    Giá bán & Kho hàng
                </h2>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <!-- Price -->
                    <div>
                        <label for="price" class="block text-xs font-semibold text-on-surface mb-1">Giá bán lẻ (VND) <span class="text-red-500">*</span></label>
                        <div class="relative rounded-xl shadow-sm">
                            <input type="number" name="price" id="price" required min="1000" step="1000" placeholder="50000"
                                   value="<c:out value="${oldPrice}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm pl-4 pr-12 focus:border-primary focus:ring-primary">
                            <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-xs text-on-surface-variant/50 font-bold">VND</div>
                        </div>
                    </div>

                    <!-- Stock -->
                    <div>
                        <label for="stock" class="block text-xs font-semibold text-on-surface mb-1">Số lượng tồn kho ban đầu <span class="text-red-500">*</span></label>
                        <input type="number" name="stock" id="stock" required min="0" placeholder="100"
                               value="<c:out value="${oldStock}"/>"
                               class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                    </div>
                </div>
            </div>

            <!-- 3. Quality & Expiry Metadata Component -->
            <div>
                <h2 class="text-base font-bold text-primary border-b border-primary/10 pb-2 mb-4 flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[18px]">verified_user</span>
                    Xuất xứ & Hạn sử dụng
                </h2>
                
                <div class="space-y-4">
                    <!-- Country and Region -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label for="originCountry" class="block text-xs font-semibold text-on-surface mb-1">Quốc gia xuất xứ</label>
                            <input type="text" name="originCountry" id="originCountry" placeholder="Ví dụ: Việt Nam, New Zealand..."
                                   value="<c:out value="${oldOriginCountry != null ? oldOriginCountry : 'Việt Nam'}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                        </div>
                        <div>
                            <label for="originRegion" class="block text-xs font-semibold text-on-surface mb-1">Vùng sản xuất thu hoạch</label>
                            <input type="text" name="originRegion" id="originRegion" placeholder="Ví dụ: Đắk Lắk, Đà Lạt, Lục Ngạn..."
                                   value="<c:out value="${oldOriginRegion}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                        </div>
                    </div>

                    <!-- Dates and storage -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label for="harvestDate" class="block text-xs font-semibold text-on-surface mb-1">Ngày thu hoạch</label>
                            <input type="date" name="harvestDate" id="harvestDate"
                                   value="<c:out value="${oldHarvestDate}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                        </div>
                        <div>
                            <label for="shelfLifeDays" class="block text-xs font-semibold text-on-surface mb-1">Hạn sử dụng tối đa (Số ngày)</label>
                            <input type="number" name="shelfLifeDays" id="shelfLifeDays" min="1" placeholder="Ví dụ: 7 ngày, 14 ngày..."
                                   value="<c:out value="${oldShelfLife}"/>"
                                   class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                        </div>
                    </div>

                    <div>
                        <label for="storageInstruction" class="block text-xs font-semibold text-on-surface mb-1">Hướng dẫn bảo quản tốt nhất</label>
                        <input type="text" name="storageInstruction" id="storageInstruction" placeholder="Ví dụ: Bảo quản ngăn mát tủ lạnh nhiệt độ 4-8 độ C..."
                               value="<c:out value="${oldStorageInstruction}"/>"
                               class="w-full rounded-xl border-gray-250/70 text-sm focus:border-primary focus:ring-primary shadow-sm">
                    </div>
                </div>
            </div>

            <!-- 4. Premium Upload Images Component -->
            <div>
                <h2 class="text-base font-bold text-primary border-b border-primary/10 pb-2 mb-4 flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[18px]">photo_library</span>
                    Album hình ảnh hoa quả <span class="text-red-500">*</span>
                </h2>
                
                <!-- Whitelist Notice -->
                <p class="text-xs text-on-surface-variant/70 mb-3 font-light">Mẹo: Tải lên nhiều ảnh khác nhau từ nhiều góc độ giúp khách hàng tin tưởng. Chỉ hỗ trợ các tệp ảnh dạng: <strong>jpg, jpeg, png, webp</strong>. Tệp tin không phải ảnh sẽ bị từ chối.</p>

                <!-- Custom Upload Drop Zone -->
                <div class="relative border-2 border-dashed border-primary/30 hover:border-primary rounded-3xl p-8 text-center transition-all bg-emerald-50/20 hover:bg-emerald-50/40 cursor-pointer group"
                     onclick="document.getElementById('images-input').click();">
                    
                    <input type="file" name="images" id="images-input" multiple accept="image/*" required class="hidden" onchange="previewImages(this)">
                    
                    <span class="material-symbols-outlined text-[48px] text-primary/50 group-hover:scale-110 transition-transform mb-3">cloud_upload</span>
                    <h3 class="text-sm font-bold text-on-surface">Kéo thả các hình ảnh vào đây hoặc nhấp để chọn</h3>
                    <p class="text-xs text-on-surface-variant/60 font-light mt-1">Hỗ trợ chọn nhiều file hình ảnh cùng một lúc</p>
                </div>

                <!-- Preview Area -->
                <div id="file-list-preview" class="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4 hidden">
                    <!-- Dynamic JS elements will render here -->
                </div>
            </div>

            <!-- 5. Form Actions Buttons -->
            <div class="flex justify-end gap-3 pt-6 border-t border-gray-100">
                <a href="${pageContext.request.contextPath}/shop/products"
                   class="border border-gray-300 hover:bg-gray-50 text-on-surface-variant font-bold text-xs px-6 py-3.5 rounded-2xl transition-all active:scale-95 duration-200 cursor-pointer">
                    Hủy bỏ
                </a>
                <button type="submit"
                        class="bg-primary hover:bg-primary-hover text-white font-bold text-xs px-8 py-3.5 rounded-2xl transition-all shadow-md active:scale-95 duration-200 cursor-pointer">
                    Đăng bán ngay
                </button>
            </div>

        </form>

    </div>
</div>

<!-- Image upload preview engine -->
<script>
    function previewImages(input) {
        const previewContainer = document.getElementById('file-list-preview');
        previewContainer.innerHTML = '';
        
        if (input.files && input.files.length > 0) {
            previewContainer.classList.remove('hidden');
            
            Array.from(input.files).forEach((file, index) => {
                const reader = new FileReader();
                
                // Outer card layout
                const card = document.createElement('div');
                card.className = "relative rounded-2xl overflow-hidden aspect-square border border-gray-200 ambient-shadow bg-white p-1 flex flex-col justify-between";
                
                reader.onload = function(e) {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.className = "w-full h-[75%] object-cover rounded-xl";
                    card.appendChild(img);
                    
                    const details = document.createElement('div');
                    details.className = "h-[22%] flex items-center justify-between px-1.5";
                    
                    const filename = document.createElement('span');
                    filename.className = "text-[10px] font-bold text-on-surface truncate w-[60%] block";
                    filename.textContent = file.name;
                    details.appendChild(filename);
                    
                    const badge = document.createElement('span');
                    badge.className = "text-[8px] font-bold px-1.5 py-0.5 rounded-md " + (index === 0 ? 'bg-primary text-white' : 'bg-gray-150 text-on-surface-variant/70');
                    badge.textContent = index === 0 ? 'Ảnh chính' : 'Ảnh phụ';
                    details.appendChild(badge);
                    
                    card.appendChild(details);
                };
                
                reader.readAsDataURL(file);
                previewContainer.appendChild(card);
            });
        } else {
            previewContainer.classList.add('hidden');
        }
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
