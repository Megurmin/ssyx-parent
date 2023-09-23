package com.hj.ssyx.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.sys.RegionWare;
import com.hj.ssyx.vo.sys.RegionWareQueryVo;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 23:04 2023/9/13
 */
@Service
public interface RegionWareService extends IService<RegionWare> {
    IPage<RegionWare> selectPageRegionWare(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo);

    // 添加开通区域
    void saveRegionWare(RegionWare regionWare);

    void updateStatus(Long id, Integer status);
}
