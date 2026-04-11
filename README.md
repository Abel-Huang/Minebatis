# Minebatis

一个轻量级的 MyBatis 框架实现，用于学习 MyBatis 核心原理。实现了完整的 CRUD 操作、事务支持、一级/二级缓存、参数处理、结果集映射等核心功能。

## 功能特性

- **完整 CRUD**: 支持 SELECT、INSERT、UPDATE、DELETE 操作
- **Mapper 代理**: JDK 动态代理实现 Mapper 接口绑定
- **多参数支持**: 支持多参数 Mapper 方法，自动转换为 Map
- **参数类型转换**: 支持 Date、BigDecimal、Boolean 等多种类型自动转换
- **结果集映射**: 自动映射、下划线转驼峰、类型自动转换
- **事务支持**: commit/rollback 事务管理，支持 autoCommit 配置
- **一级缓存**: Session 级别缓存，提升查询性能
- **二级缓存**: 跨 Session 的 namespace 级别缓存，事务安全
- **LRU 缓存**: 支持最近最少使用淘汰策略
- **数据源接口**: 支持注入第三方连接池（HikariCP、Druid 等）
- **日志框架**: SLF4J + Logback，完善的日志记录
- **配置解析**: dom4j 解析 MyBatis 配置文件

## 项目架构

```
cn.abelib.minebatis
├── binding                     # Mapper 接口绑定
│   ├── MapperProxy             # 动态代理 InvocationHandler
│   ├── MapperProxyFactory      # 代理工厂
│   ├── MapperRegistry          # Mapper 注册中心
│   ├── MapperMethod            # SQL 方法执行器
│   └── BindingException        # 绑定异常
│
├── builder                     # XML 解析构建器
│   ├── XMLConfigBuilder        # 主配置文件解析
│   ├── XMLMapperBuilder        # Mapper 文件解析
│   ├── XMLStatementBuilder     # SQL 语句解析
│   └── MapperBuilderAssistant  # 构建辅助类
│
├── cache                       # 缓存模块
│   ├── Cache                   # 缓存接口
│   ├── CacheKey                # 缓存键生成
│   ├── PerpetualCache          # 永久缓存实现
│   └── LruCache                # LRU 缓存装饰器
│
├── datasource                  # 数据源模块
│   ├── DataSource              # 数据源接口
│   └── UnpooledDataSource      # 非池化数据源
│
├── executor                    # 执行器模块
│   ├── Executor                # 执行器接口
│   ├── SimpleExecutor          # 简单执行器（含一级缓存）
│   ├── CachingExecutor         # 二级缓存执行器
│   ├── parameter/
│   │   ├── ParameterHandler            # 参数处理器接口
│   │   └── DefaultParameterHandler     # 默认参数处理
│   ├── resultset/
│   │   ├── ResultSetHandler            # 结果集处理器接口
│   │   └── DefaultResultSetHandler     # 默认结果集处理
│   └── statement/
│       ├── StatementHandler            # Statement 处理器接口
│       └── SimpleStatementHandler      # 简单 Statement 处理
│
├── mapping                     # 映射配置
│   ├── MappedStatement         # SQL 映射信息封装
│   └── SqlCommandType          # SQL 命令类型枚举
│
├── session                     # 会话模块
│   ├── SqlSession              # 会话接口
│   ├── DefaultSqlSession       # 默认会话实现
│   ├── SqlSessionFactory       # 会话工厂接口
│   ├── DefaultSqlSessionFactory # 默认会话工厂
│   ├── SqlSessionFactoryBuilder # 会话工厂构建器
│   └── Configuration           # 全局配置
│
├── transaction                 # 事务模块
│   ├── Transaction             # 事务接口
│   └── JdbcTransaction         # JDBC 事务实现
│
├── io                          # IO 工具
│   ├── Resources               # 资源加载工具
│   └── ClassLoaderWrapper      # 类加载器包装
│
└── utils                       # 工具类
    ├── StringUtils             # 字符串工具
    └── ParamNameResolver       # 参数名解析器
```

## 核心模块说明

### 1. 会话体系

```
SqlSessionFactoryBuilder       # 构建 SqlSessionFactory
        ↓
SqlSessionFactory              # 创建 SqlSession
        ↓
SqlSession                     # 执行 SQL、获取 Mapper
        ↓
Executor (SimpleExecutor / CachingExecutor)
        ↓
StatementHandler               # Statement 处理
        ↓
ParameterHandler → ResultSetHandler
```

### 2. Mapper 代理机制

```
SqlSession.getMapper(StudentMapper.class)
        ↓
MapperRegistry.getMapper()
        ↓
MapperProxyFactory.newInstance()
        ↓
Proxy.newProxyInstance() → MapperProxy (InvocationHandler)
        ↓
MapperMethod.execute() → SqlSession.selectOne/selectList/insert/update/delete
```

### 3. 事务管理

```java
// Transaction 接口
public interface Transaction {
    Connection getConnection() throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;
    void close() throws SQLException;
}

// 使用示例
SqlSession session = sqlSessionFactory.openSession(false);  // 手动提交
try {
    mapper.insert(user);
    session.commit();  // 提交事务
} catch (Exception e) {
    session.rollback();  // 回滚事务
} finally {
    session.close();
}

// 自动提交模式
SqlSession session = sqlSessionFactory.openSession(true);  // 自动提交
```

### 4. 缓存机制

```
一级缓存（Session 级别）
├── 同一 SqlSession 中相同查询直接返回缓存
├── 基于 PerpetualCache 实现（HashMap）
├── 更新操作自动清除缓存
└── commit/rollback/close 时清除缓存

二级缓存（namespace 级别）
├── 跨 SqlSession 共享
├── 通过 <cache/> 标签启用
├── 更新操作自动清除缓存
└── 事务安全：commit 时写入缓存，rollback 时丢弃

Cache 实现
├── PerpetualCache      # 永久缓存（基于 HashMap）
└── LruCache            # LRU 缓存装饰器
```

## 使用方法

### 1. 配置文件

**mybatis-config.xml** (主配置):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <settings>
        <setting name="cacheEnabled" value="true"/>
    </settings>

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1:3306/mydb"/>
                <property name="username" value="root"/>
                <property name="password" value="password"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <mapper resource="mapper/student_mapper.xml"/>
    </mappers>
</configuration>
```

**mapper/student_mapper.xml** (SQL 映射):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="cn.abelib.example.StudentMapper">
    <!-- 启用二级缓存 -->
    <cache/>

    <select id="queryById" parameterType="java.lang.Long" resultType="cn.abelib.example.Student">
        SELECT * FROM student WHERE id = #{id}
    </select>

    <select id="findByNameAndAge" resultType="cn.abelib.example.Student">
        SELECT * FROM student WHERE name = #{name} AND age = #{age}
    </select>

    <insert id="insert" parameterType="cn.abelib.example.Student">
        INSERT INTO student(id, name, age) VALUES(#{id}, #{name}, #{age})
    </insert>

    <update id="update" parameterType="cn.abelib.example.Student">
        UPDATE student SET name = #{name}, age = #{age} WHERE id = #{id}
    </update>

    <delete id="deleteById" parameterType="java.lang.Long">
        DELETE FROM student WHERE id = #{id}
    </delete>
</mapper>
```

### 2. 定义实体和 Mapper 接口

**实体类**:

```java
public class Student {
    private Long id;
    private String name;
    private Integer age;
    // 下划线字段自动映射
    private Date createTime;  // 对应数据库 create_time
    // getter/setter
}
```

**Mapper 接口**:

```java
public interface StudentMapper {
    // 单参数查询
    Student queryById(Long id);

    // 多参数查询（自动支持）
    List<Student> findByNameAndAge(String name, Integer age);

    // 增删改
    int insert(Student student);
    int update(Student student);
    int deleteById(Long id);
}
```

### 3. 使用示例

```java
public class MybatisExample {
    public static void main(String[] args) {
        // 1. 加载配置文件
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");

        // 2. 构建 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 3. 获取 SqlSession（手动提交模式）
        SqlSession session = sqlSessionFactory.openSession(false);

        // 方式一：通过 Mapper 接口（推荐）
        StudentMapper mapper = session.getMapper(StudentMapper.class);

        // 查询单条
        Student student = mapper.queryById(1L);

        // 多参数查询
        List<Student> students = mapper.findByNameAndAge("张三", 20);

        // 插入
        Student newStudent = new Student();
        newStudent.setName("李四");
        newStudent.setAge(22);
        mapper.insert(newStudent);

        // 提交事务
        session.commit();

        // 方式二：直接通过 SQL ID
        Student student = session.selectOne("cn.abelib.example.StudentMapper.queryById", 1L);

        // 关闭会话
        session.close();
    }
}
```

## 特性详解

### 多参数支持

```java
// Mapper 接口
List<Student> findByNameAndAge(String name, Integer age);

// XML 中可以使用参数名
<select id="findByNameAndAge" resultType="Student">
    SELECT * FROM student WHERE name = #{name} AND age = #{age}
</select>

// 或者使用 param1, param2
<select id="findByNameAndAge" resultType="Student">
    SELECT * FROM student WHERE name = #{param1} AND age = #{param2}
</select>
```

### 下划线转驼峰

```java
// 数据库字段: user_name, create_time
// Java 属性: userName, createTime
// 自动映射，无需额外配置
```

### 类型自动转换

```java
// 数据库返回 Integer，实体字段是 Long → 自动转换
// 数据库返回 BigDecimal，实体字段是 Double → 自动转换
// 数据库返回 Timestamp，实体字段是 Date → 自动转换
```

### 数据源配置

```java
// 方式一：XML 配置（自动创建 UnpooledDataSource）
// 见上方 mybatis-config.xml 配置示例

// 方式二：代码配置 UnpooledDataSource
Configuration config = new Configuration();
config.setDataSource(new UnpooledDataSource(
    "com.mysql.jdbc.Driver",
    "jdbc:mysql://localhost:3306/mydb",
    "root",
    "password"
));

// 方式三：注入第三方连接池（HikariCP、Druid 等）
HikariDataSource hikariDataSource = new HikariDataSource();
hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
// ... 其他配置

// 实现 DataSource 接口即可接入
config.setDataSource(new ThirdPartyDataSourceAdapter(hikariDataSource));
```

## 测试覆盖

| 测试类 | 测试数 | 覆盖内容 |
|--------|--------|----------|
| `DefaultParameterHandlerTest` | 8 | 参数类型转换、多参数、Map参数 |
| `DefaultResultSetHandlerTest` | 6 | 结果映射、下划线转驼峰、类型转换 |
| `CacheTest` | 16 | PerpetualCache、LruCache、CacheKey |
| `SimpleExecutorTest` | 9 | 一级缓存命中/清除、CacheKey生成 |
| `CachingExecutorTest` | 6 | 二级缓存事务、commit/rollback逻辑 |
| `SqlSessionTest` | 12 | CRUD操作、事务、Mapper代理 |
| `SqlSessionFactoryTest` | 6 | openSession、autoCommit参数 |
| `XMLConfigBuilderTest` | 4 | 配置解析、MappedStatement注册 |
| `UnpooledDataSourceTest` | 4 | 数据源配置、属性设置 |
| **总计** | **71** | **100% 通过率** |

## 已实现功能

| 功能 | 状态 | 说明 |
|------|------|------|
| XML 配置解析 | ✅ | mybatis-config.xml + mapper.xml |
| Mapper 动态代理 | ✅ | JDK 动态代理 |
| SELECT 查询 | ✅ | selectOne/selectList |
| INSERT 操作 | ✅ | 返回影响行数 |
| UPDATE 操作 | ✅ | 返回影响行数 |
| DELETE 操作 | ✅ | 返回影响行数 |
| 多参数支持 | ✅ | 自动转 Map，支持 param1/param2 |
| 参数类型转换 | ✅ | Date/BigDecimal/Boolean 等 |
| 结果集映射 | ✅ | 自动映射 |
| 下划线转驼峰 | ✅ | user_name → userName |
| 类型自动转换 | ✅ | Integer→Long, BigDecimal→Double |
| 事务支持 | ✅ | commit/rollback，支持 autoCommit |
| 一级缓存 | ✅ | Session 级别缓存 |
| 二级缓存 | ✅ | 跨 Session 共享，事务安全 |
| LRU 缓存 | ✅ | 最近最少使用淘汰 |
| 缓存键生成 | ✅ | SQL + 参数 |
| 数据源接口 | ✅ | 支持注入第三方连接池 |
| 日志框架 | ✅ | SLF4J + Logback |

## 待实现功能

| 功能 | 优先级 | 状态 |
|------|--------|------|
| 动态 SQL (`<if>`, `<where>`) | 中 | TODO |
| ResultMap | 中 | TODO |
| TypeHandler | 低 | TODO |
| 插件机制 | 低 | TODO |
| 延迟加载 | 低 | TODO |
| 分页支持 | 低 | TODO |

## 快速开始

```bash
# 克隆项目
git clone https://github.com/Abel-Huang/Minebatis.git

# 编译
mvn compile

# 运行测试
mvn test

# 运行示例（需配置数据库）
mvn exec:java -Dexec.mainClass="cn.abelib.example.MybatisExample"
```

## 代码统计

| 指标 | 数值 |
|------|------|
| 源代码行数 | ~3,100 行 |
| 测试代码行数 | ~1,500 行 |
| Java 文件数 | 44 个 |
| 测试方法数 | 71 个 |

## 技术栈

- Java 21
- dom4j (XML 解析)
- jaxen (XPath)
- MySQL Connector 5.1.46
- JUnit 4.13.2
- Mockito 4.11.0
- SLF4J + Logback (日志)
- Guava

## 参考资料

- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
- [MyBatis 源码](https://github.com/mybatis/mybatis-3)

## License

MIT License
