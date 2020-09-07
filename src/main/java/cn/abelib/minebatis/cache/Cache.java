package cn.abelib.minebatis.cache;

/**
 * @author abel.huang
 * @date 2020/8/7 22:10
 * Cache接口
 */
public interface Cache {
    String getId();

    void putObject(String key, Object value);

    Object getObject(String key);

    Object removeObject(String key);

    void clear();

    int getSize();
}
