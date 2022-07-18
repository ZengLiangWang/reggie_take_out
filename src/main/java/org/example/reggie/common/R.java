package org.example.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    /**
     * 请求成功时返回调用的方法
     * @param object
     * @param <T>
     * @return
     */
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    /**
     * 请求失败时，调用的方法
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    /**
     * 添加动态数据
     * @param key
     * @param value
     * @return
     */
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
