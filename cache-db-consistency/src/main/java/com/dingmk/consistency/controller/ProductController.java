package com.dingmk.consistency.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.dingmk.consistency.model.ProductInventory;
import com.dingmk.consistency.request.ProductInventoryCacheRefreshRequest;
import com.dingmk.consistency.request.ProductInventoryDBUpdateRequest;
import com.dingmk.consistency.request.Request;
import com.dingmk.consistency.service.ProductInventoryService;
import com.dingmk.consistency.service.RequestAsyncProcessService;
import com.dingmk.consistency.utils.Result;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class ProductController {

	@Autowired
    private RequestAsyncProcessService requestAsyncProcessService;

    @Autowired
    private ProductInventoryService productInventoryService;

    /**
     * 商品库存更新请求
     * @param productInventory
     * @return
     */
    @PostMapping("/updateProductInventory")
    public Result<String> updateProductInventory(ProductInventory productInventory) {
        try {
            Request request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);
            // 将商品库存更新请求加入请求异步执行：路由到对应的内存队列中
            requestAsyncProcessService.process(request);

            log.info("### productInventory db update join queue success productInventory={}###", JSON.toJSON(productInventory));
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
    }

    /**
     * 查询商品库存数据
     * @param productId
     * @return
     */
    @GetMapping("getProductInventory")
    public Result getProductInventory(String productId) {
        ProductInventory productInventory = null;
        try {
            Request request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, false);
            // 将商品库存缓存刷新请求加入异步执行：路由到对应的内存队列
            requestAsyncProcessService.process(request);

            // 将请求丢给异步去处理以后，就需要while(true)一会儿，在这里hang住
            // 去尝试等待前面有商品库存更新请求，如果有更新操作这里将刷新缓存,将最新的数据刷新到缓存中
            Instant start = Instant.now();
            long waitTime = 0l;
            while (true) {
                // 等待超过200ms没有从缓存中获取到结果
                if (waitTime > 200) {
                    log.info("### try get cache timeout than of 200 ms. goto db ###");
                    break;
                }

                // 尝试去redis中读取一次商品缓存数据
                productInventory = productInventoryService.getProductInventoryCache(productId);

                // 如果读取到了结果，就返回
                if (productInventory != null) {
                    return new Result(0, "success", productInventory);
                }
                // 如果没有读取到结果，那么就等待一段时间
                else {
                    Thread.sleep(20);
                    Instant end = Instant.now();
                    waitTime = Duration.between(start, end).toMillis();
                    log.info("### wait 20 ms. and retry get cache ... ###");
                }
            }

            // 代码执行到这里情况：
            // 1. 上一次也是读请求（刷新缓存），数据刷入redis，但redis的LRU算法给清除掉了，标志位依然为false
            // 此时如果下一个读请求过来就直接被过滤掉，进而到这里执行
            // 2. 可能代码在这里夯住200ms，也就是说读请求（刷新缓存）一致在内存队列积压着，没有等到它的执行（这种情况就要考虑redis
            // 进行升级优化了）
            // 3. 数据库里本身就没有,这里就设计到缓存穿透

            // 直接尝试去数据库获取库存数据
            productInventory = productInventoryService.findProductInventory(productId);
            if (productInventory != null) {
                log.info("### find db success ###");
                // 将刷新缓存请求加入到内存队列中: 强制执行刷新缓存请求
                request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, true);
                requestAsyncProcessService.process(request);

                return new Result(0, "success", productInventory);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Result(0, "request timeout", new ProductInventory(productId, -1l));
    }
}
