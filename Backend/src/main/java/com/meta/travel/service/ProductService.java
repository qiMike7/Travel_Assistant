package com.meta.travel.service;

import com.meta.travel.common.BusinessException;
import com.meta.travel.common.ResultCode;
import com.meta.travel.entity.Product;
import com.meta.travel.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商城商品服务（对应 guide.vue 精品商城）
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> list(String category) {
        if (StringUtils.hasText(category)) {
            return productRepository.findByCategoryAndStatusOrderByIdAsc(category, 1);
        }
        return productRepository.findByStatusOrderByIdAsc(1);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "商品不存在"));
    }
}
