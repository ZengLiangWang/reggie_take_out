package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.CustomException;
import org.example.reggie.common.R;
import org.example.reggie.dto.OrdersDto;
import org.example.reggie.entity.*;
import org.example.reggie.mapper.OrdersMapper;
import org.example.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
   //注入需要的Service
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;


    /**
     * 再来一单
     * @param id
     */
    @Override
    public void again(Long id) {
        //1，根据id查询OrderDetail对象列表
        LambdaQueryWrapper<OrderDetail> queryWrapper=new LambdaQueryWrapper<OrderDetail>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        //2，创建购物车对象列表
        //将订单列表中的每一个商品添加到购物车列表
        Long userId = BaseContextUtils.getCurrentId();
        List<ShoppingCart> shoppingCartList=list.stream().map((item)->{
            //创建一个购物车对象，并复制
            ShoppingCart shoppingCart=new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            //判断是菜品还是套餐
            Long dishId= item.getDishId();
            Long setmealId = item.getSetmealId();
            if(dishId != null){
                //添加菜品id
                shoppingCart.setDishId(dishId);
            }
            else {
                //添加套餐id
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //3，将新的购物车列表保存到数据库表
        shoppingCartService.saveBatch(shoppingCartList);
    }


    /**
     * （用户分页查询历史订单）
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page userPage(int page, int pageSize) {
        //创建分页构造器
        Page<Orders> ordersPage=new Page<Orders>(page,pageSize);
        //(最终返回的分页构造器对象)
        Page<OrdersDto> ordersDtoPage=new Page<>(page,pageSize);
        //获取用户id
        Long userId = BaseContextUtils.getCurrentId();
        //根据id查询订单表
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<Orders>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage,queryWrapper);
        //复制page
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        //获取ordersPage的records属性
        List<Orders> records = ordersPage.getRecords();
        //创建ordersDtoPage的records属性
     /*   LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<OrderDetail>();*/
        List<OrdersDto> ordersDtoList=records.stream().map((item)->{
            //创建OrdersDto对象
            OrdersDto ordersDto=new OrdersDto();
            //将之前的信息复制到新的对象中
            BeanUtils.copyProperties(item,ordersDto);
            //根据订单id查询订单明细
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<OrderDetail>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,item.getId());
            List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());
        //将修改后的 ordersDtoList 设置为新的page的records
        ordersDtoPage.setRecords(ordersDtoList);
        return ordersDtoPage;
    }



    /**
     * 用户下单
     * @param order
     */
    @Override
    public void submit(Orders order) {
        //1，获取参数
        //2，获取当前用户id
        Long userId = BaseContextUtils.getCurrentId();
        //3，获取用户信息
        User user=userService.getById(userId);
        //4，获取购物车信息
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<ShoppingCart>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if(shoppingCarts==null || shoppingCarts.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }
        //5，获取地址信息
        Long addressBookId = order.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }
        //6，向订单表中添加数据
        //补充订单信息
        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        order.setId(orderId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2);
        order.setAmount(new BigDecimal(amount.get()));//总金额
        order.setUserId(userId);
        order.setNumber(String.valueOf(orderId));
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(order);
        //7，向订单明细表中添加数据
        orderDetailService.saveBatch(orderDetails);
        //8，删除购物车表
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }
}
