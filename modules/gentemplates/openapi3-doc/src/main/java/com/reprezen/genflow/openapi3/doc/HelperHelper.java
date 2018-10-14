package com.reprezen.genflow.openapi3.doc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.IGenTemplateContext;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public class HelperHelper {

	private static ThreadLocal<HelperHelper> instance = new ThreadLocal<HelperHelper>();

	private static final List<Class<? extends Helper>> helperClasses = Arrays.asList( //
			AttributeHelper.class, //
			ArrayHelper.class, //
			DocHelper.class, //
			ExamplesHelper.class, //
			HtmlHelper.class, //
			MiscHelper.class, //
			ModelHelper.class, //
			OptionHelper.class, //
			ParameterHelper.class, //
			PropertyHelper.class, //
			RecursionHelper.class, //
			RefHelper.class, //
			ResponseHelper.class, //
			SchemaHelper.class, //
			TagHelper.class //
	);

	private OpenApi3 model;
	private IGenTemplateContext context;
	private Map<Class<? extends Helper>, Helper> helpers = Maps.newHashMap();

	private HelperHelper(OpenApi3 model, IGenTemplateContext context) {
		this.model = model;
		this.context = context;
	}

	public static void open(OpenApi3 model, IGenTemplateContext context) throws GenerationException {
		if (instance.get() != null) {
			throw new IllegalStateException("HelperHelper is already active");
		}
		HelperHelper helperHelper = new HelperHelper(model, context);
		instance.set(helperHelper);
		for (Class<? extends Helper> helperClass : helperClasses) {
			try {
				helperHelper.helpers.put(helperClass, helperClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new GenerationException("Failed to instantiate helper of type " + helperClass.getSimpleName(), e);
			}
		}
		for (Helper helper : helperHelper.helpers.values()) {
			helper.init();
		}
	}

	public static void close() {
		instance.remove();
	}

	public static <T> T getHelper(Class<T> helperClass) {
		@SuppressWarnings("unchecked")
		T result = (T) instance.get().helpers.get(helperClass);
		return result;
	}

	public static OpenApi3 getModel() {
		return instance.get().model;
	}

	public static IGenTemplateContext getContext() {
		return instance.get().context;
	}

	public static AttributeHelper getAttributeHelper() {
		return getHelper(AttributeHelper.class);
	}

	public static ArrayHelper getArrayHelper() {
		return getHelper(ArrayHelper.class);
	}

	public static DocHelper getDocHelper() {
		return getHelper(DocHelper.class);
	}

	public static ExamplesHelper getExamplesHelper() {
		return getHelper(ExamplesHelper.class);
	}

	public static HtmlHelper getHtmlHelper() {
		return getHelper(HtmlHelper.class);
	}

	public static MiscHelper getMiscHelper() {
		return getHelper(MiscHelper.class);
	}

	public static ModelHelper getModelHelper() {
		return getHelper(ModelHelper.class);
	}

	public static OptionHelper getOptionHelper() {
		return getHelper(OptionHelper.class);
	}

	public static ParameterHelper getParameterHelper() {
		return getHelper(ParameterHelper.class);
	}

	public static PropertyHelper getPropertyHelper() {
		return getHelper(PropertyHelper.class);
	}

	public static RecursionHelper getRecursionHelper() {
		return getHelper(RecursionHelper.class);
	}

	public static RefHelper getRefHelper() {
		return getHelper(RefHelper.class);
	}

	public static ResponseHelper getResponseHelper() {
		return getHelper(ResponseHelper.class);
	}

	public static SchemaHelper getSchemaHelper() {
		return getHelper(SchemaHelper.class);
	}

	public static TagHelper getTagHelper() {
		return getHelper(TagHelper.class);
	}
}
