package com.fruitmkt.test;
import com.fruitmkt.dao.CategoryDAO;
public class TestCategory {
    public static void main(String[] args) {
        try {
            CategoryDAO dao = new CategoryDAO();
            System.out.println("Categories: " + dao.findAll().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
