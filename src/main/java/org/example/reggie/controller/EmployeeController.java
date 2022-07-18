package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.Employee;
import org.example.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工管理的Controller
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登陆方法
     * @param employee （将前端登陆栏输入的 用户名，密码封装成一个 Employee 对象）
     * @return R（对应前端 Vue 的一些信息）
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1，使用md5密码加密，将传入的密码加密
        String password=employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //2,根据用户名，查询数据库中的对象
        //创建LambdaQueryWrapper对象（MybatisPlus中的对象）
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<Employee>();
        //添加查询条件
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //查询
        Employee emp = employeeService.getOne(queryWrapper);
        //3，如果没有查询到，则登陆失败
        if (emp==null){
            return R.error("用户名不存在！");
        }
        //4，比对密码是否一致,密码不一致则返回登陆失败
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误！");
        }
        //5，查看员工状态是否是禁用，若为禁用状态，返回登陆失败（0为禁用）
        if (emp.getStatus()==0){
            return R.error("该账号已禁用!");
        }
        //6，将员工id放入 session，返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //删除session中用户id
        request.getSession().removeAttribute("employee");
        //返回R
        return R.success("退出成功");
    }

    /**
     * 新增员工的方法
     * @param request
     * @param employee
     * @return
     */
    @PostMapping()
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        //1，接收到请求页面发来的关于员工信息的JSON对象
        //2，将对象其他信息补充：默认初始密码：123456，创建人，修改人，创建时间，最后一次修改时间
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
       /* Long empId=(Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());*/

        //3，将该对象插入数据库
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页方法
     * @return Page对象（由MbatisPlus提供），里面有data属性
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页查询
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        //返回Page对象
        return R.success(pageInfo);
    }

    /**
     * 按照 id 修改员工信息
     * @param employee 进行修改的员工信息
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //1，获取参数
        log.info(employee.toString());
       /* //2，设置要修改的一些参数（最后的修改者，最后修改时间）
        Long empId=(Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/
        //3，调用Service 操作数据库
        employeeService.updateById(employee);
        //4，返回
        return R.success("员工信息修改成功");
    }

    /**
     * 根据 id 获取对象（数据回显）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        //1,根据id查询对象
        Employee employee = employeeService.getById(id);
        //2，如果查询成功，将对象返回
        if(employee!=null){
            return R.success(employee);
        }
        //3，如果没有查询到对应的对象，则返回失败
        return R.error("没有查询到对应的员工信息");
    }

}
