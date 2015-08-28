package com.sluckframework.common.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sluckframework.common.exception.Assert;

/**
 * class 反射工具
 * 
 * @author sunxy
 * @time 2015年8月28日 下午12:49:48	
 * @since 1.0
 */
public class ReflectionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);
	
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);

    private ReflectionUtils() {
        // utility class
    }

    static {
        primitiveWrapperTypeMap.put(boolean.class, Boolean.class);
        primitiveWrapperTypeMap.put(byte.class, Byte.class);
        primitiveWrapperTypeMap.put(char.class, Character.class);
        primitiveWrapperTypeMap.put(double.class, Double.class);
        primitiveWrapperTypeMap.put(float.class, Float.class);
        primitiveWrapperTypeMap.put(int.class, Integer.class);
        primitiveWrapperTypeMap.put(long.class, Long.class);
        primitiveWrapperTypeMap.put(short.class, Short.class);
    }

    /**
     * 获取对象指定字段的值
     *
     * @param field  
     * @param object The object to retrieve the field's value from
     * @return the value of the filed in the object
     *
     * @throws IllegalStateException if the field is not accessible and the security manager doesn't allow it to be
     * made
     * accessible
     */
    public static Object getFieldValue(Field field, Object object) {
        ensureAccessible(field);
        try {
            return field.get(object);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Unable to access field.", ex);
        }
    }

    /**
     * 获取方法被声明的 class
     * @param instanceClass  The class on which to look for the method
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The class on which the method is declared, or <code>null</code> if not found
     */
    public static Class<?> declaringClass(Class<?> instanceClass, String methodName, Class<?>... parameterTypes) {
        try {
            return instanceClass.getMethod(methodName, parameterTypes).getDeclaringClass();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 判断是否重写了equals方法
     *
     * @param type The type to inspect
     * @return result
     */
    public static boolean hasEqualsMethod(Class<?> type) {
        return !Object.class.equals(declaringClass(type, "equals", Object.class));
    }

    /**比较两个对象不相等
     * @param value
     * @param otherValue
     * @return true if notEquals, false if ==
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean explicitlyUnequal(Object value, Object otherValue) {
        if (value == otherValue) { // NOSONAR (The == comparison is intended here)
            return false;
        } else if (value == null || otherValue == null) {
            return true;
        } else if (value instanceof Comparable) {
            return ((Comparable) value).compareTo(otherValue) != 0;
        } else if (hasEqualsMethod(value.getClass())) {
            return !value.equals(otherValue);
        }
        return false;
    }

    /**确保字段或方法可被操作，即使是private
     * @param member
     * @return
     */
    public static <T extends AccessibleObject> T ensureAccessible(T member) {
        if (!isAccessible(member)) {
        	member.setAccessible(true);
        }
        return member;
    }

    /**判断字段或方法是否可被操作
     * @param member
     * @return
     */
    public static boolean isAccessible(AccessibleObject member) {
        return member.isAccessible() || (Member.class.isInstance(member) && isNonFinalPublicMember((Member) member));
    }

    /**确保member 为public 且 not final
     * @param member
     * @return
     */
    public static boolean isNonFinalPublicMember(Member member) {
        return (Modifier.isPublic(member.getModifiers())
                && Modifier.isPublic(member.getDeclaringClass().getModifiers())
                && !Modifier.isFinal(member.getModifiers()));
    }

    /** 获取指定class 所有字段
     * @param clazz
     * @return
     */
    public static Iterable<Field> fieldsOf(Class<?> clazz) {
        List<Field> fields = new LinkedList<Field>();
        Class<?> currentClazz = clazz;
        do {
            fields.addAll(Arrays.asList(currentClazz.getDeclaredFields()));
            currentClazz = currentClazz.getSuperclass();
        } while (currentClazz != null);
        return Collections.unmodifiableList(fields);
    }

    /**获取指定class 所有method
     * @param clazz
     * @return
     */
    public static Iterable<Method> methodsOf(Class<?> clazz) {
        List<Method> methods = new LinkedList<Method>();
        Class<?> currentClazz = clazz;
        do {
            methods.addAll(Arrays.asList(currentClazz.getDeclaredMethods()));
            addMethodsOnDeclaredInterfaces(currentClazz, methods);
            currentClazz = currentClazz.getSuperclass();
        } while (currentClazz != null);
        return Collections.unmodifiableList(methods);
    }

    /**获取基本类型的包装类型
     * @param primitiveType
     * @return
     */
    public static Class<?> resolvePrimitiveWrapperType(Class<?> primitiveType) {
        Assert.notNull(primitiveType, "primitiveType may not be null");
        Assert.isTrue(primitiveType.isPrimitive(), "primitiveType is not actually primitive: " + primitiveType);

        Class<?> primitiveWrapperType = primitiveWrapperTypeMap.get(primitiveType);
        Assert.notNull(primitiveWrapperType, "no wrapper found for primitiveType: " + primitiveType);
        return primitiveWrapperType;
    }

    private static void addMethodsOnDeclaredInterfaces(Class<?> currentClazz, List<Method> methods) {
        for (Class<?> iface : currentClazz.getInterfaces()) {
            methods.addAll(Arrays.asList(iface.getDeclaredMethods()));
            addMethodsOnDeclaredInterfaces(iface, methods);
        }
    }

    /**判断字段是否为 transient 类型的
     * @param field
     * @return
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }
    
    /**
	 * 取得指定包下的所有类集合
	 * 
	 * @param packageName
	 *            包名称（例如：com.shengpay)
	 * @param fileType
	 *            类列表
	 * @return
	 */
	public static Set<Class<?>> getClassSetByPackageName(String packageName) {
		packageName = packageName.replace('.', '/');
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		String fileType = ".class";
		Set<String> classSetByPackageName = getClassSetByPackageName(packageName, fileType);
		for (String string : classSetByPackageName) {
			String classFullName = string.replace('/', '.').replace('\\', '.').substring(0, string.length() - fileType.length());
			try {
				classSet.add(Class.forName(classFullName));
			} catch (Throwable e) {
			}
		}
		return classSet;
	}

	/**
	 * 取得指定包下的所有类集合
	 * 
	 * @param packageName
	 *            包名称（例如：com.shengpay)
	 * @param fileType
	 *            类列表
	 * @return
	 */
	public static Set<Class<?>> getClassSetByPackageName(String packageName, Class<? extends Annotation> atn) {
		packageName = packageName.replace('.', '/');
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		String fileType = ".class";
		Set<String> classSetByPackageName = getClassSetByPackageName(packageName, fileType);
		for (String string : classSetByPackageName) {
			String classFullName = string.replace('/', '.').replace('\\', '.').substring(0, string.length() - fileType.length());
			try {
				Class<?> class1 = Class.forName(classFullName);
				if (class1.getAnnotation(atn) != null) {
					classSet.add(class1);
				}
			} catch (Throwable e) {
				throw new IllegalStateException("can't find class info by packageName", e);
			}
		}
		return classSet;
	}

	/**
	 * 取得类路径下指定文件夹下指定类型名的文件列表
	 * 
	 * @param dirPath
	 *            文件夹名称（例如：com/shengpay)
	 * @param fileType
	 *            类型名称（例如：.class 或者 .properties)
	 * @return 符合要求的文件路面集合(例如：\com\shengpay\commons\core\propertiesfile\
	 *         PropertiesFileHandlerImplForSystemFile.class）
	 */
	public static Set<String> getClassSetByPackageName(String dirPath, String fileType) {
		// 取得包含有指定包的所有URL集合
		Enumeration<URL> resources;
		try {
			resources = Thread.currentThread().getContextClassLoader().getResources(dirPath.replace('\\', '/'));
		} catch (IOException e) {
			throw new RuntimeException("cant get resource", e);
		}

		// 分别从文件夹或JAR中取得类集合
		Set<String> classSet = new HashSet<String>();
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();

			logger.info("从路径【" + url + "】加载类信息！");
			String protocol = url.getProtocol();
			if ("jar".equals(protocol)) {
				try {
					getClassSetForJar(dirPath, url, classSet, fileType);
				} catch (Exception e) {
					throw new RuntimeException("", e);
				}
			} else if ("file".equals(protocol)) {
				String filePath;
				try {
					filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("", e);
				}
				findAndAddClassesInPackageByFile(dirPath, filePath, classSet, fileType);
			}
		}
		return classSet;
	}

	/**
	 * @param packageName
	 * @param url
	 * @param classSet
	 * @param fileType
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static void getClassSetForJar(String packageName, URL url, Set<String> classSet, String fileType)
			throws IOException, ClassNotFoundException {
		JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			String jarEntryName = jarEntry.getName();
			if (jarEntry.isDirectory()) {
				continue;
			}
			if (!jarEntryName.startsWith(packageName)) {
				continue;
			}
			if (!jarEntryName.endsWith(fileType)) {
				continue;
			}
			classSet.add(jarEntryName);
		}
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param classSet
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, Set<String> classSet, final String fileType) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (file.isDirectory()) || (file.getName().endsWith(fileType));
			}
		});

		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + '/' + file.getName(), file.getAbsolutePath(), classSet, fileType);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName();
				classSet.add(packageName + '/' + className);
			}
		}
	}

	/**
	 * 返回指定方法的参数的KEY数组
	 * 
	 * @param method
	 * @return
	 */
	public static String[] getMethodParamsKey(Method method) {
		StringBuffer methodFullName = new StringBuffer(getMethodFullName(method));

		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			methodFullName.append((i == 0 ? "(" : ",") + parameterTypes[i].getName() + (i + 1 == parameterTypes.length ? ")" : ""));
		}

		String[] paramsKeyArr = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			paramsKeyArr[i] = methodFullName + ".param" + i + "(" + parameterTypes[i].getName() + ")";
		}
		return paramsKeyArr;
	}

	/**
	 * 取得指定类的位置信息
	 * 
	 * @param aclass
	 * @return
	 */
	public static URL getUrlByClass(Class<?> aclass) {
		if (aclass == null) {
			return null;
		}

		ProtectionDomain protectionDomain = aclass.getProtectionDomain();
		if (protectionDomain == null) {
			return null;
		}

		CodeSource codeSource = protectionDomain.getCodeSource();
		if (codeSource == null) {
			return null;
		}

		return codeSource.getLocation();
	}

	/**
	 * 获取方法调用信息
	 * 
	 * @param methodName
	 *            方法名称
	 * @param args
	 *            参数数组
	 * @return
	 */
	public static String getMethodCallInfo(String methodName, Object[] args) {
		StringBuffer callInfo = new StringBuffer();
		callInfo.append(methodName + "(");
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				callInfo.append(i > 0 ? "," : "");
				callInfo.append(String.valueOf(args[i]));
			}
		}
		callInfo.append(")");
		return callInfo.toString();
	}

	/**
	 * 获取方法签名信息(例如:test(java.lang.String,java.lang.Long))
	 * 
	 * @param mi
	 * @return
	 */
	public static String getMethodSign(Method method) {
		// 参数信息
		StringBuffer callInfo = new StringBuffer();
		callInfo.append(method.getName() + "(");
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			callInfo.append(i > 0 ? "," : "");
			callInfo.append(parameterTypes[i].getName());
		}
		callInfo.append(")");
		return callInfo.toString();
	}

	/**
	 * 取得完整的方法签名
	 * 
	 * @param method
	 *            (例如:com.sdo.transbutton.common.proxyfactroy.JmsClientProxy.
	 *            test (java.lang.String,java.lang.Long))
	 * @return
	 */
	public static String getMethodSignFull(Method method) {
		return method.getDeclaringClass().getName() + "." + getMethodSign(method);
	}

	/**
	 * 取得指定类型所有方法的签名列表
	 * 
	 * @param cla
	 * @return
	 */
	public static List<String> getMethodSignList(Class<?> cla) {
		Method[] methods = cla.getDeclaredMethods();
		List<String> methodSignList = new ArrayList<String>();
		for (Method method : methods) {
			methodSignList.add(getMethodSign(method));
		}
		return methodSignList;
	}

	/**
	 * 取得指定类型所有方法对应接口列表的映射
	 * 
	 * @param cla
	 * @return<方法签名,接口列表>
	 */
	public static Map<String, List<Class<?>>> getMethod2InterfaceMap(Class<?> cla) {
		Type[] genericInterfaces = cla.getGenericInterfaces();
		Map<String, List<Class<?>>> interfaceMethodSignMap = new HashMap<String, List<Class<?>>>();

		for (int i = 0; i < genericInterfaces.length; i++) {
			Class<?> gi = (Class<?>) genericInterfaces[i];
			Method[] dms = gi.getDeclaredMethods();
			for (Method method : dms) {
				String methodSign = getMethodSign(method);
				List<Class<?>> classList = interfaceMethodSignMap.get(methodSign);
				if (classList == null) {
					classList = new ArrayList<Class<?>>();
					interfaceMethodSignMap.put(methodSign, classList);
				}
				classList.add(gi);
			}
		}

		return interfaceMethodSignMap;
	}

	public static String getMethodSimpleName(Method method) {
		return getMethodSimpleName(null, method);
	}

	/**
	 * 取得方法简称(所属类无包名)
	 * 
	 * @param targetObj
	 * @param method
	 * @return
	 */
	public static String getMethodSimpleName(Object targetObj, Method method) {
		if (targetObj != null) {
			String objectFullClassName = getObjectFullClassName(targetObj);
			return objectFullClassName.substring(objectFullClassName.lastIndexOf(".") + 1) + "." + method.getName();
		} else {
			return method.getDeclaringClass().getSimpleName() + "." + method.getName();// 所属类名
		}
	}

	/**
	 * @param targetObj
	 * @return
	 */
	private static String getObjectFullClassName(Object targetObj) {
		String simpleName = targetObj.getClass().getSimpleName();
		String objStr = targetObj.toString();
		String regex_proxy = "\\$Proxy\\d+";
		int indexOf = objStr.indexOf("@");
		if (Pattern.matches(regex_proxy, simpleName) && indexOf != -1) {
			return objStr.substring(0, indexOf);
		} else {
			return simpleName;
		}

	}

	/**
	 * 取得一个方法的完整名称
	 * 
	 * @param method
	 * @return
	 */
	public static String getMethodFullName(Method method) {
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	/**
	 * 获取一个属性的get方法的名称
	 * 
	 * @param propertyName
	 * @return
	 */
	public static String getGetMethodName(String propertyName) {
		// 验证参数合法性
		Assert.notNull(propertyName, "无法取得属性[" + propertyName + "]对应的get方法的名称!");

		// 取得首支付大写形式
		String firstChar = String.valueOf(propertyName.charAt(0)).toUpperCase();
		return "get" + firstChar + propertyName.substring(1);
	}

	/**
	 * 获取一个属性的get方法的名称
	 * 
	 * @param propertyName
	 * @return
	 */
	public static String getSetMethodName(String propertyName) {
		// 验证参数合法性
		Assert.notNull(propertyName, "无法取得属性[" + propertyName + "]对应的get方法的名称!");

		// 取得首支付大写形式
		String firstChar = String.valueOf(propertyName.charAt(0)).toUpperCase();
		return "set" + firstChar + propertyName.substring(1);
	}

	/**
	 * 取得指定对象指定域的值
	 * 
	 * @param obj
	 * @param aField
	 * @return
	 */
	public static Object getFieldValue(Object obj, Field aField) {
		// 若当前域为常量域,则返回空信息
		boolean isConstantsField = isConstantsField(aField);
		if (isConstantsField) {
			return null;
		}

		// 获取域属性名和属性值
		Object fieldValue = null;
		try {
			aField.setAccessible(true);
			fieldValue = aField.get(obj);
		} catch (Exception e) {
			throw new RuntimeException("取得对象【" + obj + "】的域【" + aField + "】的值时发生异常：", e);
		}
		return fieldValue;
	}

	/**
	 * 取得指定对象的指定域的值
	 * 
	 * @param jp
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static Object getDeclaredFieldValue(Object jp, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Class<?> class1 = jp.getClass();
		Field aField = class1.getDeclaredField(fieldName);
		aField.setAccessible(true);
		Object fieldValue = aField.get(jp);
		return fieldValue;
	}

	/**
	 * 判断一个域是否为常量域
	 * 
	 * @param aField
	 * @return
	 */
	public static boolean isConstantsField(Field aField) {
		return isStaticField(aField) && isFinalField(aField);
	}

	/**
	 * 判断一个域是否为Final域
	 * 
	 * @param aField
	 * @return
	 */
	public static boolean isFinalField(Field aField) {
		int modifiers2 = aField.getModifiers();
		boolean isFinalField = Modifier.isFinal(modifiers2);
		return isFinalField;
	}

	/**
	 * 判断一个域是否为static域
	 * 
	 * @param aField
	 * @return
	 */
	public static boolean isStaticField(Field aField) {
		int modifiers = aField.getModifiers();
		boolean isStaticField = Modifier.isStatic(modifiers);
		return isStaticField;
	}

	/**
	 * 获取域的类型的泛型的类型
	 * 
	 * @param field
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public static Class<?> getFieldActualType(Field field) {
		Type genericType = field.getGenericType();
		if (!(genericType instanceof ParameterizedType)) {
			throw new RuntimeException("域【" + field + "】的类型不支持泛型！");
		}

		Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
		if (types.length > 1) {
			throw new RuntimeException("域【" + field + "】的类型具有多个泛型信息！");
		}

		return (Class<?>) types[0];
	}

	/**
	 * 将Object值设置到特定对象的特定属性中
	 * 
	 * @param aimObj
	 * @param field
	 * @param filedValue
	 */
	public static void setObjectValueToField(Object aimObj, Field field, Object filedValue) {
		try {
			if (filedValue != null) {
				field.setAccessible(true);
				field.set(aimObj, filedValue);
			}
		} catch (Exception e) {
			throw new RuntimeException("将属性[" + field.getName() + "]的值[" + filedValue + "]设置到对象[" + aimObj + "]时发生异常", e);
		}
	}

	/**
	 * 使用指定类加载器克隆一个目标对象
	 * 
	 * @param srcObj
	 * @param targetClass
	 * @return
	 */
	public static Object cloneObjectByClassLoad(Object srcObj, Class<?> targetClass) {
		try {
			Object newInstance = targetClass.newInstance();
			copyValue(srcObj, newInstance);
			logger.debug("将对象【" + srcObj + "(来自[" + getUrlByClass(srcObj.getClass()) + "])" + "】转化成了对象【" + newInstance + "(来自[" + getUrlByClass(newInstance.getClass()) + "])" + "】不相符，进行了类型转换！");
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public static Object newInstance(Class<?> targetClass) {
		try {
			return newAInstance(targetClass);
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public static Object newAInstance(Class<?> targetClass) throws Exception {
		try{
			Field module=targetClass.getDeclaredField("MODULE$");
			module.setAccessible(true);
			return module.get(null);
		}catch(Exception e){
		}

		// 默认构造函数不可用的情况下,寻找单例方法创建实例
		try {
			Method declaredMethod = targetClass.getDeclaredMethod("getInstance");
			if (Modifier.isStatic(declaredMethod.getModifiers())) {
				return declaredMethod.invoke(targetClass);
			}
		} catch (Exception e) {
		}
		
		// 有默认构造函数的情况下，优先使用默认构造函数创建实例
		try{
			Constructor<?> defaultConstructor = targetClass.getDeclaredConstructor();
			defaultConstructor.setAccessible(true);
			return defaultConstructor.newInstance();
		}catch(Exception e){
		}

		return null;
	}

	/**
	 * 拷贝对象信息
	 * 
	 * @param orig
	 *            源对象
	 * @param dest
	 *            目标对象
	 */
	public static void copyValue(Object orig, Object dest) {
		// 判断源对象为空的情况
		if (orig == null) {
			return;
		}

		Field[] origFields = orig.getClass().getDeclaredFields();
		Class<? extends Object> destClass = dest.getClass();
		for (Field field : origFields) {
			if (isFinalField(field) || isStaticField(field)) {
				continue;
			}

			// 取得源对象域值
			field.setAccessible(true);
			Object origValue = null;
			try {
				origValue = field.get(orig);
			} catch (Exception e) {
				throw new RuntimeException("从对象【" + orig + "】中取得域【" + field + "】的值时发生异常", e);
			}

			// 取得目标对象域信息
			Field destField;
			try {
				destField = destClass.getDeclaredField(field.getName());
			} catch (Exception e1) {
				throw new RuntimeException("", e1);
			}

			// 判断目标对象是否存在对应域
			if (destField == null) {
				throw new RuntimeException("未能找到对象【" + dest + "】的【" + field.getName() + "】域！");
			}

			// 对目标对象域进行设置值
			destField.setAccessible(true);
			try {
				destField.set(dest, origValue);
			} catch (Exception e) {
				throw new RuntimeException("向对象【" + dest + "】的【" + destField + "】域赋值【" + origValue + "】时发生异常", e);
			}
		}
	}


	public static void main(String[] args) {
	}

	/**
	 * 取得指定对象指定方法的相同方法引用(主要用于获取接口方法在指定对象所属类型的实现方法引用)
	 * 
	 * @param obj
	 * @param interfaceMethod
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getMethod(Object obj, Method interfaceMethod) {
		try {
			return obj.getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	/**
	 * 取得对象的类型信息
	 * 
	 * @param obj
	 * @return
	 */
	public static String getObjType(Object obj) {
		if (obj == null) {
			return CLASS_NAME_NULL;
		}
		return obj.getClass().getName();
	}

	/**
	 * 类型信息:null
	 */
	public static final String CLASS_NAME_NULL = "null";

	/**
	 * 判断两个类直接的父子关系
	 * 
	 * @param subClass
	 * @param supClass
	 * @return
	 */
	public static boolean asSubclass(Class<?> subClass, Class<?> supClass) {
		return supClass.isAssignableFrom(subClass);
	}

	/**
	 * 取得对象的类型信息
	 * 
	 * @param obj
	 * @return
	 */
	public static Class<?> getClass(Object obj) {
		if (obj == null) {
			return null;
		}

		return obj.getClass();
	}


	/**
	 * 获取类的泛型类型
	 * 
	 * @param ownerClass
	 * @param idx
	 * @return
	 */
	public static Class<?> getGenericType(Class<?> ownerClass, int idx) {
		Type genericSuperclass = ownerClass.getGenericSuperclass();
		if (!(genericSuperclass instanceof ParameterizedType)) {
			throw new RuntimeException("宿主类【" + ownerClass + "】的父类型【" + genericSuperclass + "】不是泛型类型！");
		}
		Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
		if (idx > actualTypeArguments.length) {
			throw new RuntimeException("指定索引【" + idx + "】大于泛型数量【" + actualTypeArguments.length + "】");
		}
		return (Class<?>) actualTypeArguments[idx];
	}

	public static Class<?> getArrayOrigClass(Class<?> dtoClass) {
		if (!dtoClass.isArray()) {
			return dtoClass;
		}

		String name = dtoClass.getName();
		String replace = name.replace("[", "").replace(";", "");
		String origClassName = replace;
		if (replace.startsWith("L")) {
			origClassName = replace.substring(1);
		}
		try {
			return Class.forName(origClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("为能找到类型【" + origClassName + "】！");
		}
	}

}
