package org.example.reggie.common;

/**
 * ThreadLocal 工具类（获取修改人id 和 创建人id）
 */
public class BaseContextUtils {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<Long>();

    /**
     * 将 id 放入 ThreadLocal 的方法
     * @param id
     */
    public static void set(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
