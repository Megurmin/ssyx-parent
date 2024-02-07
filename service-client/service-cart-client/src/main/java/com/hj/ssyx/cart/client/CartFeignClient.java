package com.hj.ssyx.cart.client;

import com.hj.ssyx.model.order.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:41 2024/1/17
 */
@FeignClient("service-cart")
public interface CartFeignClient {
    // 获取当前用户购物车选中的购物项
    @GetMapping("/api/cart/inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId);
}
