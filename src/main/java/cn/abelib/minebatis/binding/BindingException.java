package cn.abelib.minebatis.binding;

/**
 * @author abel.huang
 * @date 2026/4/11
 * 绑定异常
 */
public class BindingException extends RuntimeException {

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
