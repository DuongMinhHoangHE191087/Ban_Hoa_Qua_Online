package com.fruitmkt.test;

import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.model.entity.Product;
import java.util.List;

public class TestDB {
    public static void main(String[] args) {
        try {
            ProductDAO dao = new ProductDAO();
            ProductImageDAO imgDao = new ProductImageDAO();
            ProductVariantDAO varDao = new ProductVariantDAO();
            
            List<Product> list = dao.findAll(1, 8);
            System.out.println("Products found: " + list.size());
            for (Product p : list) {
                System.out.println("Product: " + p.getProductId());
                imgDao.findPrimary(p.getProductId());
                varDao.findByProduct(p.getProductId());
            }
            System.out.println("All DAO calls succeeded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
