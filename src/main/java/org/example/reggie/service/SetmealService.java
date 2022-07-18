package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    Page getSetmealPage(int page,int pageSize,String name);
    void removeWithDish(List<Long> ids);
    SetmealDto getByIdWithDish(Long id);
    void updateWithDish(SetmealDto setmealDto);
    List setmealDish(Long id);
}
