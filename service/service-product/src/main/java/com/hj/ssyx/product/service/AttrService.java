package com.hj.ssyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.product.Attr;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:55 2023/9/19
 */
public interface AttrService extends IService<Attr> {
    List<Attr> getAttrListByGroupId(Long groupId);
}
