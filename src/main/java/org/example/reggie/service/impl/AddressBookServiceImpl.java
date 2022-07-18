package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.AddressBook;
import org.example.reggie.mapper.AddressBookMapper;
import org.example.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper,AddressBook> implements AddressBookService {
    @Override
    public R<AddressBook> setDefault(AddressBook addressBook) {
        //1，将该id下的所有状态设置为非默认（0）
        LambdaUpdateWrapper<AddressBook> updateWrapper=new LambdaUpdateWrapper<AddressBook>();
        updateWrapper.eq(AddressBook::getUserId, BaseContextUtils.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault,0);
        this.update(updateWrapper);
        //2，将当前地址对象的状态设为默认（1）
        addressBook.setIsDefault(1);
        //3，修改数据库
        this.updateById(addressBook);
        return R.success(addressBook);
    }
}
