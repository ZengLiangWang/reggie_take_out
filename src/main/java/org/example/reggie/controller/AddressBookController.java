package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sun.prism.impl.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.AddressBook;
import org.example.reggie.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController{
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        //1. 接收参数
        log.info("addressBook：{}",addressBook);
        // 2. 给传过来的地址对象加上当前登录用户的 ID
        addressBook.setUserId(BaseContextUtils.getCurrentId());
        //3. 保存到数据库
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设为默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        R<AddressBook> addressBookR = addressBookService.setDefault(addressBook);
        return addressBookR;
    }

    /**
     * 查询该 id 下的所有地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        //1，设置userId
        addressBook.setUserId(BaseContextUtils.getCurrentId());
        log.info("addressBook: {}",addressBook);
        //2，添加查询条件和排序条件
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<AddressBook>();
        queryWrapper.eq(addressBook.getUserId()!=null,AddressBook::getUserId,addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        //3，查询
        List<AddressBook> list = addressBookService.list(queryWrapper);
        //4，返回
        return R.success(list);
    }

    /**
     * 根据 id 查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id){
        //1，根据id查询
        AddressBook addressBook=addressBookService.getById(id);
        //2，判断是否能查询到,并返回
        if(addressBook!=null){
            return R.success(addressBook);
        }
        return R.error("没有找到该地址");
    }


    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        //1，设置查询条件（用户id 和 默认状态）
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<AddressBook>();
        queryWrapper.eq(AddressBook::getUserId,BaseContextUtils.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook=addressBookService.getOne(queryWrapper);
        //2，判断能否查询到，并返回
        if (addressBook!=null){
            return R.success(addressBook);
        }
        return R.error("没有找到该地址");
    }


    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> delete(Long ids){
        //1，判断参数是否为空
        if (ids==null){
            return R.error("请求异常");
        }
        //2，不为空则直接删除
        addressBookService.removeById(ids);
        return R.success("删除地址成功");
    }


    /**
     * 修改地址信息
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        //1，判断参数不为空
        if (addressBook==null){
            return R.error("请求异常");
        }
        //2，不为空则直接修改
        addressBookService.updateById(addressBook);
        return R.success("修改地址成功");
    }
}
