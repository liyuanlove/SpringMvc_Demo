package com.springmvc.lvcr.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springmvc.lvcr.annotion.Autowired;
import com.springmvc.lvcr.annotion.Controller;
import com.springmvc.lvcr.annotion.RequestMapping;
import com.springmvc.lvcr.annotion.Service;
import com.springmvc.lvcr.commons.CofigUtils;
import com.springmvc.lvcr.controller.HelloController;

//@WebServlet("MyDispatcherServlet")
public class MyDispatcherServlet extends HttpServlet {

	/**
	 * serialVersionUID:TODO(用一句话描述这个变量表示什么).
	 */
	private static final long serialVersionUID = -8832896412769630971L;

	// 定义一个扫面的类的全名的集合（eg：com.****.**.class）
	private List<String> classPathNames = new ArrayList<String>();
	// 定义一个装实例的Map
	private Map<String, Object> instanceMaps = new HashMap<String, Object>();
	// 方法链的集合对象
	private Map<String, Method> handlerMaps = new HashMap<String, Method>();
	@Override
	public void init() throws ServletException {

		try {
			// 获取xml配置信息
			String basePackge = CofigUtils.getBasePackgName(
					"E:\\eclipse\\codeWorkspace\\dongnao\\SpringMvc_Demo\\config\\springmvc.xml");
			System.out.println("====basePackge:" + basePackge);
			// 1、扫描基础包下面的Bean
			scanBase(basePackge);
			// 2、找到每个Bean的实例
			foundBeansInstance();
			// 3、 注入通过Autowired.class对象注入实例
			springIoc();
			// 4、 方法链去处理
			handlerMaps();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * 
	 * TODO(将方法的访问全路径 添加到handlerMaps方法链中 eg:/helloController/insert)   
	 * @author Administrator  
	 * @Date 2017年4月23日下午7:36:44
	 */
	private void handlerMaps() {
		if (instanceMaps.size() == 0) {
			return;
		}
		//遍历instanceMaps中所有的bean
		for(Map.Entry<String, Object> entry  : instanceMaps.entrySet()){
			//如果该类是被@Controller注解的Bean
			if(entry.getValue().getClass().isAnnotationPresent(Controller.class)){
				//1、获取@Controller设置的类访问路径
				//获取Controller注解类
				Controller controllerClass = (Controller)entry.getValue().getClass().getAnnotation(Controller.class);
				//获取@Controller设置的value  即controller类的访问路径 eg:@Controller("/hello")
				String controllerAnnotionParam = controllerClass.value();
				//1、获取@RequestMapping设置的方法访问路径 
				Method[] methods = entry.getValue().getClass().getMethods();
				for (Method method : methods) {
					//如果方法是被@RequestMapping注解的方法
					 if(method.isAnnotationPresent(RequestMapping.class)){
						 //获取RequestMapping 注解类
						 RequestMapping requestMappingAnnotionObject = method.getAnnotation(RequestMapping.class);
						 //获取@RequestMapping设置的value  即方法的访问路径 eg:@RequestMapping("/insert")
						 String requestMappingAnnotionParam = requestMappingAnnotionObject.value();
						 //将方法的访问全路径 添加到handlerMaps方法链中 eg:/helloController/insert
						 System.out.println("========="+controllerAnnotionParam+requestMappingAnnotionParam);
						 handlerMaps.put(controllerAnnotionParam+requestMappingAnnotionParam, method);
					 }else{
						 continue;
					 }
				}
				
				
			}else{
				continue;
			}
			
		}
		

	}
	/**
	 * 
	 * TODO(依赖注入：eg：如果被@Controller注解的类 的 属性 中被@Autowired注解了，则将该属性设置为@Autowired中value对应的实例对象 )  
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException 
	 * @author Administrator  
	 * @Date 2017年4月23日下午7:21:30
	 */
	private void springIoc() throws IllegalArgumentException, IllegalAccessException {
		if (instanceMaps.size() == 0) {
			return;
		}
		//遍历instanceMaps中所有的bean
		for(Map.Entry<String, Object> entry  : instanceMaps.entrySet()){
			//获取每个bean的属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				//如果属性是@Autowired注解的
				if(field.isAnnotationPresent(Autowired.class)){
					String autowiredParam = ((Autowired) field
							.getAnnotation(Autowired.class)).value();
					//获取private
					field.setAccessible(true);
					field.set(entry.getValue(),
							instanceMaps.get(autowiredParam));
				}else{
					continue;
				}
			}
		}

	}

	private void foundBeansInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (classPathNames.isEmpty()) {
			return;
		}
		for (String className : classPathNames) {
			Class<?> cs = Class.forName(className.replace(".class", ""));
			// 如果类是@Controller注解的类
			if (cs.isAnnotationPresent(Controller.class)) {
				// 获取class文件的实例
				Object contrellerClass = cs.newInstance();
				// 获取注解类
				Controller controllerAnnotionObject = (Controller) cs.getAnnotation(Controller.class);
				// 获取注解的value
				String annotionValue = controllerAnnotionObject.value();
				//将@controller注解的类 存到key为value的值     value为该对象的缓存Map中
				instanceMaps.put(annotionValue, contrellerClass);
				
				//如果类是@Service注解的类
			}else if(cs.isAnnotationPresent(Service.class)){
				// 获取class文件的实例
				Object serviceClass = cs.newInstance();
				// 获取注解类
				Service serviceAnnotionObject = (Service) cs.getAnnotation(Service.class);
				String annotionValue = serviceAnnotionObject.value();
				//将@service注解的类 存到key为value的值     value为该对象的缓存Map中
				instanceMaps.put(annotionValue, serviceClass);
			}else{
				continue;
			}
		}

	}

	/**
	 * 
	 * TODO(后去基础包下面所有的java类，并添加到packeNames中)
	 * 
	 * @param basePackge
	 * @author Administrator
	 * @Date 2017年4月23日下午6:55:02
	 */
	private void scanBase(String basePackge) {
		URL url = this.getClass().getClassLoader().getResource("/" + replacePath(basePackge));
		// 拿到这个资源路径下面的文件或者文件夹
		String pathFile = url.getFile();
		File file = new File(pathFile);
		String[] files = file.list();
		for (String filepath : files) {
			// 这个要写全文件名称
			File eachFile = new File(pathFile + filepath);
			// 如果是文件夹 那么我们在要调用这个本方法进入拿真正的文件
			if (eachFile.isDirectory()) {
				// 将基础包下面的包当成二次基包传入进去
				scanBase(basePackge + "." + eachFile.getName());
			} else if (eachFile.isFile()) {
				classPathNames.add(basePackge + "." + eachFile.getName());
				System.out.println("Spring容器扫面到的类有：" + basePackge + "." + eachFile.getName());
			}
		}

	}

	private String replacePath(String basePackge) {
		// TODO Auto-generated method stub
		return basePackge.replaceAll("\\.", "/");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 拿到完整路径
		String uri = req.getRequestURI();
		String projectName = req.getContextPath();
		// bastURL+methodUrl
		String path = uri.replace(projectName, "");
		// 方法对象
		Method method = handlerMaps.get(path);
		PrintWriter outPrintWriter = resp.getWriter();
		if (method == null) {
			// 给客户端一个友好的提示
			outPrintWriter.write("您访问的这个资源找不到，请检查您的访问地址！");
			return;
		}
		// localhost:8080/SpringMvcDemo/hello/insert
 		String className = uri.split("/")[2];
		 className = "/"+className;
		HelloController helloController = (HelloController) instanceMaps.get(className);
		try {
			method.invoke(helloController, new Object[] { req, resp, null });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

}
