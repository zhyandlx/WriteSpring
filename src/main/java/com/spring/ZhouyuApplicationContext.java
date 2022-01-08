package com.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZhouyuApplicationContext {

    private  Class configClass;

    private Map<String,BeanDefinition> beanDefinitionMap=new HashMap<>();

    private Map<String,Object> singletonObjects=new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessors =new ArrayList<>();

    public ZhouyuApplicationContext(Class configClass) throws Exception {
        this.configClass = configClass;
        //扫描
        scan(configClass);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }
    }

    private Object createBean(String beanName,BeanDefinition beanDefinition) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class clazz = beanDefinition.getType();
        Object instance=null;
           instance= clazz.getConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance,getBean(field.getName()));
                }
            }

            if (instance instanceof BeanNameAware) {
                 ((BeanNameAware)instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance=beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance=beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

        return instance;
    }

    public Object getBean(String beanName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!beanDefinitionMap.containsKey(beanName)){
            throw new RuntimeException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition.getScope().equals("singleton")) {
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean==null) {
                singletonBean=createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,singletonBean);
            }
            return singletonBean;
        }else{
            Object prototypeBean = createBean(beanName, beanDefinition);
            return prototypeBean;
        }
    }




    private void scan(Class configClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan annotationComponentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            //找target下面的class
            String path= annotationComponentScan.value();
            path = path.replace(".", "/");
            ClassLoader classLoader = ZhouyuApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file =new File(resource.getFile());
            if (file.isDirectory()){
                for (File f : file.listFiles()) {
                    String absolutePath = f.getAbsolutePath();
                    absolutePath=absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                    absolutePath=absolutePath.replace("\\",".");
                    try {
                        Class<?> clazz = classLoader.loadClass(absolutePath);
                        if (clazz.isAnnotationPresent(Component.class)){
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor     beanPostProcessor= (BeanPostProcessor) clazz.getConstructor().newInstance();
                                beanPostProcessors.add(beanPostProcessor);
                            }
                            Component component = clazz.getAnnotation(Component.class);
                            String beanName=component.value();
                            if (beanName.equals("")) {
                                beanName=Introspector.decapitalize(clazz.getSimpleName());
                            }
                            BeanDefinition beanDefinition=new BeanDefinition();
                            beanDefinition.setType(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                String value = scopeAnnotation.value();
                                beanDefinition.setScope(value);
                            }else{
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName,beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
