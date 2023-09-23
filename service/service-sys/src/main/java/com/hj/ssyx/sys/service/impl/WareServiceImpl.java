package com.hj.ssyx.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.sys.Ware;
import com.hj.ssyx.sys.mapper.WareMapper;
import com.hj.ssyx.sys.service.WareService;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 23:44 2023/9/13
 */
@Service
public class WareServiceImpl extends ServiceImpl<WareMapper, Ware> implements WareService {
}
