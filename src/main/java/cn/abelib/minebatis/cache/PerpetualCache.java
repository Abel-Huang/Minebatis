package cn.abelib.minebatis.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/7 22:34
 * Cache接口默认实现
 */
public class PerpetualCache implements Cache {
    private String id;
    private Map<String, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(String key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object getObject(String key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(String key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }
}
