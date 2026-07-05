package model.dto.common;

import util.PaginationHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * PageRequest - OOP request model containing pagination parameters and dynamic filters.
 */
public class PageRequest {

    private int page;
    private int pageSize;
    private final Map<String, Object> filters = new HashMap<>();

    public PageRequest(int page, int pageSize) {
        this.page = Math.max(1, page);
        this.pageSize = Math.max(1, pageSize);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
    }

    public int getOffset() {
        return PaginationHelper.calculateOffset(page, pageSize);
    }

    public void addFilter(String key, Object value) {
        if (value != null) {
            filters.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getFilter(String key, T defaultValue) {
        Object val = filters.get(key);
        if (val == null) {
            return defaultValue;
        }
        return (T) val;
    }

    public boolean hasFilter(String key) {
        return filters.containsKey(key);
    }

    public Map<String, Object> getFilters() {
        return filters;
    }
}
