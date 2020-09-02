package cn.abelib.minebatis.utils;

import cn.abelib.example.StudentMapper;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @Author: abel.huang
 * @Date: 2020-09-02 22:20
 */
public class ParamNameResolverTest {

    @Test
    public void test() throws Exception {
        Method method = StudentMapper.class.getMethod("select", Integer.class, String.class);


        ParamNameResolver resolver = new ParamNameResolver(method);
        Field field = resolver.getClass().getDeclaredField("names");
        field.setAccessible(true);

        Object names = field.get(resolver);
        //System.out.println("names: " + names);

        Type type = method.getAnnotatedReturnType().getType();
        //System.out.println(type);


        Method method1 = StudentMapper.class.getMethod("selectVoid");

        Method method2 = StudentMapper.class.getMethod("selectList");
        Type type1 = method1.getAnnotatedReturnType().getType();
        //System.out.println(type1);

        Type type2 = method2.getAnnotatedReturnType().getType();
        System.out.println(type2);
        if (type2 instanceof Class<?>) {
            System.out.println((Class<?>) type2);
        } else if (type2 instanceof ParameterizedType) {
            System.out.println((Class<?>) ((ParameterizedType) type2).getRawType());
            System.out.println(Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type2).getRawType()));
        } else {
            System.out.println(method2.getReturnType());
        }

    }
}
