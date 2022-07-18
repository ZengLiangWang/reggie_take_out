package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.reggie.common.R;
import org.example.reggie.dto.OrdersDto;
import org.example.reggie.entity.Orders;
import org.example.reggie.service.OrderDetailService;
import org.example.reggie.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单的方法
     * @param order
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders order){
        ordersService.submit(order);
        return R.success("下单成功");
    }


    /**
     * （用户）分页查询历史订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        Page<OrdersDto> userPage = ordersService.userPage(page, pageSize);
        return R.success(userPage);
    }


    /**
     * 后台分页（查询）查看订单信息
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number,String beginTime,String endTime){
        //创建分页构造器
        Page<Orders> pageInfo=new Page<Orders>(page,pageSize);
        //创建查询条件
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<Orders>();
        //重点：使用动态sql(gt(大于)，lt(小于))
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //查询
        ordersService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /**
     * 后台修改订单的状态
     * @param map
     * @return
     */
    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){
        //1，接收参数
        String id=map.get("id");
        Long orderId=Long.parseLong(id);
        Integer status=Integer.parseInt(map.get("status"));
        if(orderId==null || status==null){
            return R.error("传入信息不合法");
        }
        //2，根据id查询订单
        Orders oeders=ordersService.getById(orderId);
        //3，修改订单状态
        oeders.setStatus(status);
        ordersService.updateById(oeders);
        //4，返回
        return R.success("订单状态修改成功");
    }


    /**
     * 移动端再来一单
     * @param map
     * @return
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,Long> map){
        Long id=map.get("id");
        log.info("id="+id);
        ordersService.again(id);
       return R.success("再来一单成功");
    }
}
