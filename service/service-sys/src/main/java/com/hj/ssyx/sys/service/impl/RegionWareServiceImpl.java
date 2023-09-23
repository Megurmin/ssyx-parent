package com.hj.ssyx.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.common.exception.SsyxException;
import com.hj.ssyx.common.result.ResultCodeEnum;
import com.hj.ssyx.model.sys.RegionWare;
import com.hj.ssyx.sys.mapper.RegionWareMapper;
import com.hj.ssyx.sys.service.RegionWareService;
import com.hj.ssyx.vo.sys.RegionWareQueryVo;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 23:05 2023/9/13
 */
@Service
public class RegionWareServiceImpl extends ServiceImpl<RegionWareMapper, RegionWare> implements RegionWareService {
    @Override
    public IPage<RegionWare> selectPageRegionWare(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo) {
        // 1 获取查询条件值
        String keyword = regionWareQueryVo.getKeyword();
        // 2 判断条件是否为空，不为空封装条件
        LambdaQueryWrapper<RegionWare> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null){
            // 封装条件
            // 根据区域名称 或者 仓库名称查询
            wrapper.like(RegionWare::getRegionName, keyword).or().like(RegionWare::getWareName, keyword);
        }
        // 3 调用方法实现条件分页查询
        IPage<RegionWare> regionWarePage = baseMapper.selectPage(pageParam, wrapper);
        // 4 数据返回
        return regionWarePage;
    }

    // 添加开通区域
    @Override
    public void saveRegionWare(RegionWare regionWare) {
        // 判断区域是否已经开通了
        LambdaQueryWrapper<RegionWare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionWare::getRegionId, regionWare.getRegionId());
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) { // 已经存在
            // 抛出异常
            throw new SsyxException(ResultCodeEnum.REGION_OPEN);
        }
        baseMapper.insert(regionWare);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        RegionWare regionWare = baseMapper.selectById(id);
        regionWare.setStatus(status);
        baseMapper.updateById(regionWare);
    }
}
