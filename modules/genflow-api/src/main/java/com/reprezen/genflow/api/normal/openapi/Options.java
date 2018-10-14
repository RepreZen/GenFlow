package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.ADDITIONAL_FILES;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.HOIST;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.INLINE;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.RETAIN;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reprezen.genflow.api.normal.openapi.Option.ExtensionData;
import com.reprezen.genflow.api.normal.openapi.Option.HoistType;
import com.reprezen.genflow.api.normal.openapi.Option.OptionType;
import com.reprezen.genflow.api.normal.openapi.Option.OrderingScheme;
import com.reprezen.genflow.api.normal.openapi.Option.RetentionScopeType;

public class Options {
	private final Integer modelVersion;
	private final Map<Option.OptionType, Object> options = Maps.newHashMap();
	private Reference topRef = null;
	private final List<String> inScopeUrlStrings = Lists.newArrayList();

	public Options(Integer modelVersion, Option... options) {
		this.modelVersion = modelVersion;
		for (Option option : options) {
			this.options.put(option.getType(), option.getData());
		}
	}

	public Integer getModelVersion() {
		return modelVersion;
	}

	public void replace(Option option) {
		options.put(option.getType(), option.getData());
	}

	public Object getOptionData(Option.OptionType optionType) {
		return options.get(optionType);
	}

	public boolean isDoNotNormalize() {
		return isOptionSet(OptionType.DO_NOT_NORMALIZE);
	}

	public boolean isInlined(ObjectType type) {
		if (type == ObjectType.PATH) {
			// paths must always be inlined
			return true;
		} else {
			return optionDataHasValue(INLINE, type);
		}
	}

	public boolean isRetained(ObjectType objectType, boolean hasPaths) {
		if (options.get(RETAIN) == Option.PATH_OR_COMPONENTS) {
			return hasPaths == (objectType == ObjectType.PATH);
		} else {
			return optionDataHasValue(RETAIN, objectType);
		}
	}

	public List<String> getAdditionalFileStrings() {
		List<String> results = Lists.newArrayList();
		Object data = options.get(ADDITIONAL_FILES);
		if (data instanceof Collection<?>) {
			Collection<?> list = (Collection<?>) data;
			for (Object obj : list) {
				if (obj instanceof String) {
					results.add((String) obj);
				}
			}
		}
		return results;
	}

	public List<URL> getAdditionalFileUrls() {
		List<URL> results = Lists.newArrayList();
		for (String urlString : getAdditionalFileStrings()) {
			results.add(new Reference(fixFile(urlString), topRef, modelVersion).getUrl());
		}
		return results;
	}

	// additional files will typically be file paths, rather than URLs, and in
	// that case we need to urlencode special chars. Except the built-in
	// encoding turns spaces into plus signs, which end up being incorrectly
	// treated as plus signs - not spaces - when Java later attempts to
	// resolve the resulting file: URL to a file path. So we pre-convert
	// spaces to %20 before performing url-encoding.
	private String fixFile(String url) {
		if (!looksLikeFilePath(url)) {
			return url;
		}
		String[] segments = url.split("[/\\\\]");
		String fixed = Stream.of(segments).map(s -> encodeSegment(s)).collect(Collectors.joining("/"));
		return fixed;
	}

	// An additional file string is treated as a URL if it doesn't "look like"
	// a simple file path. Any of the following doesn't "look like" a file:
	// * beginning with something that looks like a scheme, of length > 1 to avoid
	// counting drive letters)
	// * containing any urlencoded chars (% + 2 hex digits)
	private boolean looksLikeFilePath(String url) {
		return !url.matches("\\p{Alpha}[\\p{Alpha}\\d+.-]+:.*") //
				&& !url.matches(".*%\\p{XDigit}{2}");
	}

	private String encodeSegment(String s) {
		// we can't just convert spaces to %20 before URLEncode - that would
		// end up encoded as %2520. So we need to process space-separated parts
		// individually then join them with encoded spaces.
		String[] parts = s.split(" ");
		return Stream.of(parts).map(new Function<String, String>() {
			@Override
			public String apply(String s) {
				try {
					return URLEncoder.encode(s, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return s;
				}
			}

		}).collect(Collectors.joining("%20"));
	}

	public void resolveScope(String topUrlString) throws MalformedURLException {
		inScopeUrlStrings.clear();
		inScopeUrlStrings.add(topUrlString);
		topRef = new Reference(topUrlString, modelVersion);
		URL topUrl = topRef.getUrl();
		for (URL additionalUrl : getAdditionalFileUrls()) {
			URL resolved = new URL(topUrl, additionalUrl.toString());
			inScopeUrlStrings.add(resolved.toString());
		}
	}

	public boolean isUrlInScope(String urlString) {
		return options.get(OptionType.RETENTION_SCOPE) == RetentionScopeType.ALL
				|| inScopeUrlStrings.contains(urlString);
	}

	public boolean isHoistMediaTypes() {
		return optionDataHasValue(HOIST, HoistType.MEDIA_TYPE) && !isDoNotNormalize();
	}

	public boolean isHoistParameters() {
		return optionDataHasValue(HOIST, HoistType.PARAMETER) && !isDoNotNormalize();
	}

	public boolean isHoistSecurityRequirements() {
		return optionDataHasValue(HOIST, HoistType.SECURITY_REQUIREMENT) && !isDoNotNormalize();
	}

	public boolean isRewriteSimpleRefs() {
		return isOptionSet(OptionType.REWRITE_SIMPLE_REFS);
	}

	public boolean isCreateDefTitles() {
		return isOptionSet(OptionType.CREATE_DEF_TITLES) && !isDoNotNormalize();
	}

	public boolean isInstantiateNullTypes() {
		return isOptionSet(OptionType.INSTANTIATE_NULL_COLLECTIONS) && !isDoNotNormalize();
	}

	public boolean isFixMissingTypes() {
		return isOptionSet(OptionType.FIX_MISSING_TYPES) && !isDoNotNormalize();
	}

	public boolean isAddJsonPointers() {
		return isOptionSet(OptionType.ADD_JSON_POINTERS) && !isDoNotNormalize();
	}

	public boolean isSortedOrdering() {
		return optionDataIs(OptionType.ORDERING, OrderingScheme.SORTED);
	}

	public boolean isAsDeclaredOrdering() {
		return optionDataIs(OptionType.ORDERING, OrderingScheme.AS_DECLARED);
	}

	public boolean isFixXExamples() {
		return isOptionSet(OptionType.FIX_X_EXAMPLES) && !isDoNotNormalize();
	}

	public boolean isRetainAllExtensionData() {
		return isRetainOrderingExtensionData() && isRetainPointerExtensionData() && isRetainFileExtensionData()
				&& isRetainTypeNameExtensionData() && isRetainBadRefExtensionData();
	}

	public boolean isRetainOrderingExtensionData() {
		return optionDataHasValue(OptionType.RETAIN_EXTENSION_DATA, ExtensionData.ORDERING)
				// maintain backward compatibility
				|| isOptionSet(OptionType.RETAIN_POSITION_VALUES);
	}

	public boolean isRetainPointerExtensionData() {
		return optionDataHasValue(OptionType.RETAIN_EXTENSION_DATA, ExtensionData.POINTER);
	}

	public boolean isRetainFileExtensionData() {
		return optionDataHasValue(OptionType.RETAIN_EXTENSION_DATA, ExtensionData.FILE);
	}

	public boolean isRetainTypeNameExtensionData() {
		return optionDataHasValue(OptionType.RETAIN_EXTENSION_DATA, ExtensionData.TYPE_NAME);
	}

	public boolean isRetainBadRefExtensionData() {
		return optionDataHasValue(OptionType.RETAIN_EXTENSION_DATA, ExtensionData.BAD_REF);
	}

	public boolean isDeferExtensionDataRemoval() {
		return isOptionSet(OptionType.DEFER_EXTENSION_DATA_REMOVAL);
	}

	public boolean isOptionSet(OptionType optionType) {
		Object data = options.get(optionType);
		if (data != null && data instanceof Boolean) {
			return (Boolean) data;
		} else {
			return false;
		}
	}

	private <T> boolean optionDataIs(OptionType optionType, T value) {
		Object data = options.get(optionType);
		return data == value;
	}

	private <T> boolean optionDataHasValue(OptionType optionType, T value) {
		Object data = options.get(optionType);
		if (data instanceof Collection<?>) {
			return ((Collection<?>) data).contains(value);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		List<String> opts = Lists.newArrayList();
		for (Entry<OptionType, Object> entry : options.entrySet()) {
			opts.add(new Option(entry.getKey(), entry.getValue()).toString());
		}
		return "[" + StringUtils.join(opts, ", \n") + "]";
	}
}