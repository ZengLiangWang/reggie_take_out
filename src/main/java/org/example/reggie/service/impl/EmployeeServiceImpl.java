package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.entity.Employee;
import org.example.reggie.mapper.EmployeeMapper;
import org.example.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

//因为使用了MybatisPlus，所以需要继承 ServiceImpl
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
