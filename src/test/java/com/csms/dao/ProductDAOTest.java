package com.csms.dao;

import com.csms.entity.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductDAOTest {

    @Test
    void shouldReturnProductsFromDatabase() {
        ProductDAO productDAO = new ProductDAO();

        List<Product> products = productDAO.findAll();

        products.forEach(product -> System.out.println(
                product.getId()
                        + " | "
                        + product.getName()
                        + " | "
                        + product.getCategoryName()
                        + " | "
                        + product.getPrice()));

        assertFalse(
                products.isEmpty(),
                "Danh sách sản phẩm không được rỗng");
    }
}