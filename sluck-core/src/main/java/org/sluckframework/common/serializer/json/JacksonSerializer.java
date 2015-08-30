package org.sluckframework.common.serializer.json;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.YearMonth;
import org.sluckframework.common.serializer.AnnotationRevisionResolver;
import org.sluckframework.common.serializer.ChainingConverterFactory;
import org.sluckframework.common.serializer.ConverterFactory;
import org.sluckframework.common.serializer.RevisionResolver;
import org.sluckframework.common.serializer.SerializationException;
import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.SerializedType;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.serializer.SimpleSerializedObject;
import org.sluckframework.common.serializer.SimpleSerializedType;
import org.sluckframework.common.serializer.UnknownSerializedTypeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 使用 jackson 序列化 对象 为 json形式
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:18:05
 * @since 1.0
 */
public class JacksonSerializer implements Serializer {

	private final RevisionResolver revisionResolver;
	private final ConverterFactory converterFactory;
	private final ObjectMapper objectMapper;
	private final ClassLoader classLoader;

	/**
	 * 使用 AnnotationRevisionResolver ChainingConverterFactory 初始化
	 */
	public JacksonSerializer() {
		this(new AnnotationRevisionResolver(), new ChainingConverterFactory());
	}

	/**
	 * 使用jackjson 的objectMapper 初始化
	 *
	 * @param objectMapper The objectMapper to serialize objects and parse JSON with
	 */
	public JacksonSerializer(ObjectMapper objectMapper) {
		this(objectMapper, new AnnotationRevisionResolver(), new ChainingConverterFactory());
	}

	/**
	 * 使用指定的 RevisionResolver ConverterFactory 初始化
	 * @param revisionResolve The strategy to use to resolve the revision of an object
	 * @param converterFactory The factory providing the converter instances for upcasters
	 */
	public JacksonSerializer(RevisionResolver revisionResolver, ConverterFactory converterFactory) {
		this(new ObjectMapper(), revisionResolver, converterFactory);
	}

	/**
	 * 使用 ObjectMapper 和 RevisionResolver 初始化
	 * @param objectMapper The objectMapper to serialize objects and parse JSON with
	 * @param revisionResolver The strategy to use to resolve the revision of an object
	 */
	public JacksonSerializer(ObjectMapper objectMapper, RevisionResolver revisionResolver) {
		this(objectMapper, revisionResolver, new ChainingConverterFactory());
	}

	/**
	 * 使用 ObjectMapper  RevisionResolver ConverterFactory 初始化
	 *
	 * @param objectMapper
	 * @param revisionResolver
	 * @param converterFactory
	 */
	public JacksonSerializer(ObjectMapper objectMapper,
			RevisionResolver revisionResolver, ConverterFactory converterFactory) {
		this(objectMapper, revisionResolver, converterFactory, null);
	}

	/**
	 * 使用指定属性初始化，并 增加 DateTime 的 初始化与解析 到 ObjectMapper
	 *
	 * @param objectMapper
	 * @param revisionResolver
	 * @param converterFactory
	 * @param classLoader
	 */
	public JacksonSerializer(ObjectMapper objectMapper,
			RevisionResolver revisionResolver,
			ConverterFactory converterFactory, ClassLoader classLoader) {
		this.revisionResolver = revisionResolver;
		this.converterFactory = converterFactory;
		this.objectMapper = objectMapper;
		this.classLoader = classLoader == null ? getClass().getClassLoader(): classLoader;
		this.objectMapper.registerModule(new SimpleModule("Sluck-Jackson Module")
						 .addSerializer(ReadableInstant.class, new ToStringSerializer())
						 .addDeserializer(DateTime.class, new JodaDeserializer<DateTime>(DateTime.class))
						 .addDeserializer(Instant.class, new JodaDeserializer<Instant>(Instant.class))
						 .addDeserializer(MutableDateTime.class, new JodaDeserializer<MutableDateTime>(
										  MutableDateTime.class))
						 .addDeserializer(YearMonth.class, new JodaDeserializer<YearMonth>(YearMonth.class))
						 .addDeserializer(MonthDay.class, new JodaDeserializer<MonthDay>(MonthDay.class))
						 .addDeserializer(LocalDate.class, new JodaDeserializer<LocalDate>(LocalDate.class))
						 .addDeserializer(LocalTime.class, new JodaDeserializer<LocalTime>(LocalTime.class))
						 .addDeserializer(LocalDateTime.class, new JodaDeserializer<LocalDateTime>(
										  LocalDateTime.class)));
		if (converterFactory instanceof ChainingConverterFactory) {
			registerConverters((ChainingConverterFactory) converterFactory);
		}
	}

	protected void registerConverters(ChainingConverterFactory converterFactory) {
		converterFactory.registerConverter(new JsonNodeToByteArrayConverter(
				objectMapper));
		converterFactory.registerConverter(new ByteArrayToJsonNodeConverter(
				objectMapper));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> SerializedObject<T> serialize(Object object,
			Class<T> expectedRepresentation) {

		try {
			if (String.class.equals(expectedRepresentation)) {
				// noinspection unchecked
				return new SimpleSerializedObject<T>((T) getWriter()
						.writeValueAsString(object), expectedRepresentation,
						typeForClass(object.getClass()));
			}

			byte[] serializedBytes = getWriter().writeValueAsBytes(object);
			T serializedContent = converterFactory.getConverter(byte[].class,
					expectedRepresentation).convert(serializedBytes);
			return new SimpleSerializedObject<T>(serializedContent,
					expectedRepresentation, typeForClass(object.getClass()));
		} catch (JsonProcessingException e) {
			throw new SerializationException("Unable to serialize object", e);
		}
	}

	/**
	 * 返回 jackjson 的 objectMapper
	 *
	 * @return the ObjectMapper
	 */
	public final ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * objectMapper wirter
	 *
	 * @return The writer to serialize objects with
	 */
	protected ObjectWriter getWriter() {
		return objectMapper.writer();
	}

	/**
	 * objectMapper reader
	 *
	 * @param type The type of object to create a reader for
	 * @return The writer to serialize objects with
	 */
	protected ObjectReader getReader(Class<?> type) {
		return objectMapper.readerFor(type);
	}

	@Override
	public <T> boolean canSerializeTo(Class<T> expectedRepresentation) {
		return JsonNode.class.equals(expectedRepresentation)
				|| String.class.equals(expectedRepresentation)
				|| converterFactory.hasConverter(byte[].class,
						expectedRepresentation);
	}

	@Override
	public <S, T> T deserialize(SerializedObject<S> serializedObject) {
		try {
			if (JsonNode.class.equals(serializedObject.getContentType())) {
				return getReader(classForType(serializedObject.getType()))
						.readValue((JsonNode) serializedObject.getData());
			}
			SerializedObject<byte[]> byteSerialized = converterFactory
					.getConverter(serializedObject.getContentType(),byte[].class).convert(serializedObject);
			return getReader(classForType(serializedObject.getType()))
					.readValue(byteSerialized.getData());
		} catch (IOException e) {
			throw new SerializationException(
					"Error while deserializing object", e);
		}
	}

	@Override
	public Class<?> classForType(SerializedType type)
			throws UnknownSerializedTypeException {
		try {
			return classLoader.loadClass(resolveClassName(type));
		} catch (ClassNotFoundException e) {
			throw new UnknownSerializedTypeException(type, e);
		}
	}

	protected String resolveClassName(SerializedType serializedType) {
		return serializedType.getName();
	}

	@Override
	public SerializedType typeForClass(Class<?> type) {
		return new SimpleSerializedType(type.getName(),
				revisionResolver.revisionOf(type));
	}

	@Override
	public ConverterFactory getConverterFactory() {
		return converterFactory;
	}

}
