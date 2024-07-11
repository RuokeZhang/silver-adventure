package com.xuecheng.base.exception;

/**
 * 学成在线项目的自定义异常类，继承自RuntimeException。
 */
public class XueChengPlusException extends RuntimeException {

    // 用于存储错误信息的变量。
    private String errMessage;

    /**
     * 无参数构造器。
     * 调用RuntimeException的无参数构造器。
     */
    public XueChengPlusException() {
        super();  // 调用父类（RuntimeException）的构造器
    }

    /**
     * 带有错误消息参数的构造器。
     * 这个构造器设置异常的错误消息，并存储到本地变量中。
     * @param errMessage 错误消息
     */
    public XueChengPlusException(String errMessage) {
        super(errMessage);  // 调用RuntimeException的构造器，传入错误消息
        this.errMessage = errMessage;  // 将错误消息保存到实例变量中
    }

    /**
     * 获取错误消息的方法。
     * @return 错误消息字符串
     */
    public String getErrMessage() {
        return errMessage;
    }

    /**
     * 静态方法，用于抛出一个带有指定错误消息的异常。
     * @param errMessage 错误消息字符串
     */
    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }

    /**
     * 静态方法，用于抛出一个根据CommonError对象的错误消息创建的异常。
     * @param commonError CommonError对象，包含错误信息
     */
    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

}
