package com.zhouyu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;
import com.spring.ZhouyuApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class ZhouyuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            Object instance = Proxy.newProxyInstance(ZhouyuApplicationContext.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    return method.invoke(bean,args);
                }
            });
            return instance;
        }
        return bean;
    }
}

