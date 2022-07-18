package org.example.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie.entity.Employee;

//使用 MybatisPlus，继承 BaseMapper
//操作员工表的Mapper接口
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
