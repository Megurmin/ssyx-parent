package com.hj.ssyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.common.constant.RedisConst;
import com.hj.ssyx.common.exception.SsyxException;
import com.hj.ssyx.common.result.ResultCodeEnum;
import com.hj.ssyx.model.product.SkuAttrValue;
import com.hj.ssyx.model.product.SkuImage;
import com.hj.ssyx.model.product.SkuInfo;
import com.hj.ssyx.model.product.SkuPoster;
import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.mq.service.RabbitService;
import com.hj.ssyx.product.mapper.SkuInfoMapper;
import com.hj.ssyx.product.service.SkuAttrValueService;
import com.hj.ssyx.product.service.SkuImageService;
import com.hj.ssyx.product.service.SkuInfoService;
import com.hj.ssyx.product.service.SkuPosterService;
import com.hj.ssyx.vo.product.SkuInfoQueryVo;
import com.hj.ssyx.vo.product.SkuInfoVo;
import com.hj.ssyx.vo.product.SkuStockLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HuangJin
 * @date 1:15 2023/9/20
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Autowired
    private SkuImageService skuImageService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private SkuPosterService skuPosterService;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public IPage<SkuInfo> selectPageSkuInfo(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        String skuType = skuInfoQueryVo.getSkuType();
        String keyword = skuInfoQueryVo.getKeyword();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(skuType)){
            wrapper.like(SkuInfo::getSkuType, skuType);
        }
        if (!StringUtils.isEmpty(keyword)){
            wrapper.like(SkuInfo::getSkuType, keyword);
        }
        if (!StringUtils.isEmpty(categoryId)){
            wrapper.eq(SkuInfo::getSkuType, categoryId);
        }
        IPage<SkuInfo> pageModel = baseMapper.selectPage(pageParam, wrapper);
        return pageModel;
    }

    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        // 1 添加sku基本信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        baseMapper.insert(skuInfo);

        // 2 保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)) {
            for (SkuPoster skuposter : skuPosterList) {
                skuposter.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }
        // 3 保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)) {
            for (SkuImage skuImage : skuImagesList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImagesList);
        }
        // 4 保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public SkuInfoVo getSkuInfo(Long id) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        // 根据id查询sku基本信息
        SkuInfo skuInfo = baseMapper.selectById(id);
        // 根据id查询商品图片列表
        List<SkuImage> skuImageList = skuImageService.getImageListBySkuId(id);
        // 根据id查询商品海报列表
        List<SkuPoster> skuPosterList = skuPosterService.getPosterListBySkuId(id);
        // 根据id查询商品属性信息列表
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.getAttrValueListBySkuId(id);
        // 分装所有数据，返回
        BeanUtils.copyProperties(skuInfo, skuInfoVo);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return skuInfoVo;
    }

    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        //修改sku基本信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        baseMapper.updateById(skuInfo);

        Long skuId = skuInfoVo.getId();
        //海报信息
        LambdaQueryWrapper<SkuPoster> wrapperSkuPoster = new LambdaQueryWrapper<>();
        wrapperSkuPoster.eq(SkuPoster::getSkuId,skuId);
        skuPosterService.remove(wrapperSkuPoster);

        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)) {
            //遍历，向每个海报对象添加商品skuid
            for (SkuPoster skuPoster:skuPosterList) {
                skuPoster.setSkuId(skuId);
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //商品图片
        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,skuId));
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)) {
            for (SkuImage skuImage:skuImagesList) {
                //设置商品skuid
                skuImage.setSkuId(skuId);
            }
            skuImageService.saveBatch(skuImagesList);
        }

        //商品属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,skuId));
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (SkuAttrValue skuAttrValue:skuAttrValueList) {
                //设置商品skuid
                skuAttrValue.setSkuId(skuId);
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long skuId, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setCheckStatus(status);
        baseMapper.updateById(skuInfo);
    }

    @Override
    public void publish(Long skuId, Integer status) {
        if(status == 1) { //上架
            SkuInfo skuInfo = baseMapper.selectById(skuId);
            skuInfo.setPublishStatus(status);
            baseMapper.updateById(skuInfo);
            //整合mq把数据同步到es里面
            rabbitService.sendMessage(MQConst.EXCHANGE_GOODS_DIRECT,
                    MQConst.ROUTING_GOODS_UPPER,
                    skuId);
        } else { //下架
            SkuInfo skuInfo = baseMapper.selectById(skuId);
            skuInfo.setPublishStatus(status);
            baseMapper.updateById(skuInfo);
            //整合mq把数据同步到es里面
            rabbitService.sendMessage(MQConst.EXCHANGE_GOODS_DIRECT,
                    MQConst.ROUTING_GOODS_LOWER,
                    skuId);
        }
    }

    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setIsNewPerson(status);
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIdList) {
        List<SkuInfo> skuInfoList = baseMapper.selectBatchIds(skuIdList);
        return skuInfoList;
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = baseMapper.selectList(new LambdaQueryWrapper<SkuInfo>().like(SkuInfo::getSkuName,keyword));
        return skuInfoList;
    }

    @Override
    public List<SkuInfo> findNewPersonSkuInfoList() {
        // 条件1：is_new_person=1
        // 条件2：publish_status=1
        // 条件3：显示其中三个
        // 获取第一页数据，每页显示三条记录
        Page<SkuInfo> pageParam = new Page<>(1,3);
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuInfo::getIsNewPerson,1);
        wrapper.eq(SkuInfo::getPublishStatus,1);
        wrapper.orderByDesc(SkuInfo::getStock);
        IPage<SkuInfo> skuInfoIPage = baseMapper.selectPage(pageParam,wrapper);
        List<SkuInfo> skuInfoList = skuInfoIPage.getRecords();
        return skuInfoList;
    }

    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        // skuId查询skuInfo
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        // skuId查询sku图片
        List<SkuImage> imageList = skuImageService.getImageListBySkuId(skuId);
        // skuId查询sku海报
        List<SkuPoster> posterList = skuPosterService.getPosterListBySkuId(skuId);
        // skuId查询sku属性
        List<SkuAttrValue> attrValueList = skuAttrValueService.getAttrValueListBySkuId(skuId);

        // 封装到skuInfoVo对象
        BeanUtils.copyProperties(skuInfo,skuInfoVo);
        skuInfoVo.setSkuImagesList(imageList);
        skuInfoVo.setSkuPosterList(posterList);
        skuInfoVo.setSkuAttrValueList(attrValueList);
        return skuInfoVo;
    }

    // 验证和锁定库存
    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo) {
        //1 判断skuStockLockVoList集合是否为空
        if (CollectionUtils.isEmpty(skuStockLockVoList)) {
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        //2 遍历skuStockLockVoList，验证每个商品的库存并锁定
        skuStockLockVoList.forEach(skuStockLockVo -> {
            this.checkLock(skuStockLockVo);
        });
        //3 只要有一个失败，所有锁定成功的商品都解锁
        boolean flag = skuStockLockVoList.stream()
                .anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock());
        if (flag) {
            // 所有锁定的商品都解锁
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock)
                    .forEach(skuStockLockVo -> {
                        baseMapper.unlockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
                    });
            // 返回失败的状态
            return false;
        }
        //4 如果所有商品都锁定成功，redis缓存相关数据，方便后面解锁和减库存
        redisTemplate.opsForValue()
                .set(RedisConst.SROCK_INFO+orderNo,skuStockLockVoList);
        return true;
    }

    @Override
    public void minusStock(String orderNo) {
        // 从redis获取锁定库存信息
        List<SkuStockLockVo> skuStockLockVoList = (List<SkuStockLockVo>) redisTemplate.opsForValue().get(RedisConst.SROCK_INFO + orderNo);
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            return;
        }
        skuStockLockVoList.forEach(skuStockLockVo -> {
            baseMapper.minusStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
        });
        // 删除redis数据
        redisTemplate.delete(RedisConst.SROCK_INFO+orderNo);
    }

    private void checkLock(SkuStockLockVo skuStockLockVo) {
        // 获取锁，公平锁
        RLock rLock = redissonClient.getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        // 加锁
        rLock.lock();

        try {
            // 验证库存
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
            // 判断
            // 如果没有满足条件的商品，设置为false并返回
            if (skuInfo == null) {
                skuStockLockVo.setIsLock(false);
                return;
            }
            // 有满足条件的商品，锁定库存
            Integer rows = baseMapper.lockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
            if (rows == 1) {
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            // 解锁
            rLock.unlock();
        }
    }
}
