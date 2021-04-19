package com.wingstudio.servlet;

import com.alibaba.fastjson.JSON;
import com.wingstudio.annotation.Controller;
import com.wingstudio.annotation.RequestMapping;
import com.wingstudio.annotation.ResponseBody;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ITcz
 * @Data 2021-04-18 - 21:07
 */

public class ItczDispatcherServlet extends HttpServlet{

    /**
    *获得项目路径
     * value = /F:/java_Project/springmvc_despacherServlet/target/classes/
     */
    private static String CLASS_PATH = ItczDispatcherServlet.class.getResource("/").getPath();

    /**
    * 获取在ItczMvc.xml中配置的package包扫描路径
    * */
    private static String SCAN_PATH = "";

    /**
    * BASE_PATH = CLASS_PATH + SCAN_PATH
    * */
    private static String BASE_PATH;

    private static ConcurrentHashMap<String, Method> requestMappingMap = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //localhost:80/user/getUser.do     /user/getUser.do
            String requestURI = req.getRequestURI();
            Method method = requestMappingMap.get(requestURI);
            if (method != null) {
                //应该从Spring容器里拿到Controller对象
                // 这里从方法拿到类
                Class<?> declaringClass = method.getDeclaringClass();
                Object o = declaringClass.newInstance();
                //拿到形参对象
                Parameter[] parameters = method.getParameters();
                //定义一个实参数组，调用controller方法传入的参数
                Object[] objects = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    Class paramType = parameters[i].getType();
                    if (paramType == HttpServletRequest.class) {
                        objects[i] = req;
                    }else if (paramType == HttpServletResponse.class) {
                        objects[i] = resp;
                    }else if (paramType == String.class) {
                        //通过请求名从request拿到参数
                        String parameter = req.getParameter(paramName);
                        objects[i] = parameter;
                    }else {
                        //剩余的类型为Object
                        Object o1 = paramType.newInstance();
                        for (Field field : paramType.getDeclaredFields()) {
                            //声明可以操作字节码对象,因为可能对象是私有的，操作会报错
                            field.setAccessible(true);
                            String name = field.getName();
                            String parameter = req.getParameter(name);
                            field.set(o1 ,parameter);
                        }
                        objects[i] = o1;
                    }

                }
                //执行方法（对象，方法参数）
                Object invoke = method.invoke(o, objects);
                if (method.getAnnotation(ResponseBody.class) != null) {
                    resp.getWriter().write(JSON.toJSONString(invoke));
                }else {
                    if (method.getReturnType() == String.class) {
                        req.getRequestDispatcher("/" + (String) invoke).forward(req, resp);
                    }
                }
            }else {
                resp.setStatus(404);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            //解析XML
            String myConfigLocation = config.getInitParameter("ItczContextConfigLocation");
            //Java会把空格转成"%20"，这里转化回来
            CLASS_PATH = CLASS_PATH.replaceAll("%20", " ");
            File xmlFile = new File(CLASS_PATH + myConfigLocation);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(xmlFile);
            Element rootElement = document.getRootElement();
            Element packageScan = rootElement.element("packageScan");
            Attribute aPackage = packageScan.attribute("package");
            //xml中配置的值为 "com"
            SCAN_PATH = aPackage.getValue();
            File file = new File(CLASS_PATH + SCAN_PATH);
            BASE_PATH = file.getPath();
            scanProject(file);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //super.init(config);
    }


    public void scanProject(File file) {
        try {
            //递归
            if (file.isDirectory()) {
                for (File file1 : file.listFiles()) {
                    scanProject(file1);
                }
            }else {
                //不是一个文件夹
                //判断是不是class
                String fileName = file.getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".class")) {
                    String filePath = file.getPath();
                    //F:\java_Project\springmvc_despacherServlet\target\classes\com\wingstudio\servlet\ItczDispatcherServlet.class
                    System.out.println("filePath: " + filePath);
                    //F:\java_Project\springmvc_despacherServlet\target\classes\com
                    System.out.println("BASE_PATH: " + BASE_PATH);
                    filePath = filePath.replace(BASE_PATH, "");
                    filePath = SCAN_PATH + filePath;
                    String classPath = filePath.replaceAll("\\\\", ".");
                    String className =  classPath.substring(0 , classPath.lastIndexOf("."));
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                        String url_path = "";
                        if (classRequestMapping != null) {
                            url_path = classRequestMapping.value();
                        }
                        for (Method method : clazz.getDeclaredMethods()) {
                            String methodPath = "";
                            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                            if (methodRequestMapping != null) {
                                 methodPath = methodRequestMapping.value();
                                //Spring底层也是维护了一个ConcurrentHashMap
                                requestMappingMap.put(url_path + methodPath, method);
                                System.out.println(url_path + methodPath + "路径被映射到了" + clazz.getName() + "类的" + method.getName() + "方法");
                            }
                        }
                    }

                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
