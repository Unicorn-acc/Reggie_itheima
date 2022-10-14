package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author MiracloW
 * @date 2022-10-12 10:38
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * http://localhost:8080/category/page?page=1&pageSize=10
     * 分页管理
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") int page,@RequestParam("pageSize") int pagesize){
        Page<Category> pageinfo = new Page<>(page,pagesize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件，需要按照sort排序
        queryWrapper.orderByAsc(Category::getSort);//根据sort字段升序排序

        //分页查询
        categoryService.page(pageinfo,queryWrapper);
        return R.success(pageinfo);
    }

    /**
     * 根据ID 删除分类（要先检查当前分类是否关联了菜品或者套餐）
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类：{}",id);
        //categoryService.removeById(id);
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }

    /**
     * http://localhost:8080/category
     * 根据ID修改信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody  Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);

        return R.success("修改成功");
    }

    /**
     * http://localhost:8080/category/list?type=1
     * 菜品管理-添加菜品-菜品分类
     * 查询菜品分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getType).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
