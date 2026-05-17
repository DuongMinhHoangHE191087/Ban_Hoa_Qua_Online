package com.fruitmkt.model.dto;

/**
 * /** DTO dùng chung cho mọi trang có phân trang. Servlet set vào request attribute. */
 * @author fruitmkt-team
 */
public class PagedResultDTO {

        private java.util.List<?> items;  // Danh sách record của trang hiện tại
        private int currentPage;          // Trang hiện tại (1-based)
        private int totalPages;           // Tổng số trang
        private long totalItems;          // Tổng số record
        private int pageSize;             // Số record/trang

    // TODO: Thêm constructor, getters, setters theo fields bên trên
    public PagedResultDTO() {}
}
