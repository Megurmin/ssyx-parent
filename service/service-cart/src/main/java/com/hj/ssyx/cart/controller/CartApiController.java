package com.hj.ssyx.cart.controller;

import com.hj.ssyx.activity.client.ActivityFeignClient;
import com.hj.ssyx.cart.service.CartInfoService;
import com.hj.ssyx.common.auth.AuthContextHolder;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HuangJin
 * @date 15:50 2024/1/7
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartInfoService cartInfoService;

    @Autowired
    private ActivityFeignClient activityFeignClient;


    // 根据skuId选中
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked) {
        //获取userId
        Long userId = AuthContextHolder.getUserId();
        //调用方法
        cartInfoService.checkCart(userId,skuId,isChecked);
        return Result.ok(null);
    }

    // 全选
    @GetMapping("checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable Integer isChecked) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.checkAllCart(userId,isChecked);
        return Result.ok(null);
    }

    // 批量选中
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@RequestBody List<Long> skuIdList,
                                 @PathVariable Integer isChecked) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchCheckCart(skuIdList,userId,isChecked);
        return Result.ok(null);
    }

    @GetMapping("activityCartList")
    public Result activityCartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        OrderConfirmVo orderConfirmVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList,userId);
        return Result.ok(orderConfirmVo);
    }
    // 购物车列表
    @GetMapping("cartList")
    public Result cartList() {
        // 获取userId
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    // 添加商品到购物车
    // 添加内容：当前登录用户id，skuId，商品数量
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        // 获取当前登录用户Id
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId,skuId,skuNum);
        return Result.ok(null);
    }

    // 根据skuId删除购物车
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId,userId);
        return Result.ok(null);
    }

    // 清空购物车
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    // 批量删除购物车 多个skuId
    @DeleteMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(skuIdList,userId);
        return Result.ok(null);
    }

    // 获取当前用户购物车选中的购物项
    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
}
