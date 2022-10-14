package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author MiracloW
 * @date 2022-10-13 15:59
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * http://localhost:8080/user/sendMsg
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机4位的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用阿里云短信api发送短信
            //SMSUtils.sendMessage("阿里云短信测试","SMS_*********",phone,code);

            //需要将生成的验证码保存起来（session）
            session.setAttribute(phone,code);

            return R.success("短信验证码发送成功");
        }
        return R.error("短信验证码发送失败");
    }

    /**
     * http://localhost:8080/user/login
     * 用户登录
     * {phone: "15221106926", code: "2521"} 使用Map接收
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        // 获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //进行比对，如果比对成功，说明登录成功
        if(codeInSession != null && codeInSession.equals(code)){
            //判断当前手机号对应的用户是否为新用户，如果是则自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //新用户自动完成组测
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            log.info("用户登录成功");
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("短信发送失败");
    }

    /**
     *  http://localhost:8080/user/loginout
     *
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){
        session.removeAttribute("user");
        return R.success("用户退出成功");
    }
}
