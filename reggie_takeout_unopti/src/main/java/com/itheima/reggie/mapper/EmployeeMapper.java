package com.itheima.reggie.mapper;

import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author MiracloW
 * @date 2022-10-09 16:01
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{

}
