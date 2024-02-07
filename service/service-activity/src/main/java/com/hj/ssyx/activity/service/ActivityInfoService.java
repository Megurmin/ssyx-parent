package com.hj.ssyx.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.activity.ActivityInfo;
import com.hj.ssyx.model.activity.ActivityRule;
import com.hj.ssyx.model.order.CartInfo;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.vo.activity.ActivityRuleVo;
import com.hj.ssyx.vo.order.CartInfoVo;
import com.hj.ssyx.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:05 2023/10/1
 */
public interface ActivityInfoService extends IService<ActivityInfo> {
    IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam);

    Map<String, Object> findActivityRuleList(Long activityId);

    List<ActivityRule> findActivityRuleBySkuId(Long skuId);

    void saveActivityRule(ActivityRuleVo activityRuleVo);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    //根据skuId获取营销数据和优惠卷
    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    //获取购物车里面满足条件的优惠卷和活动信息
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    // 获取购物车对应规则数据
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
