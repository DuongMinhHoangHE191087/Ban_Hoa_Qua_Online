package com.fruitmkt.model.dto;

import com.fruitmkt.model.entity.Product;

/**
 * ProductDTO - Lớp DTO tối giản kế thừa từ Product để tương thích ngược với JSP,
 * nhưng các trường mô tả lớn (description) hoặc tài liệu xác minh (verificationDocPath)
 * sẽ không được nạp khi hiển thị danh sách sản phẩm nhằm tiết kiệm I/O và dung lượng bộ nhớ.
 */
public class ProductDTO extends Product {
    
    public ProductDTO() {
        super();
    }
}
