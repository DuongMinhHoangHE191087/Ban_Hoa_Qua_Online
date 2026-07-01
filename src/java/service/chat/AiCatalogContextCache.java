package service.chat;

import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache ngắn hạn cho catalog AI để tránh load full context ở mỗi request.
 */
// Touched for IDE re-indexing
public class AiCatalogContextCache {

    private static final long CACHE_TTL_MS = 30_000L;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private volatile Snapshot snapshot;
    private final Object lock = new Object();

    public Snapshot getSnapshot() throws SQLException {
        Snapshot current = snapshot;
        long now = System.currentTimeMillis();
        if (current != null && current.expiresAtMs > now) {
            return current;
        }

        synchronized (lock) {
            current = snapshot;
            if (current != null && current.expiresAtMs > now) {
                return current;
            }
            List<Category> categories = categoryDAO.findAllActive();
            Map<Integer, String> categoryNames = new HashMap<>();
            for (Category category : categories) {
                categoryNames.put(category.getCategoryId(), category.getName());
            }
            List<Product> activeProducts = productDAO.findAllActiveForAI();
            Snapshot refreshed = new Snapshot(
                    new ArrayList<>(categories),
                    new ArrayList<>(activeProducts),
                    new HashMap<>(categoryNames),
                    now + CACHE_TTL_MS);
            snapshot = refreshed;
            return refreshed;
        }
    }

    public static final class Snapshot {
        private final List<Category> categories;
        private final List<Product> activeProducts;
        private final Map<Integer, String> categoryNames;
        private final long expiresAtMs;

        private Snapshot(List<Category> categories,
                         List<Product> activeProducts,
                         Map<Integer, String> categoryNames,
                         long expiresAtMs) {
            this.categories = categories;
            this.activeProducts = activeProducts;
            this.categoryNames = categoryNames;
            this.expiresAtMs = expiresAtMs;
        }

        public List<Category> getCategories() {
            return categories;
        }

        public List<Product> getActiveProducts() {
            return activeProducts;
        }

        public Map<Integer, String> getCategoryNames() {
            return categoryNames;
        }
    }
}
