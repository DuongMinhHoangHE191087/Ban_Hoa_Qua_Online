package model.dto.common;
import model.dto.common.PagedResultDTO;

import java.util.List;

/**
 * DTO dùng chung cho mọi trang có phân trang. Servlet set vào request attribute.
 * 
 * @author fruitmkt-team
 */
public class PagedResultDTO {

    private List<?> items;  // Danh sách record của trang hiện tại
    private int currentPage;          // Trang hiện tại (1-based)
    private int totalPages;           // Tổng số trang
    private long totalItems;          // Tổng số record
    private int pageSize;             // Số record/trang

    public PagedResultDTO() {}

    public PagedResultDTO(List<?> items, int currentPage, int totalPages, long totalItems, int pageSize) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
    }

    public List<?> getItems() {
        return items;
    }

    public void setItems(List<?> items) {
        this.items = items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
