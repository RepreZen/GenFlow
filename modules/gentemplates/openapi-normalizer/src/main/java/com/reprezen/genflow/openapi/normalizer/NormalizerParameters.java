package com.reprezen.genflow.openapi.normalizer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.normal.openapi.ObjectType;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Option.ExtensionData;
import com.reprezen.genflow.api.normal.openapi.Option.HoistType;
import com.reprezen.genflow.api.normal.openapi.Option.OptionType;
import com.reprezen.genflow.api.normal.openapi.Option.OrderingScheme;
import com.reprezen.genflow.api.normal.openapi.Option.RetentionScopeType;

public class NormalizerParameters {

	private Map<String, Object> params;

	public NormalizerParameters(Map<String, Object> params) {
		this.params = params;
	}

	public Option[] getOptions() throws BadParameterException {
		Map<OptionType, Object> options = Maps.newLinkedHashMap();
		for (Entry<String, Object> param : params.entrySet()) {
			OptionType type = getOptionType(param.getKey());
			if (type != null) {
				Object data = getOptionData(param.getValue(), type);
				if (data != null) {
					options.put(type, new Option(type, data));
				}
			}
		}
		// the generator does extension removal after applying requested ordering, so we
		// need the normalizer
		// to skip it. This option is _not_ made available as a gentemplate parameter.
		options.put(OptionType.DEFER_EXTENSION_DATA_REMOVAL, new Option(OptionType.DEFER_EXTENSION_DATA_REMOVAL, true));
		return options.values().toArray(new Option[options.size()]);
	}

	private OptionType getOptionType(String name) {
		try {
			return OptionType.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private Object getOptionData(Object data, OptionType type) throws BadParameterException {
		try {
			switch (type) {
			case INLINE:
				return getInlineData(data);
			case RETAIN:
				return getRetainData(data);
			case RETENTION_SCOPE:
				return getRetentionScopeData(data);
			case ADDITIONAL_FILES:
				return getStringListData(data);
			case HOIST:
				return getHoistData(data);
			case REWRITE_SIMPLE_REFS:
				return getBooleanData(data);
			case CREATE_DEF_TITLES:
				return getBooleanData(data);
			case INSTANTIATE_NULL_COLLECTIONS:
				return getBooleanData(data);
			case FIX_MISSING_TYPES:
				return getBooleanData(data);
			case ADD_JSON_POINTERS:
				return getBooleanData(data);
			case ORDERING:
				return getOrderingData(data);
			case RETAIN_POSITION_VALUES:
				return getBooleanData(data);
			case RETAIN_EXTENSION_DATA:
				return getExtensionRetentionData(data);
			case FIX_X_EXAMPLES:
				return getBooleanData(data);
			default:
				throw new BadParameterException("Unhandled parameter: " + type.name());
			}
		} catch (BadParameterException e) {
			throw new BadParameterException("Invalid value for parameter " + type.name() + ": " + e.getMessage());
		}
	}

	private static Map<String, Collection<ObjectType>> specialInlineTypes = Maps.newHashMap();
	static {
		specialInlineTypes.put("ALL", Option.ALL_OBJECTS);
		specialInlineTypes.put("COMPONENTS", Option.COMPONENT_OBJECTS);
		specialInlineTypes.put("NONE", Option.NO_OBJECTS);
	}

	private Set<ObjectType> getInlineData(Object data) throws BadParameterException {
		return getEnumListData(data, ObjectType.class, specialInlineTypes);
	}

	private static Map<String, Collection<ObjectType>> specialRetainTypes = Maps.newHashMap();
	static {
		specialRetainTypes.put("ALL", Option.ALL_OBJECTS);
		specialRetainTypes.put("COMPONENTS", Option.COMPONENT_OBJECTS);
	}

	private Set<ObjectType> getRetainData(Object data) throws BadParameterException {
		return getEnumListData(data, ObjectType.class, specialRetainTypes);
	}

	private RetentionScopeType getRetentionScopeData(Object data) throws BadParameterException {
		return getEnumData(data, RetentionScopeType.class);
	}

	private static Map<String, Collection<HoistType>> specialHoistTypes = Maps.newHashMap();
	static {
		specialHoistTypes.put("ALL", Option.ALL_HOIST_TYPES);
		specialHoistTypes.put("NONE", Option.NO_HOIST_TYPES);
	}

	private Set<HoistType> getHoistData(Object data) throws BadParameterException {
		return getEnumListData(data, HoistType.class, specialHoistTypes);
	}

	private OrderingScheme getOrderingData(Object data) throws BadParameterException {
		return getEnumData(data, OrderingScheme.class);
	}

	private static Map<String, Collection<ExtensionData>> specialExtensionRetentionTypes = Maps.newHashMap();
	static {
		specialExtensionRetentionTypes.put("ALL", Option.ALL_EXTENSION_DATA);
		specialExtensionRetentionTypes.put("NONE", Option.NO_EXTENSION_DATA);
	}

	private Set<ExtensionData> getExtensionRetentionData(Object data) throws BadParameterException {
		return getEnumListData(data, ExtensionData.class, specialExtensionRetentionTypes);
	}

	private <T extends Enum<T>> Set<T> getEnumListData(Object data, Class<T> cls, Map<String, Collection<T>> specials)
			throws BadParameterException {
		Set<T> results = Sets.newHashSet();
		if (data instanceof Collection<?>) {
			Collection<?> list = (Collection<?>) data;
			for (Object value : list) {
				results.add(getEnumData(value, cls));
			}
		} else if (data instanceof String) {
			String string = (String) data;
			if (specials != null && specials.containsKey(string)) {
				results.addAll(specials.get(string));
			} else {
				results.add(getEnumData(data, cls));
			}
		} else {
			throw new BadParameterException("Expected a list of " + cls.getSimpleName() + " enum members, got", data);
		}
		return results;
	}

	private <T extends Enum<T>> T getEnumData(Object data, Class<T> cls) throws BadParameterException {
		if (data instanceof String) {
			String string = (String) data;
			Method valueOf;
			try {
				valueOf = cls.getMethod("valueOf", String.class);
				@SuppressWarnings("unchecked")
				T eValue = (T) valueOf.invoke(null, string);
				return eValue;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
			}
		}
		throw new BadParameterException("Expected name of " + cls.getSimpleName() + " enum member, got", data);
	}

	private List<String> getStringListData(Object data) throws BadParameterException {
		List<String> results = Lists.newArrayList();
		if (data instanceof Collection<?>) {
			Collection<?> list = (Collection<?>) data;
			for (Object value : list) {
				if (value instanceof String) {
					results.add((String) value);
				} else {
					throw new BadParameterException("Expected a string, got: ", data);
				}
			}
		} else if (data instanceof String) {
			results.add((String) data);
		} else {
			throw new BadParameterException("Expected a string array");
		}
		return results;
	}

	private Boolean getBooleanData(Object data) throws BadParameterException {
		if (data instanceof Boolean) {
			return (Boolean) data;
		} else if (data instanceof String) {
			String value = ((String) data).trim().toLowerCase();
			if (value.equals("true") || value.equals("false")) {
				return Boolean.valueOf(value);
			}
		}
		throw new BadParameterException("Invalid boolean value");
	}

	public static class BadParameterException extends Exception {
		private static final long serialVersionUID = 1L;

		public BadParameterException(String msg) {
			super(msg);
		}

		public BadParameterException(String msg, Object data) {
			this(msg + ": " + String.valueOf(data));
		}
	}
}
