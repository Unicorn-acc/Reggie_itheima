package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MiracloW
 * @date 2022-10-12 11:41
 */
@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 1.更新菜品基本信息
        this.updateById(dishDto);

        // 2.清理当前菜品的口味数据--dishflavor的delete操作
        Long id = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getId,id);
        dishFlavorService.remove(queryWrapper);

        // 3.保存提交的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{ // 设置是哪个菜的口味
            item.setDishId(dishDto.getId());
            item.setId(IdWorker.getId());//显示错误：ID已存在
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void delete(List<Long> ids) {
        //查看套餐内是否有当前菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(queryWrapper);

        //没有就直接删除
        if(count == 0){
            for(int i = 0; i < ids.size(); i++){
                Long id = ids.get(i);
                this.removeById(id);
            }
        }else{
            //有就报自定义异常
            throw new CustomException("当前菜品存在套餐中，请检查套餐配置");
        }
    }

    /**
     * 更新菜品状态
     * status=1起售，status=0停售
     * @param status
     * @param ids
     */
    @Override
    public void updatastatus(int status, List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(queryWrapper);
        if(status == 1){
            for(int i = 0; i < ids.size(); i++){
                Long id = ids.get(i);
                Dish dish = this.getById(id);
                dish.setStatus(1);
                this.updateById(dish);
            }
        }else{
            if(count > 0) throw new CustomException("当前有菜品正在套餐中，不能停售");
            else{
                for(int i = 0; i < ids.size(); i++){
                    Long id = ids.get(i);
                    Dish dish = this.getById(id);
                    dish.setStatus(0);
                    this.updateById(dish);
                }
            }
        }
    }

    /**
     * 查询菜品以及对应口味信息
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        // 1.查询菜品基本信息 dish表
        Dish dish = this.getById(id);

        // 2.查询菜品口味信息，dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //拷贝+赋值
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 新增菜品，同时插入菜品对应的口味数据
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息
        this.save(dishDto);

        Long dishid = dishDto.getId();//菜品ID

        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map((item)->{
            item.setDishId(dishid);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dishflavor
        //记得一定要关联dishid
        dishFlavorService.saveBatch(flavors);

    }
}
