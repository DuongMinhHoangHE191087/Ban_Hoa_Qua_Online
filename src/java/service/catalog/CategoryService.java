package service.catalog;

import dao.catalog.CategoryDAO;
import model.entity.catalog.Category;

import java.sql.SQLException;
import java.util.List;

/**
 * CategoryService — Lớp xử lý nghiệp vụ cho Category (Danh mục sản phẩm).
 *
 * SRP: Validate dữ liệu đầu vào, áp dụng quy tắc nghiệp vụ (Business Rules)
 * và gọi tầng DAO để truy xuất dữ liệu.
 *
 * Cache TTL 5 phút cho getAllCategories() và getActiveCategories() —
 * danh mục ít thay đổi, tránh truy vấn DB mỗi request.
 *
 * @author fruitmkt-team
 */
public class CategoryService {

    private static final long CACHE_TTL_MS = 5 * 60 * 1000L; // 5 phút

    private static final Object CACHE_LOCK = new Object();

    // Cache cho toàn bộ danh mục (kể cả inactive)
    private static volatile List<Category> allCache = null;
    private static volatile long allCacheExpiry = 0;

    // Cache riêng cho danh mục đang active
    private static volatile List<Category> activeCache = null;
    private static volatile long activeCacheExpiry = 0;

    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Lấy toàn bộ danh mục sản phẩm, sắp xếp theo thứ tự hiển thị.
     * Kết quả được cache 5 phút — tránh truy vấn DB mỗi request trang catalog.
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> cached = allCache;
        if (cached != null && System.currentTimeMillis() <= allCacheExpiry) return cached;
        synchronized (CACHE_LOCK) {
            cached = allCache;
            if (cached != null && System.currentTimeMillis() <= allCacheExpiry) return cached;
            allCache = categoryDAO.findAll();
            allCacheExpiry = System.currentTimeMillis() + CACHE_TTL_MS;
            return allCache;
        }
    }

    /**
     * Lấy danh sách các danh mục đang hoạt động (hiển thị công khai).
     * Kết quả được cache 5 phút.
     */
    public List<Category> getActiveCategories() throws SQLException {
        List<Category> cached = activeCache;
        if (cached != null && System.currentTimeMillis() <= activeCacheExpiry) return cached;
        synchronized (CACHE_LOCK) {
            cached = activeCache;
            if (cached != null && System.currentTimeMillis() <= activeCacheExpiry) return cached;
            activeCache = categoryDAO.findAllActive();
            activeCacheExpiry = System.currentTimeMillis() + CACHE_TTL_MS;
            return activeCache;
        }
    }

    /** Xóa cache sau khi có thay đổi danh mục (tạo/sửa/xóa/toggle). */
    private static void invalidateCache() {
        synchronized (CACHE_LOCK) {
            allCache = null;
            activeCache = null;
        }
    }

    /**
     * Lấy chi tiết danh mục theo ID.
     *
     * @param categoryId ID danh mục
     * @return đối tượng Category hoặc null nếu không tìm thấy
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public Category getCategoryById(int categoryId) throws SQLException {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }
        List<Category> list = categoryDAO.findById(categoryId);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Tạo danh mục mới.
     *
     * @param category đối tượng danh mục cần lưu
     * @return ID tự tăng của danh mục mới tạo
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public int createCategory(Category category) throws SQLException {
        validateCategory(category);
        int id = categoryDAO.save(category);
        invalidateCache();
        return id;
    }

    /**
     * Cập nhật thông tin danh mục.
     *
     * @param category đối tượng danh mục chứa thông tin cập nhật
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public void updateCategory(Category category) throws SQLException {
        if (category == null || category.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Thông tin danh mục không hợp lệ để cập nhật.");
        }
        validateCategory(category);
        categoryDAO.update(category);
        invalidateCache();
    }

    /**
     * Xóa danh mục sản phẩm khỏi cơ sở dữ liệu.
     * Chỉ cho phép xóa khi danh mục không chứa bất kỳ sản phẩm nào còn hoạt động.
     *
     * @param categoryId ID danh mục cần xóa
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     * @throws IllegalStateException nếu danh mục chứa sản phẩm hoạt động
     */
    public void deleteCategory(int categoryId) throws SQLException {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }
        if (categoryDAO.hasActiveProducts(categoryId)) {
            throw new IllegalStateException("Không thể xóa danh mục đang có sản phẩm hoạt động!");
        }
        categoryDAO.delete(categoryId);
        invalidateCache();
    }

    /**
     * Bật/tắt trạng thái hoạt động của danh mục.
     *
     * @param categoryId ID danh mục cần chuyển đổi trạng thái
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu
     */
    public void toggleCategoryStatus(int categoryId) throws SQLException {
        Category category = getCategoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Danh mục không tồn tại.");
        }
        category.setIsActive(!category.getIsActive());
        categoryDAO.update(category);
        invalidateCache();
    }

    /**
     * Validate thông tin danh mục (tên, slug).
     *
     * @param category đối tượng danh mục cần kiểm tra
     */
    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Dữ liệu danh mục không được rỗng (null).");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Slug danh mục không được để trống.");
        }
    }
}
