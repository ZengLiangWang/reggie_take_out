package org.example.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie.entity.Category;

//操作分类表的Mapper接口
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
