package top.iceclean.chatspace.gateway.common;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.ExpiredJwtException;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Ice'Clean
 * @date : 2022-01-29
 *
 * 返回给前端的固定格式
 */
public class Response {
    /** 返回的状态码，由 ResponseStatusEnum 给定 */
    private Integer status;
    /** 附加的信息 */
    private String msg;
    /** 返回的数据 */
    private Map<String, Object> data = new HashMap<>();

    public Response(Exception e) {
        if (e instanceof NullPointerException) {
            setStatus(AuthEnum.EMPTY_TOKEN);
        } else if (e instanceof ExpiredJwtException) {
            setStatus(AuthEnum.EXPIRE_TOKEN);
        } else {
            setStatus(AuthEnum.ILLEGAL_TOKEN);
        }
    }

    public Response addData(String name, Object value) {
        data.put(name, value);
        return this;
    }

    public Response removeData(String name) {
        data.remove(name);
        return this;
    }

    public Response setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Response setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 通过全局枚举字段直接装配
     * @param status 全局枚举状态码
     * @return 通用返回结果
     */
    public Response setStatus(AuthEnum status) {
        this.status = status.value();
        this.msg = status.msg();
        return this;
    }

    /**
     * 封装一个对象数据
     * @param data 对象数据
     * @param <T> 类型
     * @return 通用返回结果
     */
    public <T> Response setData (T data) {
        Class<?> dataClass = data.getClass();
        for (Field field : dataClass.getDeclaredFields()) {
            boolean flag = field.isAccessible();
            try {
                field.setAccessible(true);
                this.addData(field.getName(),field.get(data));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(flag);
        }
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "[" + status + " : " + msg + "] " + data;
    }

    public byte[] toBytes() {
        return JSONObject.toJSONString(this).getBytes(StandardCharsets.UTF_8);
    }
}
