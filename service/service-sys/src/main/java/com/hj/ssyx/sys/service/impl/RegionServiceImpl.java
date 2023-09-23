package com.hj.ssyx.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.sys.Region;
import com.hj.ssyx.sys.mapper.RegionMapper;
import com.hj.ssyx.sys.service.RegionService;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 0:29 2023/9/13
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {
}
