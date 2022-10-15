package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MiracloW
 * @date 2022-10-14 10:48
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
