package cn.abelib.minebatis.utils;

import cn.abelib.minebatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author abel.huang
 * @date 2020-08-27 23:42
 * 用于解析 Mapper 方法参数
 */
public class ParamNameResolver {

    private static final Logger log = LoggerFactory.getLogger(ParamNameResolver.class);

    private static final String PARAMETER_CLASS = "java.lang.reflect.Parameter";
    private static Method GET_PARAMS;
    private static Method GET_NAME;

    static {
        try {
            Class<?> paramClass = Resources.classForName(PARAMETER_CLASS);
            GET_PARAMS = Method.class.getMethod("getParameters");
            GET_NAME = paramClass.getMethod("getName");
        } catch (Exception e) {
            log.warn("Failed to initialize parameter reflection, parameter names may not be available", e);
        }
    }

    /**
     * 参数位置 -> 参数名
     */
    private final SortedMap<Integer, String> names;

    /**
     * 参数个数
     */
    private final int paramCount;

    /**
     * 解析 Mapper 方法的参数名
     * @param method Mapper 方法
     */
    public ParamNameResolver(Method method) {
        final SortedMap<Integer, String> map = new TreeMap<>();
        int count = 0;
        try {
            Object[] params = (Object[]) GET_PARAMS.invoke(method);
            count = params.length;
            for (int idx = 0; idx < count; idx++) {
                String name = (String) GET_NAME.invoke(params[idx]);
                map.put(idx, name);
            }
        } catch (Exception e) {
            log.warn("Failed to resolve parameter names for method: {}", method.getName(), e);
        }
        this.names = Collections.unmodifiableSortedMap(map);
        this.paramCount = count;
    }

    /**
     * 获取参数名映射
     */
    public SortedMap<Integer, String> getNames() {
        return names;
    }

    /**
     * 获取参数个数
     */
    public int getParamCount() {
        return paramCount;
    }

    /**
     * 将方法参数数组转换为 SQL 参数对象
     * - 单参数：直接返回参数值
     * - 多参数：返回 Map<参数名, 参数值>
     *
     * @param args 方法参数值数组
     * @return SQL 参数对象
     */
    public Object convertArgsToSqlCommandParam(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // 单参数直接返回
        if (paramCount == 1) {
            return args[0];
        }

        // 多参数转换为 Map
        Map<String, Object> param = new HashMap<>();
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            int index = entry.getKey();
            String name = entry.getValue();
            if (index < args.length) {
                param.put(name, args[index]);
                // 同时支持 param1, param2 的形式
                param.put("param" + (index + 1), args[index]);
            }
        }
        return param;
    }
}
