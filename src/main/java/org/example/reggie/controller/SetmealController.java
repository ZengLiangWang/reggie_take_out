package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.CustomException;
import org.example.reggie.common.R;
import org.example.reggie.dto.DishDto;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.entity.SetmealDish;
import org.example.reggie.service.SetmealDishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐的方法
     * @param setmealDto
     * @return
     */
    @PostMapping()
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 分页查询套餐信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        Page setmealPage = setmealService.getSetmealPage(page, pageSize, name);
        return R.success(setmealPage);
    }


    /**
     * （批量）删除套餐
     * @param ids
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * （批量）起售/停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatusById(@PathVariable Integer status,@RequestParam List<Long> ids){
        for (Long id:ids) {
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal.getStatus()==status){
                throw new CustomException(status==1?"有菜品已经是起售状态":"有菜品已经是停售状态");
            }
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("成功修改");
    }


    /**
     * 根据id获得套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmeal = setmealService.getByIdWithDish(id);
        return R.success(setmeal);
    }


    /**
     * 修改套餐的信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }


    /**
     * 返回该分类和起售状态下的所有套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //1，接收参数
        //2，添加查询条件和排序条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //3，查询并返回
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }


    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> SetmealDish(@PathVariable Long id){
        log.info("id:"+id);
        List list = setmealService.setmealDish(id);
        return R.success(list);
    }
}
