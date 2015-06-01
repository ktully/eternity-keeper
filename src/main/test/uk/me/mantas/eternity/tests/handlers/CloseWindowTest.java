package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.WindowCloser;
import uk.me.mantas.eternity.handlers.CloseWindow;
import uk.me.mantas.eternity.tests.TestHarness;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CloseWindowTest extends TestHarness {
	@Test
	public void closesWindow ()
		throws NoSuchFieldException
		, IllegalAccessException {

		Environment environment = Environment.getInstance();
		ExecutorService mockWorkers = mock(ExecutorService.class);
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		JFrame mockFrame = mock(JFrame.class);

		ArgumentCaptor<WindowCloser> executeArg =
			ArgumentCaptor.forClass(WindowCloser.class);

		Field frameField = WindowCloser.class.getDeclaredField("frame");
		Field workersField = Environment.class.getDeclaredField("workers");
		frameField.setAccessible(true);
		workersField.setAccessible(true);
		workersField.set(environment, mockWorkers);

		assertFalse(environment.closing);

		CloseWindow cls = new CloseWindow(mockFrame);
		cls.onQuery(mockBrowser, 0, "true", false, mockCallback);

		assertTrue(environment.closing);
		verify(mockCallback).success("true");
		verify(mockWorkers).execute(executeArg.capture());

		assertEquals(mockFrame, frameField.get(executeArg.getValue()));
	}
}
