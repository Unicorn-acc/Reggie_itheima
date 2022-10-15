package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author MiracloW
 * @date 2022-10-12 11:40
 */
public interface SetmealService extends IService<Setmeal> {

    //新增套餐，保存套餐关联的菜品关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐关联的菜品信息
    public void removeWithDish(List<Long> ids);

    //更新套餐的状态
    public void updateStatusById(List<Long> ids,int status);

    //根据id获取套餐信息
    public SetmealDto getdetailById(Long id);

    //修改套餐信息
    public void updatedetail(SetmealDto setmealDto);
}
