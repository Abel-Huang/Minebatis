package cn.abelib.minebatis.binding;

import cn.abelib.minebatis.session.SqlSession;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-08-19 00:43
 * 优先支持 Mapper方式
 */
public class MapperRegistry {
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = Maps.newHashMap();

    public MapperRegistry() {
    }

    public <T> void addMapper(Class<T> type) {
        // 检测是否为接口
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new RuntimeException();
            }
        }
        knownMappers.put(type, new MapperProxyFactory<>(type));
        // 注解的处理暂时不支持
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        // 如果为空则抛出异常
        if (Objects.isNull(mapperProxyFactory)) {
            throw new RuntimeException();
        }
        return mapperProxyFactory.newInstance(sqlSession);
    }
}
