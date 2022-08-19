package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZhouyuApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); // 单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new LinkedList<>();


    public ZhouyuApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        // 解析ComponentScan注解 ---> 扫描路径 ---> 扫描 ---> Beandefinition ---> BeanDefinitionMap
        scan(configClass);  // 扫描

        // 对于单例bean在容器启动的时候就应该创建好
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);   // 单例Bean
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition){
        // 创建bean
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance(); // 通过反射调用无参的构造方法得到一个对象

            // 依赖注入（对属性进行赋值）
            for (Field declaredField : clazz.getDeclaredFields()) {     // 将所有的属性拿出来
                if(declaredField.isAnnotationPresent(Autowired.class)){ // 只有加了Autowired注解才需要赋值
                    // 根据属性名字找
                    Object bean = getBean(declaredField.getName());// 调用getBean方法（里面根据单例、多例有不同的策略）
                    declaredField.setAccessible(true);              // 取消 Java 语言访问检查
                    declaredField.set(instance, bean);              // 给instance对象的declaredField属性赋值
                }
            }

            // Aware回调
            if(instance instanceof BeanNameAware){  // 当前这个实例是否实现了BeanNameAware接口
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 执行List中所有 BeanPostProcessor 的 postProcessBeforeInitialization 初始化前方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                 instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 执行List中所有 BeanPostProcessor 的 postProcessAfterInitialization 初始化后方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }



            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {

        //获取ComponentScan注解
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();  // 扫描路径  com.zhouyu.service
        path = path.replace(".", "/");  // 将 . 替换为 /

        // 扫描
        // 三种类加载器
        // Bootstrap --->jre/lib
        // Ext       --->jre/ext/lib
        // App       --->classpath      其实就是target中的classes目录
        ClassLoader classLoader = ZhouyuApplicationContext.class.getClassLoader();  // App类加载器
        URL resource = classLoader.getResource(path);// 相对路径，相对的就是classpath路径
        File file = new File(resource.getFile());       // 将resource转成file
        if(file.isDirectory()){                         // 判断当前file是不是一个目录
            File[] files = file.listFiles();       // 将目录下的所有文件拿出来
            for (File f : files) {
                String fileName = f.getAbsolutePath();  // 得到的这个路径是绝对路径，我们需要截取其中我们需要的
                if(fileName.endsWith(".class")){        // 是类文件的话才处理
                    // 将路径中不需要的去掉，把 \ 改为 .  ，去掉最后的class
                    // 从com开始截取，截取到.class（不含.class），例如 com\zhouyu\service\UserService
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    // 将 \ 替换为 .     \\ 中第一个\表示将第二个\转义为普通的\
                    className = className.replace("\\", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);   // // 加载类\
                        if (clazz.isAnnotationPresent(Component.class)){   // 判断是否有Component注解

                            //Spring底层并不是这样实现的，我们只是模拟
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) { //如果这个bean是 BeanPostProcessor
                                // 如果当前这个类实现了 BeanPostProcessor 接口，直接实例化
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            // 表示当前这个类是一个bean
                            // 解析类，判断当前类是单例bean还是prototype类型bean
                            // 解析类 --> BeanDefinition
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();  // 当前这个类的bean名字

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if(clazz.isAnnotationPresent(Scope.class)){ // 存在Scope注解就获取这个类的Scope注解
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());   // 有scope注解就设置为注解中的值
                            } else {
                                // 没有Scope注解表示当前这个bean是单例的
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){      // 单例bean
                Object o = singletonObjects.get(beanName);
                return o;
            }else {     //多例
                // 每次都创建新的bean对象
                Object bean = createBean(beanName, beanDefinition);
                return bean;
            }
        } else {
            // 不存在这个bean的话就抛出空指针
            throw new NullPointerException();
        }

    }
}
