package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author MiracloW
 * @date 2022-10-09 16:05
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //http://localhost:8080/employee/login
    //请求有两个参数：json格式，username和password，用@RequestBody接收
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.分析逻辑 2.编写代码

        //1、将页面提交的密码password进行md5加密处理
        //2、根据页面提交的用户名username查询数据库
        //3、如果没有查询到则返回登录失败结果
        //4、密码比对，如果不一致则返回登录失败结果
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        //6、登录成功，将员工id存入Session并返回登录成功结果

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名 Employee
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //Employee::getUsername：创建一个Employee对象并调用其getUsername方法
        queryWrapper.eq(Employee::getUsername,employee.getUsername());//等值查询的条件
        Employee emp = employeeService.getOne(queryWrapper);


        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果;0禁用
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //1.清session中保存的当前登录员工的id 2.返回结果
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("新增员工信息：{}",employee.toString());
        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获得当前登录的用户ID，设置创建人 更新人
//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        //这样当新增的用户存在时，会有错误，解决方法：
        //1.trycatch 2.全局异常捕获
        employeeService.save(employee);
        log.info("新增员工成功");
        return R.success("新增员工成功");
    }

    //页面需要records,total等，所以泛型处用mp提供的page
    /**
     * 员工信息分页查询（基于mp提供的分页框架）
     * http://localhost:8080/employee/page?page=1&pageSize=10&name=1
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器(Employee)
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        //添加过滤条件 like模糊查询
        //当name不为空时，才会添加这个like(name)条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName,name);
        //添加排序条件，按更新时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);//提供分页构造器，条件构造器，mp中会自己执行查询
        /**
         * 1. SELECT COUNT(*) FROM employee
         * 2. SELECT id,username,name,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user
         *                                                      FROM employee ORDER BY update_time DESC LIMIT ?
         */

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * 存在精度丢失问题，原因是因为id超过16了，前端丢弃了后面两位
     * 解决方法：配置消息转换器（在服务端给页面响应json数据时进行处理，将long型数据统一转为String字符串）
     * 1、使用对象转换器JacksonObjectMapper,基于Jackson进行Java对象到json数据的转换（以提供）
     * 2、在WebMvcConfig配置类中拓展MVC的消息转换器，使用提供的对象转换器进行Java对象到Json数据的转换
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info("线程id:{}", Thread.currentThread().getId());
        log.info(employee.toString());
        //新增：看看尝试禁用的是否是admin超级管理员
        Long id = employee.getId();
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //Employee::getUsername：创建一个Employee对象并调用其getUsername方法
        queryWrapper.eq(Employee::getId,id);//等值查询的条件
        Employee emp = employeeService.getOne(queryWrapper);
        if(emp.getUsername().equals("admin")) return R.error("不能修改admin");

//        employee.setUpdateUser((Long)request.getSession().getAttribute("employee"));
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }


    //http://localhost:8080/employee/1579794004229910530
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据ID查询员工信息..");
        Employee employee = employeeService.getById(id);
        if(employee == null) return R.error("没有查询到相关员工信息。");
        else    return R.success(employee);
    }
}
