package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MiracloW
 * @date 2022-10-13 15:57
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
