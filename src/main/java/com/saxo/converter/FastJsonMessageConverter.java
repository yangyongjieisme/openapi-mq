package com.saxo.converter;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.alibaba.fastjson.JSON;

/**
 * @author lotey
 * @date 2017/3/22 14:25
 * @desc fastjson消息转换
 */
public class FastJsonMessageConverter extends AbstractMessageConverter {

    private static Logger logger = LogManager.getLogger(FastJsonMessageConverter.class);

    public static final String DEFAULT_CHARSET = "UTF-8";

    private volatile String defaultCharset = DEFAULT_CHARSET;

    public FastJsonMessageConverter() {
        super();
        
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = (defaultCharset != null) ? defaultCharset : DEFAULT_CHARSET;
    }

    public Object fromMessage(Message message) throws MessageConversionException {
        return null;
    }

    /**
     * @date 2017/3/22 14:35
     * @desc 将mq消息转换为对�?
     * @param message
     * @param t
     * @param <T>
     * @return
     */
    public <T> T fromMessage(Message message,T t) {
        String json = "";
        try {
            json = new String(message.getBody(),defaultCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return (T) JSON.parseObject(json,t.getClass());
    }

    /**
     * @date 2017/3/22 14:35
     * @desc 创建新的message对象
     * @param objectToConvert
     * @param messageProperties
     * @return
     * @throws MessageConversionException
     */
    protected Message createMessage(Object objectToConvert, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes = null;
        try {
            String jsonString = JSON.toJSONString(objectToConvert);
            bytes = jsonString.getBytes(this.defaultCharset);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException("Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(this.defaultCharset);
        if (bytes != null) {
            messageProperties.setContentLength(bytes.length);
        }
        return new Message(bytes, messageProperties);
    }
}