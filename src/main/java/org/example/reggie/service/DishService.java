package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
        void saveWithFlavor(DishDto dishDto);
        Page pageWithCategoryName(int page,int pageSize,String name);
        DishDto getByIdWithFlavor(Long id);
        void updateWithFlavor(DishDto dishDto);
        void updateStatus(int status,Long[] ids);
        void removeWithFlavors(List<Long> ids);
        List listWithFlavors(Dish dish);
}
