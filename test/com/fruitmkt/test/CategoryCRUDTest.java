package com.fruitmkt.test;

import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.service.CategoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

/**
 * CategoryCRUDTest — Bộ kiểm thử JUnit 4 cho CategoryService.
 */
public class CategoryCRUDTest {

    private CategoryService categoryService;
    private int testCategoryId = -1;

    @Before
    public void setUp() {
        categoryService = new CategoryService();
    }

    @After
    public void tearDown() {
        if (testCategoryId != -1) {
            try {
                // Sử dụng DAO trực tiếp ở bước dọn dẹp để đảm bảo dọn sạch DB không bị chặn bởi logic ràng buộc
                new CategoryDAO().delete(testCategoryId);
            } catch (SQLException ignored) {
                // Bỏ qua lỗi dọn dẹp nếu danh mục đã được xóa trong test case
            }
        }
    }

    @Test
    public void testCategoryLifecycle() throws SQLException {
        // 1. Tạo mới và lưu danh mục qua Service
        Category category = new Category();
        category.setName("Test Category JUnit " + System.currentTimeMillis());
        category.setSlug("test-category-junit-" + System.currentTimeMillis());
        category.setDisplayOrder(99);
        category.setIsActive(true);

        testCategoryId = categoryService.createCategory(category);
        assertTrue("Mã danh mục được sinh tự động phải lớn hơn 0", testCategoryId > 0);

        // 2. Đọc thông tin danh mục qua Service
        Category fetched = categoryService.getCategoryById(testCategoryId);
        assertNotNull("Danh mục lấy theo ID không được null", fetched);
        assertEquals("Tên danh mục không khớp", category.getName(), fetched.getName());
        assertEquals("Slug danh mục không khớp", category.getSlug(), fetched.getSlug());
        assertEquals("Thứ tự hiển thị không khớp", 99, fetched.getDisplayOrder());
        assertTrue("Trạng thái hoạt động ban đầu phải là true", fetched.getIsActive());

        // 3. Cập nhật danh mục qua Service
        fetched.setName("Updated Test Category " + System.currentTimeMillis());
        fetched.setSlug("updated-test-category-" + System.currentTimeMillis());
        fetched.setDisplayOrder(100);
        fetched.setIsActive(false);
        categoryService.updateCategory(fetched);

        Category updated = categoryService.getCategoryById(testCategoryId);
        assertEquals("Tên cập nhật không đúng", fetched.getName(), updated.getName());
        assertEquals("Slug cập nhật không đúng", fetched.getSlug(), updated.getSlug());
        assertEquals("Thứ tự hiển thị cập nhật không đúng", 100, updated.getDisplayOrder());
        assertFalse("Trạng thái hoạt động cập nhật phải là false", updated.getIsActive());

        // 4. Xóa danh mục qua Service
        categoryService.deleteCategory(testCategoryId);
        Category deleted = categoryService.getCategoryById(testCategoryId);
        assertNull("Danh mục không được tồn tại sau khi xóa", deleted);
        testCategoryId = -1; // Đánh dấu đã xóa thành công để tránh lỗi dọn dẹp ở tearDown
    }

    @Test
    public void testFindAllActive() throws SQLException {
        Category category = new Category();
        category.setName("Active JUnit Cat " + System.currentTimeMillis());
        category.setSlug("active-junit-cat-" + System.currentTimeMillis());
        category.setDisplayOrder(50);
        category.setIsActive(true);

        testCategoryId = categoryService.createCategory(category);
        
        List<Category> activeCats = categoryService.getActiveCategories();
        boolean found = false;
        for (Category c : activeCats) {
            if (c.getCategoryId() == testCategoryId) {
                found = true;
                break;
            }
        }
        assertTrue("Danh mục active mới tạo phải nằm trong kết quả trả về của getActiveCategories()", found);
    }
}
