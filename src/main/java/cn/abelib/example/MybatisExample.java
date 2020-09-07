package cn.abelib.example;

import cn.abelib.minebatis.io.Resources;
import cn.abelib.minebatis.session.SqlSession;
import cn.abelib.minebatis.session.SqlSessionFactory;
import cn.abelib.minebatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author abel.huang
 * @date 2020/5/19 11:39
 */
public class MybatisExample {
    private static SqlSession session;

    static {
        String resource = "mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            session = sqlSessionFactory.openSession();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mapperTest();
    }

    private static void sessionTest() {
        Student user = session.selectOne("cn.abelib.example.StudentMapper.queryUserInfoById",
                1L);
        System.out.println(user);
    }

    private static void mapperTest() {
        StudentMapper studentDao = session.getMapper(StudentMapper.class);
        Student user = studentDao.queryUserInfoById(3L);
        System.out.println(user);
    }
}
