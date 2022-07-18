package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.CustomException;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.mapper.CategoryMapper;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 删除分类的方法
     * @param id
     */
    @Override
    public void remove(Long id) {
        //1，根据 分类id 在 Dish表和 Setmeal表中查找是否有关联的菜品或者套餐
        //创建条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<Dish>();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<Setmeal>();
        //查询Dish表
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1=dishService.count(dishLambdaQueryWrapper);
        if(count1>0){
        //说明有关联
        //抛出异常
        throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询Setmeal表
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2=setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //说明有关联
            //抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //2，如果没有则删除
        super.removeById(id);
    }
}
