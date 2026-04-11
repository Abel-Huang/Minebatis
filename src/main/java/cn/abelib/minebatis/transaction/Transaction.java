package cn.abelib.minebatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 事务接口
 */
public interface Transaction {

    /**
     * 获取数据库连接
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     */
    void commit() throws SQLException;

    /**
     * 回滚事务
     */
    void rollback() throws SQLException;

    /**
     * 关闭连接
     */
    void close() throws SQLException;
}
