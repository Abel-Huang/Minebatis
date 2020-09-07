package cn.abelib.minebatis.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author abel.huang
 * @date 2020/8/7 22:14
 * 默认缓存大小为1024，可以设置
 * 一个比较不错的LRU实现参考
 */
public class LruCache implements Cache {
    private Cache delegate;
    private Map<String, Object> keyMap;
    private String eldestKey;

    public LruCache(Cache delegate) {
        this.delegate = delegate;
        setSize(1024);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    /**
     * 添加缓存，并且清除旧数据缓存
     * @param key
     * @param value
     */
    @Override
    public void putObject(String key, Object value) {
        delegate.putObject(key, value);
        cycleKeyList(key);
    }

    @Override
    public Object getObject(String key) {
        //访问会修改LinkedHashMap中的记录顺序
        keyMap.get(key);
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(String key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(final int size) {
        keyMap = new LinkedHashMap<String, Object>(size, .75F, true) {
            private static final long serialVersionUID = 4267176411845948333L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }

    private void cycleKeyList(String key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }
}
