package com.hj.ssyx.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.ssyx.model.activity.ActivityInfo;
import com.hj.ssyx.model.activity.ActivityRule;
import com.hj.ssyx.model.activity.ActivitySku;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HuangJin
 * @date 16:06 2023/10/1
 */
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    // 查询已经参加活动的sku
    List<Long> selectSkuIdListExist(@Param("skuIdList") List<Long> skuIdList);

    // 根据skuId进行查询，查询sku对应活动里面的规则列表
    List<ActivityRule> findActivityRule(@Param("skuId") Long skuId);

    // 根据所有skuId列表获取参与的活动
    List<ActivitySku> selectCartActivity(@Param("skuIdList") List<Long> skuIdList);
}
