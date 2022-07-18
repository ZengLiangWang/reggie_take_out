package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.common.R;
import org.example.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders order);
    Page userPage(int page,int pageSize);
    void again(Long id);
}
