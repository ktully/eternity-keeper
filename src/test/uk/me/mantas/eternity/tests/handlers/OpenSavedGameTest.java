package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.handlers.OpenSavedGame;
import uk.me.mantas.eternity.tests.TestHarness;
import uk.me.mantas.eternity.tests.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class OpenSavedGameTest extends TestHarness {
	@Test
	public void requestFileNotExists () {
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		OpenSavedGame cls = new OpenSavedGame();

		cls.onQuery(mockBrowser, 0, "404", false, mockCallback);
		verify(mockCallback).success("{\"error\":\"NOT_EXISTS\"}");
	}

	@Test
	public void requestFileExists ()
		throws NoSuchFieldException
		, IllegalAccessException
		, IOException {

		Environment mockEnvironment = TestUtils.mockEnvironment();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		ExecutorService mockWorkers = mock(ExecutorService.class);
		OpenSavedGame cls = new OpenSavedGame();

		when(mockEnvironment.getWorkers()).thenReturn(mockWorkers);

		File tmp = File.createTempFile(PREFIX, "OpenSavedGameTest");
		cls.onQuery(mockBrowser, 0, tmp.getAbsolutePath(), false, mockCallback);

		verify(mockWorkers).execute(anyObject());
	}
}
