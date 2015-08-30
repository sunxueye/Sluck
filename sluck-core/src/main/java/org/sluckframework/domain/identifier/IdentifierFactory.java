package org.sluckframework.domain.identifier;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 标识符工厂，用来生成标识符, 可以继承此工厂来实现自己的标识符，也可以使用默认的UUID instance来产生
 * 使用ServiceLoader机制来加载工厂实现类，如果没有使用默认
 * 想使用自定义的工厂，在META-INF/services 报下新建 org.sluckframework.domain.IdentifierFactory文件
 * 在里面定义类的全称名进行加载工厂类
 * 
 * @author sunxy
 * @time 2015年8月28日 上午11:46:04
 * @since 1.0
 */
public abstract class IdentifierFactory {

	private static final Logger logger = LoggerFactory.getLogger(IdentifierFactory.class);
	private static final IdentifierFactory INSTANCE;

	static {
		logger.debug("Looking for IdentifierFactory implementation using the context class loader");
		IdentifierFactory factory = locateFactories(Thread.currentThread().getContextClassLoader(), "Context");
		if (factory == null) {
			logger.debug("Looking for IdentifierFactory implementation using the IdentifierFactory class loader.");
			factory = locateFactories(IdentifierFactory.class.getClassLoader(), "IdentifierFactory");
		}
		// 如果没有实现，使用默认的UUID来生成标识
		if (factory == null) {
			factory = new IdentifierFactory() {
				@SuppressWarnings("unchecked")
				@Override
				public String generateIdentifier() {
					return UUID.randomUUID().toString();
				}
			};
			logger.debug("Using default UUID-based IdentifierFactory");
		} else {
			logger.info("Found custom IdentifierFactory implementation: {}",
					factory.getClass().getName());
		}
		INSTANCE = factory;
	}

	private static IdentifierFactory locateFactories(ClassLoader classLoader, String classLoaderName) {
		IdentifierFactory found = null;
		// 使用classLoader加载实现指定接口或继承指定类的集合
		Iterator<IdentifierFactory> services = ServiceLoader.load(IdentifierFactory.class, classLoader).iterator();
		if (services.hasNext()) {
			logger.debug(
					"Found IdentifierFactory implementation using the {} Class Loader", classLoaderName);
			found = services.next();
			if (services.hasNext()) {
				logger.warn( "More than one IdentifierFactory implementation was found using the {} "
								+ "Class Loader. This may result in different selections being made after "
								+ "restart of the application.",
						classLoaderName);
			}
		}
		return found;
	}

	public static IdentifierFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * 为实体或聚合产生唯一标识符
	 * @return 自定义产生的标识符
	 */
	public abstract <T> T generateIdentifier();

}
