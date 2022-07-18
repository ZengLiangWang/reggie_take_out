package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.ShoppingCart;
import org.example.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 向购物车中添加商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody  ShoppingCart shoppingCart){
        shoppingCart = shoppingCartService.add(shoppingCart);
        return R.success(shoppingCart);
    }

    /**
     * 展示购物车中的商品
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        //1，查询该id下的购物车商品并添加排序条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContextUtils.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        //2，返回
        return R.success(list);
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //1，删除所有购物车表中的所有当前userId的商品
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContextUtils.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        //2，返回
        return R.success("清空购物车成功");
    }


    /**
     * 从购物车中减少商品数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        shoppingCartService.sub(shoppingCart);
        return R.success("修改成功");
    }

}
