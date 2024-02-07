package com.hj.ssyx.home.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author HuangJin
 * @date 22:33 2023/12/30
 */
public interface ItemService{
    // 详情
    Map<String, Object> item(Long id, Long userId);
}
