package com.saxo.mqproducer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author lotey
 * @date 2017/4/20 10:18
 * @desc 自定义rabbitmq Template实现读取配置文件实例化bean
 */
public class CustomRabbitTemplate extends RabbitTemplate {

    private static Logger logger = LogManager.getLogger(CustomRabbitTemplate.class);

    public CustomRabbitTemplate(ConnectionFactory connectionFactory, RabbitAdmin admin, MessageConverter messageConverter, String exchangeName, String routingKey,String configFileName) {
        
        super();
        this.setMessageConverter(messageConverter);
        this.setConnectionFactory(connectionFactory);
        //读取配置文件，完成mqqueue的初始化
        BufferedReader bfr = null;
        FileReader fr = null;
        try {
            String fileName = getClass().getClassLoader().getResource(configFileName).getFile();
            //读取配置文件，初始化
            fr = new FileReader(new File(fileName));
            bfr = new BufferedReader(fr);
            String line = null;
            StringBuffer configSbf = new StringBuffer();
            while ((line = bfr.readLine()) != null) {
                configSbf.append(line);
            }
            String configJsonText = configSbf.toString();
            //创建
            FanoutExchange fanoutExchange = new FanoutExchange(exchangeName);
            admin.declareExchange(fanoutExchange);
            //创建mqqueue对象
            Queue queue = null;
            Map<String,Object> map = (Map<String,Object>)JSON.parse(configJsonText);
            Iterator<Map.Entry<String,Object>> ite = map.entrySet().iterator();
            JSONObject queueObject = null;
            while (ite.hasNext()) {
                Map.Entry<String,Object> entry = ite.next();
                queueObject = (JSONObject)entry.getValue();
                queue = new Queue(queueObject.getString("name"));
                admin.declareQueue(queue);
                admin.declareBinding(new Binding(queueObject.getString("name"), Binding.DestinationType.QUEUE,exchangeName,routingKey,new HashedMap()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("======================mq初始化失败======================");
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bfr != null) {
                try {
                    bfr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}