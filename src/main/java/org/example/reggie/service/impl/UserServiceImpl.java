package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.R;
import org.example.reggie.entity.User;
import org.example.reggie.mapper.UserMapper;
import org.example.reggie.service.UserService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 移动端登录方法
     * @param map
     * @param session
     * @return
     */
    @Override
    public R<User> login(Map map, HttpSession session) {
        //1，获取手机号
        String phone=map.get("phone").toString();
        //2，获取验证码
        String code=map.get("code").toString();
        //3，比对验证码
        String codeInSession=null;

        try {
            codeInSession = session.getAttribute(phone).toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return R.error("请先获取有效的验证码");
        }
        if(codeInSession!=null && codeInSession.equals(code)){
            //登陆成功
            //判断数据库中是否有该用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<User>();
            queryWrapper.eq(User::getPhone,phone);
            User user = this.getOne(queryWrapper);
            //如果没有，是新用户，创建新用户，将其保存到数据库
            if (user == null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                this.save(user);
            }
            //将userId保存到session
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败");
    }
}
