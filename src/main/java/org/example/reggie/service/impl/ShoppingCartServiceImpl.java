package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.entity.ShoppingCart;
import org.example.reggie.mapper.ShoppingCartMapper;
import org.example.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService{

    /**
     * 从购物车中减少商品数量
     * @param shoppingCart
     * @return
     */
    @Override
    public void sub(ShoppingCart shoppingCart) {
        //1. 接收参数
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContextUtils.getCurrentId());
        //2. 判断是菜品还是套餐
        if (dishId != null){
            //是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else {
            //是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //3. 查询当前id下的商品
        ShoppingCart commodity = this.getOne(queryWrapper);
        Integer number = commodity.getNumber();
        if(number>1){
            //4. 如果number>1，number-1
            commodity.setNumber(number-1);
            this.updateById(commodity);
        }
        else {
            //5. 如果number<=1，删除该商品
            this.removeById(commodity);
        }

    }

    /**
     * 向购物车中添加商品
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {
        //1，接收参数
        //2，添加userId
        shoppingCart.setUserId(BaseContextUtils.getCurrentId());
        //3，判断购物车中是否已经有该商品
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        //判断当前商品是套餐还是菜品
        Long dishId = shoppingCart.getDishId();
        if(dishId!=null){
            //说明是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else {
            //说明是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询是否有该商品
        ShoppingCart commodity = this.getOne(queryWrapper);
        if (commodity!=null){
            //4，如果有该商品，数量加1
            commodity.setNumber(commodity.getNumber()+1);
            this.updateById(commodity);
        }
        else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
            commodity=shoppingCart;
            //5，如果没有该商品，加入购物车数量为1
        }
        //6，返回
        return commodity;
    }
}
