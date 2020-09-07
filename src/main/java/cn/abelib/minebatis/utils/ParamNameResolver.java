package cn.abelib.minebatis.utils;

import cn.abelib.minebatis.io.Resources;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @Author: abel.huang
 * @Date: 2020-08-27 23:42
 *  用于解析Mapper方法参数
 */
public class ParamNameResolver {
   private static final String PARAMETER_CLASS = "java.lang.reflect.Parameter";
   private static Method GET_PARAMS;
   private static Method GET_NAME;

   static {
      try {
         Class<?> paramClass = Resources.classForName(PARAMETER_CLASS);
         GET_PARAMS = Method.class.getMethod("getParameters");
         GET_NAME = paramClass.getMethod("getName");
      } catch (Exception e) {
         // ignore
      }
   }

   private SortedMap<Integer, String> names;

   /**
    * Mybatis中可以使用@Param注解别名，这里省略直接使用反射原名
    * @param method
    */
   public ParamNameResolver(Method method) {
      final SortedMap<Integer, String> map = new TreeMap<>();
      try {
         Object[] params = (Object[]) GET_PARAMS.invoke(method);

         int paramCount = params.length;
         for (int idx = 0; idx < paramCount; idx ++) {
            String name = (String) GET_NAME.invoke(params[idx]);
            map.put(idx, name);
         }
      } catch (Exception e) {
         e.printStackTrace();
         return;
      }
      this.names = Collections.unmodifiableSortedMap(map);
   }
}
