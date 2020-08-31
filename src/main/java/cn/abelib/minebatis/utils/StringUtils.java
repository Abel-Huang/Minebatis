package cn.abelib.minebatis.utils;

import java.util.Objects;

/**
 * @Author: abel.huang
 * @Date: 2020-08-31 23:32
 */
public class StringUtils {
    private StringUtils(){}

    public static boolean isEquals(String str1, String str2) {
        if (Objects.isNull(str1) || Objects.isNull(str2)) {
            return false;
        }
        return str1.equals(str2);
    }
}
