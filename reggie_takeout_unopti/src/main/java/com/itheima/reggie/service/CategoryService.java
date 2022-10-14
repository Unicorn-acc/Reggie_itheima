package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @author MiracloW
 * @date 2022-10-12 10:37
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
