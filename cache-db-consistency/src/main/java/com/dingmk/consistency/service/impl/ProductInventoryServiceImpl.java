package com.dingmk.consistency.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import com.dingmk.consistency.mapper.ProductInventoryMapper;
import com.dingmk.consistency.model.ProductInventory;
import com.dingmk.consistency.service.ProductInventoryService;
import com.dingmk.consistency.service.RedisService;

/**
 * 商品库存Service实现类
 */
@Service
public class ProductInventoryServiceImpl implements ProductInventoryService {

    @Autowired
    private ProductInventoryMapper productInventoryMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 修改商品库存数据库
     * @param productInventory
     */
    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
    }

    /**
     * 清除商品库存缓存
     * @param productInventory
     */
    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisService.del(key);
    }

    /**
     * 根据商品id查询库存信息
     * @param productId
     * @return
     */
    @Override
    public ProductInventory findProductInventory(String productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    /**
     * 获取商品库存缓存数据
     * @param productId
     * @return
     */
    @Override
    public ProductInventory getProductInventoryCache(String productId) {
        long inventoryCnt = 0l;
        String key = "product:inventory:" + productId;
        String value = redisService.get(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                inventoryCnt = Long.valueOf(value);
                return new ProductInventory(key, inventoryCnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将商品库存添加到缓存中
     * @param productInventory
     */
    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisService.set(key, productInventory.getInventoryCnt() + "");
    }
}