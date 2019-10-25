package com.dingmk.consistency.listener;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dingmk.consistency.thread.RequestProcessorThreadPool;

/**
 * 系统初始化监听器
 *
 * @Author: wolf
 * @Date: 2019/1/17 10:59
 */
public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // 初始化工作线程池和内存队列
        RequestProcessorThreadPool.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}