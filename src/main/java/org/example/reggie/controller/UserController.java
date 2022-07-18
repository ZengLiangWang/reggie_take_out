package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.User;
import org.example.reggie.service.UserService;
import org.example.reggie.utils.SMSUtils;
import org.example.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.PrivateKey;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //获取腾讯云的配置属性
    @Value("${SMS.secretId}")
    private String secretId;

    @Value("${SMS.secretKey}")
    private String secretKey;

    @Value("${SMS.sdkAppId}")
    private String sdkAppId;

    @Value("${SMS.signName}")
    private String signName;

    @Value("${SMS.templateId}")
    private String templateId;

    /**
     * 发送短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //1，获取手机号
        String phone = user.getPhone();
        //判断手机号不为空
        if (StringUtils.isNotEmpty(phone)){
            //2，获取验证码
            /*String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //3，调用腾讯云的短信业务API
            SMSUtils.sendMessage(secretId,secretKey,sdkAppId,signName,templateId,code,phone);*/
            //4，将验证码存到Session中
           /* session.setAttribute(phone,code);*/
            session.setAttribute(phone,"1234");
            return R.success("发送验证码成功");
        }
        return R.error("短信发送失败");
    }


    /**
     * 移动端登录
     * @param map：手机号和验证码
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
       return userService.login(map,session);
    }


    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        //删除session中的id
        request.getSession().removeAttribute("user");
        //返回
        return R.success("退出成功");
    }


    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping("/updateUser")
    public R<String> updateUser(@RequestBody User user){
        //1，添加用户id
        user.setId(BaseContextUtils.getCurrentId());
        //2，修改
        userService.updateById(user);
        //3，返回
        return R.success("修改成功");
    }
}
