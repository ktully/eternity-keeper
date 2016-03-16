package uk.me.mantas.eternity.handlers;

import com.google.common.reflect.ClassPath.ClassInfo;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONObject;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GetGameStructures extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(GetGameStructures.class);

	@Override
	public boolean onQuery (
		final CefBrowser browser
		, final long id
		, final String request
		, final boolean persistent
		, final CefQueryCallback callback) {

		final Environment environment = Environment.getInstance();
		final Set<ClassInfo> wrappedClasses =
			environment.classFinder().allClassesInPackage(environment.config().gameStructuresPkg());
		final Class<?>[] classes =
			wrappedClasses.stream().map(ClassInfo::load).toArray(Class[]::new);
		final Set<Class<?>> enums = findEnums(classes);

		callback.success(enumsToJSON(enums));
		return true;
	}

	private static Set<Class<?>> findEnums (final Class<?>[] haystack) {
		final Set<Class<?>> enums = new HashSet<>();
		for (final Class<?> cls : haystack) {
			if (cls.isEnum()) {
				enums.add(cls);
			}

			enums.addAll(findEnums(cls.getDeclaredClasses()));
		}

		return enums;
	}

	private static String enumsToJSON (final Set<Class<?>> enums) {
		final JSONObject json = new JSONObject();
		for (final Class<?> enm : enums) {
			final String[] constants =
				Arrays.stream(enm.getEnumConstants())
					.map(GetGameStructures::enumConstantName)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.toArray(String[]::new);
			json.put(enm.getName(), constants);
		}

		return json.toString();
	}

	private static Optional<String> enumConstantName (final Object constant) {
		final Class<?> cls = constant.getClass();

		try {
			final Method nameMethod = cls.getMethod("name");
			final Object result = nameMethod.invoke(constant);

			if (result instanceof String) {
				return Optional.of((String) result);
			}
		} catch (final NoSuchMethodException
			| IllegalAccessException
			| InvocationTargetException ignore) {}

		logger.error("Unable to extract enum constant name for %s.", cls.getName());
		return Optional.empty();
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		logger.error("Query #%d cancelled.", id);
	}
}
