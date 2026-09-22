package com.meta.travel.controller;

import com.meta.travel.common.Result;
import com.meta.travel.entity.Product;
import com.meta.travel.security.PublicApi;
import com.meta.travel.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商城商品接口（对应 guide.vue 精品商城）
 */
@PublicApi
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<List<Product>> list(@RequestParam(required = false) String category) {
        return Result.success(productService.list(category));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }
}
