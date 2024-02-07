package com.hj.ssyx.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.ssyx.model.activity.CouponInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HuangJin
 * @date 16:20 2023/10/1
 */
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {
    //根据skuId+userId查询优惠卷信息
    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long id,
                                          @Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId);

    //根据userId获取用户的全部优惠卷
    List<CouponInfo> selectCartCouponInfoList(@Param("userId") Long userId);
}
