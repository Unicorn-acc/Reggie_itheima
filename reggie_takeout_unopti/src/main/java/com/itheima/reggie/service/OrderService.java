package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * @author MiracloW
 * @date 2022-10-14 10:48
 */
public interface OrderService extends IService<Orders> {

    public void submit(Orders orders);
}
