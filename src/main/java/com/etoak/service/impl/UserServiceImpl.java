package com.etoak.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.etoak.bean.Email;
import com.etoak.bean.User;
import com.etoak.mapper.UserMapper;
import com.etoak.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    JmsTemplate jmsTemplate;

    @Override
    public int addUser(User user) {
        /*
         * 密码MD5加密
         */
        String password = user.getPassword();
        password = DigestUtils.md5Hex(password);
        user.setPassword(password);
        int addResult = userMapper.addUser(user);
        // 将自增id返回到user对象的id属性
        log.info("user.id - {}", user.getId());

        // 发视JMS消息
        jmsTemplate.send("email", session -> {
            Email email = new Email();
            email.setSubject("用户激活邮件");
            email.setReceiver(user.getEmail());
            email.setContent("请点击激活:http://localhost:8001/boot/user/active/" + user.getId());
            return session.createTextMessage(JSONObject.toJSONString(email));
        });
        return addResult;
    }
}
