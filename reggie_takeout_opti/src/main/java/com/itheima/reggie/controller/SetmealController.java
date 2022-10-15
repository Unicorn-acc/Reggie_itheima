package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理
 * @author MiracloW
 * @date 2022-10-12 23:48
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * http://localhost:8080/setmeal/page?page=1&pageSize=10&name=11
     * 套餐分页查询
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageinfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);//更新时间降序排序
        setmealService.page(pageinfo,queryWrapper);

        //-----------------------------------------------------------------------
        //进行改造：返回值要套餐分类==>SetmealDto里有categoryName
        Page<SetmealDto> dtopageinfo = new Page<>();

        //拷贝一下（排除records，因为这是Page提供的列表数据，我们要进行改造。添加分类信息，再set）
        BeanUtils.copyProperties(pageinfo,dtopageinfo,"records");
        //将List<Setmeal>改造成List<SetmealDto>对象
        List<Setmeal> records = pageinfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            //先将Setmeal每个实体拷贝到SetmealDto中,再设置分类名称
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类的name
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                //set分类名称
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtopageinfo.setRecords(list);

        return R.success(dtopageinfo);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        log.info(ids.toString());
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 更改套餐状态
     * @param status 停售0 起售1
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updatestatus(@PathVariable("status") int status,@RequestParam("ids") List<Long> ids){
        log.info(status+" "+ids.toString());
        setmealService.updateStatusById(ids,status);
        return R.success("状态更改成功");
    }

    /*
        查看套餐的菜品信息
     */
//    @GetMapping("/list")
//    public R<List<Setmeal>> list(Setmeal setmeal){
//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId,setmeal.getCategoryId());
//        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
//        queryWrapper.eq(Setmeal::getStatus,1);
//        List<Setmeal> list = setmealService.list(queryWrapper);
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        queryWrapper.eq(Setmeal::getStatus,1);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        List<SetmealDto> list = setmealList.stream().map((item) -> {
            //先将Setmeal每个实体拷贝到SetmealDto中,再设置分类名称
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类的name
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                //set分类名称
                setmealDto.setCategoryName(categoryName);
            }
            Long id = item.getId();
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
            lambdaQueryWrapper.orderByDesc(SetmealDish::getUpdateTime);
            List<SetmealDish> dishList = setmealDishService.list(lambdaQueryWrapper);
            setmealDto.setSetmealDishes(dishList);

            return setmealDto;
        }).collect(Collectors.toList());

        return R.success(list);
    }

    /**
     * http://localhost:8080/setmeal/1415580119015145474
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getdetail(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getdetailById(id);
        return  R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto:{}",setmealDto.toString());
        setmealService.updatedetail(setmealDto);
        return R.success("修改套餐信息成功");
    }

}
