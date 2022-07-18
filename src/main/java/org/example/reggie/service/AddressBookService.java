package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.common.R;
import org.example.reggie.entity.AddressBook;

public interface AddressBookService extends IService<AddressBook> {
    R<AddressBook> setDefault(AddressBook addressBook);
}
