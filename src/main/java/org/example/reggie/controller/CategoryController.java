package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.entity.Category;
import org.example.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理的Controller
 */
@Slf4j
@RestController
@RequestMapping("category")
public class CategoryController {
    //创建一个Service
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category 新增分类的信息
     * @return
     */
    @PostMapping
    public R<String> Save(@RequestBody Category category){
        //1，调用Service将信息插入数据库
        categoryService.save(category);
        //2，返回成功
        return R.success("新增分类成功");
    }

    /**
     * 分页处理
     * @param page 第几页
     * @param pageSize 每页的长度
     * @return Page对象
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        //创建Page对象（分页构造器）
        Page pageInfo=new Page(page,pageSize);
        //创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<Category>();
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(pageInfo,queryWrapper);
        //返回
        return R.success(pageInfo);
    }


    /**
     *删除分类信息
     * @param ids 分类的id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("id为：{}",ids);
        //调用Service 的方法删除
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 修改分类的方法
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        //调用Service
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }


    /**
     * 根据类型查询分类信息z
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //添加条件（category中的Type）
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<Category>();
        //type不为空
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //查询
        List<Category> list = categoryService.list(queryWrapper);
        //返回
        return R.success(list);
    }

}
