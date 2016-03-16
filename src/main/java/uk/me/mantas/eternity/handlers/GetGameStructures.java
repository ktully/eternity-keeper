package uk.me.mantas.eternity.handlers;

import com.google.common.reflect.ClassPath.ClassInfo;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;

import java.util.HashSet;
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
			environment.classFinder().allClassesInPackage("uk.me.mantas.eternity.game");
		final Class<?>[] classes =
			wrappedClasses.stream().map(ClassInfo::getClass).toArray(Class[]::new);
		final Set<Class<?>> enums = findEnums(classes);

		return true;
	}

	private Set<Class<?>> findEnums (final Class<?>[] haystack) {
		final Set<Class<?>> enums = new HashSet<>();
		for (final Class<?> cls : haystack) {
			if (cls.isEnum()) {
				enums.add(cls);
			}

			enums.addAll(findEnums(cls.getDeclaredClasses()));
		}

		return enums;
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		logger.error("Query #%d cancelled.", id);
	}
}
