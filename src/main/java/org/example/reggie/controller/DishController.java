package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.DishFlavor;
import org.example.reggie.service.DishFlavorService;
import org.example.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新增菜品的功能
     * @param dishDto：dto类（Dish和 DishFlavor 的结合）
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        //调用saveWithFlavor 方法
        dishService.saveWithFlavor(dishDto);
        //返回
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息查询与分页
     * @param page：第几页
     * @param pageSize：每页的数据量
     * @param name：搜索菜品的关键字
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info(""+page);
        log.info(""+pageSize);
        //调用service的pageWithCategoryName方法完成查询与分页
        Page<DishDto> pageInfo= dishService.pageWithCategoryName(page,pageSize,name);
        //返回
        return R.success(pageInfo);
    }

    /**
     * 根据 id 获取菜品信息和菜品口味（修改页面的数据回显）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping()
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品信息成功");
    }

    /**
     * （批量）修改菜品状态（停售/起售）
     * @param status
     * @param ids
     * @return
     */

    public R<String> updateStatusById(@PathVariable int status,Long[] ids){
        dishService.updateStatus(status,ids);
        return R.success("修改成功");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> delete(@RequestParam    List<Long> ids){
        dishService.removeWithFlavors(ids);
        return R.success("删除成功");

    }

    /**
     * 根据菜品类型id查询该类型下的所有菜品
     * @param dish
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //1，添加查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.like(dish.getName()!=null,Dish::getName,dish.getName());
        //2，添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //3，查询
        List<Dish> list = dishService.list(queryWrapper);
        //4，返回
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> list = dishService.listWithFlavors(dish);
        return R.success(list);
    }


}
