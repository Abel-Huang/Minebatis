package cn.abelib.minebatis.binding;


import cn.abelib.minebatis.Configuration;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.todo.MappedStatement;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-08-20 00:43
 *  todo
 */
public class MapperMethod {
    private final SqlCommand command;
    private final MethodSignature signature;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.signature = new MethodSignature(configuration, mapperInterface, method);
    }

    /**
     * todo
     * @param sqlSession
     * @param args
     * @return
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        MappedStatement ms = null;
        switch (command.getType()) {
            case "INSERT":

                break;
            case "UPDATE":
                break;

            case "SELECT":
                result = executeForMany(sqlSession, args);
                break;
            default:
                throw new RuntimeException("....");
        }
        result = null;
       return result;
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = signature.convertArgsToSqlCommandParam(args);



        result = sqlSession.selectList(command.getName(), param);

        return result;
    }

    /**
     * name SQL语句的名称
     * type SQL语句的类型，INSERT，UPDATE，DELETE，SELECT
     */
    public static class SqlCommand {
        private final String name;
        private final String type;
        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = null;
            if (configuration.hasStatement(statementName)) {
                ms = configuration.getMappedStatement(statementName);
            } else if (!mapperInterface.equals(method.getDeclaringClass())) {
                String parentStatementName = method.getDeclaringClass().getName() + "." + method.getName();
                if (configuration.hasStatement(parentStatementName)) {
                    ms = configuration.getMappedStatement(parentStatementName);
                }
            }
            if (Objects.isNull(ms)) {
                throw new RuntimeException();
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (Objects.isNull(type)) {
                    throw new RuntimeException();
                }
            }
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }
    }

    /**
     * returnsMany返回值是否是集合或者数组
     * returnsMap返回值是否是Map
     * returnsVoid返回值是否为空
     * returnType返回值类型
     */
    public static class MethodSignature {
        private final boolean returnsMany;
        private final boolean returnsMap;
        private final boolean returnsVoid;
        private final Class<?> returnsType;

        private final String mapKey;
        private final Integer resultHandlerIndex;
        private final Integer rowBoundIndex;

        /**
         * todo
         * @param configuration
         * @param mapperInterface
         * @param method
         */
        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
            this.returnsMany = false;
            this.returnsMap = false;
            this.returnsVoid = false;
            this.returnsType = null;

            this.mapKey = "";
            this.rowBoundIndex = 0;
            this.resultHandlerIndex = 0;

        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            return null;
        }
    }
}
