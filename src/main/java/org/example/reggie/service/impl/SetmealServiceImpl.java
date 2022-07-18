package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.CustomException;
import org.example.reggie.dto.DishDto;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.entity.SetmealDish;
import org.example.reggie.mapper.SetmealMapper;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishService;
import org.example.reggie.service.SetmealDishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作套餐的 Service
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;


    /**
     * （批量）删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //1，判断套餐是否在售
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        //2，如果在售，抛出一个异常
        if(count>0){
            throw new CustomException("套餐在售，无法删除");
        }
        //3，删除setmeal表中的数据
        this.removeByIds(ids);
        //4，删除setmeal_dish表中的数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<SetmealDish>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

    }

    /**
     * 移动端点击套餐图片，查看套餐中的菜品信息
     * @param id
     * @return
     */
    @Override
    public List setmealDish(Long id) {
        //1，根据套餐id查询套餐信息表
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<SetmealDish>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        //2，将套餐信息封装到DishDto中
        List<DishDto> dishDtoList=list.stream().map((item)->{
            //将每一个菜品的信息复制到Dto中
            DishDto dishDto=new DishDto();
            dishDto.setCopies(item.getCopies());
            //设置dto中其他需要的信息(Dish的信息)
            //根据id查询Dish
            Dish dish = dishService.getById(item.getDishId());
            BeanUtils.copyProperties(dish,dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        //3，返回
        return dishDtoList;
    }

    /**
     * 修改套餐的信息
     * @param setmealDto
     * @return
     */
    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //1，修改setmeal表
        this.updateById(setmealDto);
        //2，删除原来的setmeal_dish信息
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<SetmealDish>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //3，给新的setmeal_dish信息加上setmeal_id
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        Long setmealDtoId = setmealDto.getId();
        System.out.println("id:"+setmealDtoId);
        System.out.println("list"+list.size());
        list.stream().map((item->{
            item.setSetmealId(setmealDtoId);
            return item;
        })).collect(Collectors.toList());
        //4，保存新的setmeal_dish
        setmealDishService.saveBatch(list);
    }

    /**
     * 根据id获得套餐信息
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        //1，根据id获取setmeal对象的信息
        Setmeal setmeal = this.getById(id);
        //2，根据id获取setmeal_dish的信息
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<SetmealDish>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        //3，创建一个SetmealDto对象，将两个对象的信息复制到其中
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);
        //返回
        return setmealDto;
    }

    @Override
    /**
     * 分页查询套餐信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page getSetmealPage(int page, int pageSize, String name) {
        //1，创建一个菜品信息的分页查询结果
        Page<Setmeal> pageInfo=new Page<Setmeal>(page,pageSize);
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<Setmeal>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        this.page(pageInfo,queryWrapper);
        //2，创建一个SetmealDto的page对象，将之前查询结果除 records之外，复制到新对象中
        Page<SetmealDto> setmealDtoPage=new Page<SetmealDto>();
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        //3，使用Stream流对page对象的records进行处理（加入套餐分类名称）
        //获取之前的pageInfo对象的records，并创建一个list集合接收处理后的集合，作为新page对象的records
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list=records.stream().map((item)->{
            //对集合中每一个元素处理
            //创建一个Setmeal对象，将之前的属性（item）复制到其中，并加上分类名
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //获取分类id
            Long categoryId=item.getCategoryId();
            //获取分类信息
            Category category = categoryService.getById(categoryId);
            //将分类名加入setmealDto对象中
            if(category.getName()!=null){
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        //设置setmealDtoPage的 records
        setmealDtoPage.setRecords(list);
        return setmealDtoPage;
    }

    /**
     * 新增套餐的方法
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //1，保存Setmeal数据
        this.save(setmealDto);
        //2，使用 Stream 流对 Setmeal_dish 表数据进行处理（加上 Setmeal id）
        List<SetmealDish> list=setmealDto.getSetmealDishes();
        list.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //3，保存，并返回
        setmealDishService.saveBatch(list);
    }
}
