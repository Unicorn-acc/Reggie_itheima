package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

/**
 * @author MiracloW
 * @date 2022-10-12 11:40
 */
public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dishflavor
    public void saveWithFlavor(DishDto dishDto);

    //查询菜品以及对应口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品以及口味信息
    public void updateWithFlavor(DishDto dishDto);

    //删除菜品
    public void delete(List<Long> ids);

    //更新菜品状态
    public void updatastatus(int status,List<Long> ids);
}
