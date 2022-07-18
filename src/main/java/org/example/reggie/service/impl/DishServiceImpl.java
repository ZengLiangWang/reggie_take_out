package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.CustomException;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.DishFlavor;
import org.example.reggie.mapper.DishMapper;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishFlavorService;
import org.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品的service
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品信息查询与分页
     * @param page：第几页
     * @param pageSize：每页的数据量
     * @param name：搜索菜品的关键字
     * @return
     */
    @Override
    public Page pageWithCategoryName(int page, int pageSize, String name) {
        //1，分页查询dish表，获取基本信息
        Page<Dish> pageInfo=new Page<Dish>(page,pageSize);
        //添加查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<Dish>();
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //查询
        super.page(pageInfo,queryWrapper);
        //2，创建一个新的Page<DishDto>，复制之前page中的信息
        Page<DishDto> dishDtoPage=new Page<DishDto>();
        //对象拷贝(拷贝除了records之外的所有属性)
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //3，查询category表，将分类名称添加到Page 的records中
        //获取之前pageInfo的records
        List<Dish> records=pageInfo.getRecords();
        //给records加上分类名，并赋给dishdtoPage的records
        //创建一个新集合
        List<DishDto> list=records.stream().map((item)->{
            //创建一个DishDto对象，拷贝item的信息
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //获取分类id
            Long categoryId=item.getCategoryId();
            //查询分类信息表
            Category category = categoryService.getById(categoryId);
            //获取分类名
            if (category.getName()!=null){
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        //返回
        return dishDtoPage;
    }

    /**
     * 新增菜品的方法
     * @param dishDto
     */
    //因为要操作两个表，所以开启事务
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //1，先将Dish 保存到 dish表
        super.save(dishDto);
        //2，使用stream流，给dishflavor 赋值id
        //获取dishflavor
        List<DishFlavor> flavors=dishDto.getFlavors();
        //获取id
        Long dishId = dishDto.getId();
        flavors.stream().map((item)->{
            //给集合中每一个item加上id
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //3，将dishFlavor存到 dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }


    /**
     * （批量）删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithFlavors(List<Long> ids) {
        //1，判断商品是否在售
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<Dish>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        //2，如果在售抛出业务异常
        if(count>0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        //3，删除dish表中的数据
        this.removeByIds(ids);
        //4，删除dish_flavor表中的数据
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper =new LambdaQueryWrapper<DishFlavor>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
    }

    /**
     * （批量）修改菜品状态（停售/起售）
     * @param status
     * @param ids
     * @return
     */
    @Override
    public void updateStatus(int status, Long[] ids) {
        //1，遍历数组
        for (int i=0;i<ids.length;i++){
            //2，根据id获取dish对象
            Long id=ids[i];
            Dish dish = this.getById(id);
            //3，修改dish
            if(dish.getStatus()==status){
                throw new CustomException(status==1?"有菜品已经是起售状态":"有菜品已经是停售状态");
            }
            dish.setStatus(status);
            this.updateById(dish);
        }
    }


    /**
     * 根据菜品类型id查询该类型下的所有菜品和菜品口味
     * @param dish
     * @return
     */
    @Override
    public List listWithFlavors(Dish dish) {
        //1，查询Dish表，获取基本的菜品信息
        //条件：分类类别和状态为起售以及排序
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<Dish>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=this.list(queryWrapper);
        //2，使用Stream流，加上口味信息
        List<DishDto> dishDtoList=list.stream().map((item)->{
            //创建一个DishDto对象，将item复制过去，并加上口味信息,然后返回
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<DishFlavor>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> dishFlavor = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavor);
            return dishDto;
        }).collect(Collectors.toList());
        return dishDtoList;
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @Override
    @Transactional
    public void   updateWithFlavor(DishDto dishDto) {
        //1，修改dish表
        this.updateById(dishDto);
        //2，删除原来的dishFlavor信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<DishFlavor>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //3，新增dishFlavor信息
        List<DishFlavor> list=dishDto.getFlavors();
        Long dishId=dishDto.getId();
        list.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);
    }


    /**
     * 根据 id 获取菜品信息和菜品口味（修改页面的数据回显）
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1，查询Dish表，获取菜品信息
        Dish dish=this.getById(id);
        //2，查询DishFlavor表，获取菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<DishFlavor>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //3，将他们封装到一个DishDao 对象中
        DishDto dishDto=new DishDto();
        //复制dish到dishdto中
        BeanUtils.copyProperties(dish,dishDto);
        //将list加入dishdto
        dishDto.setFlavors(list);
        //4，返回
        return dishDto;
    }
}
