package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MiracloW
 * @date 2022-10-12 20:32
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody  DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * http://localhost:8080/dish/page?page=1&pageSize=10&name=11
     * 拓展内容：
     * 1、查询菜品对应的分类
     * 2、下载菜品对应的图片
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //1.先查询所有菜品信息
        //2.将Page<Dish>改造为Page<DishDto>（因为还需要一个菜品的类别信息）
        //  2.1 在针对查询的每个菜品信息都去执行categoryservice.getbyid，得到类别名，然后添加上去，最后收集成为一个list
        //  2.2 Dishdto中有categoryName
        //3.最后返回Page<DishDto>
        Page<Dish> pageinfo = new Page<>(page,pageSize);
        //有菜品类别信息，符合要求
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器和添加过滤条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageinfo,queryWrapper);

        //原来的pageInfoRecords没有菜品分类属性，所以把原来的records取出来，扩展一下，做新的dishDtoPageRecords

        //对象拷贝(排除records)
        BeanUtils.copyProperties(pageinfo,dishDtoPage,"records");

        List<Dish> records = pageinfo.getRecords();


        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //拷贝item中普通的Dish属性
            BeanUtils.copyProperties(item,dishDto);

            //查询该对象中的分类类别
            Long categoryId = item.getCategoryId();//分类Id
            //根据ID查询对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);


        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody  DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

//        //方法1：清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish*");//所有以dish开头的缓存数据
//        redisTemplate.delete(keys);
//        log.info("菜品更新，清理所有dish缓存数据");
        //方法2：只清理具体更新到的分类的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        log.info("菜品更新，清理dish:{}的缓存数据",dishDto.getCategoryId().toString());

        return R.success("更新成功");
    }

    /**
     * 根据条件查询菜品数据（在添加套餐中）
     * 修改：在用户端除了展示菜品信息还要展示口味数据
     * http://localhost:8080/dish/list?categoryId=1580031292453130242
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        log.info(dish.toString());
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        //查询状态为1（起售）的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;

        //将分类id和状态status构造一下(作为key来取)
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果存在，直接返回，无需查询数据库
        if(dishDtoList != null){
            log.info("菜品list已经缓存在Redis中");
            return R.success(dishDtoList);
        }

        //------------------------------------------------------------------------------------
        log.info("菜品list未缓存在Redis中，继续查询");

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //查询状态为1（起售）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        List<Dish> list = dishService.list(queryWrapper);

        //根据每个菜品查询口味数据
        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //拷贝item中普通的Dish属性
            BeanUtils.copyProperties(item,dishDto);

            //查询该对象中的分类类别
            Long categoryId = item.getCategoryId();//分类Id
            //根据ID查询对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的ID
            Long dishid = item.getId();
            //根据菜品id查询dishflavor表中口味信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishid);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        //------------------------------------------------------
        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis,设置过期时间60分钟
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    /**
     * http://localhost:8080/dish?ids=1580798219748343810
     * 删除菜品
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        log.info("deleteids:{}",ids);
        dishService.delete(ids);

        Set keys = redisTemplate.keys("dish*");//所有以dish开头的缓存数据
        redisTemplate.delete(keys);
        log.info("菜品更新，清理所有dish缓存数据");
        return R.success("删除菜品成功！");
    }

    /**
     * http://localhost:8080/dish/status/1?ids=1580196307025920001
     * 停售菜品 status=1起售，status=0停售
     */
    @PostMapping("/status/{status}")
    public R<String> updatestatus(@PathVariable("status") int status,@RequestParam("ids") List<Long> ids){
        log.info("updatestatus:{}",ids.toString());
        dishService.updatastatus(status,ids);

        Set keys = redisTemplate.keys("dish*");//所有以dish开头的缓存数据
        redisTemplate.delete(keys);
        log.info("菜品更新，清理所有dish缓存数据");
        return R.success("更改成功");
    }

}
