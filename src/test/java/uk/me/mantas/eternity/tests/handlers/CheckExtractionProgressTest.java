package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.handlers.CheckExtractionProgress;
import uk.me.mantas.eternity.save.SaveGameExtractor;
import uk.me.mantas.eternity.tests.TestHarness;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;

public class CheckExtractionProgressTest extends TestHarness {
	private static final String RESPONSE = "{\"update\":%s}";

	@Test
	public void getCurrentSaveListerNull () {
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		CheckExtractionProgress cls = new CheckExtractionProgress();
		cls.onQuery(mockBrowser, 0, "true", false, mockCallback);

		verify(mockCallback).success(String.format(RESPONSE, "0"));
	}

	@Test
	public void extractorNull () {
		Environment environment = Environment.getInstance();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		SaveInfoLister mockLister = mock(SaveInfoLister.class);

		environment.setCurrentSaveLister(mockLister);
		CheckExtractionProgress cls = new CheckExtractionProgress();
		cls.onQuery(mockBrowser, 0, "true", false, mockCallback);

		verify(mockCallback).success(String.format(RESPONSE, "0"));
	}

	@Test
	public void calculatesPercentage () {
		Environment environment = Environment.getInstance();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		SaveInfoLister mockLister = mock(SaveInfoLister.class);
		SaveGameExtractor mockExtractor = mock(SaveGameExtractor.class);

		environment.setCurrentSaveLister(mockLister);
		mockLister.extractor = mockExtractor;
		mockExtractor.currentCount = new AtomicInteger(1);
		mockExtractor.totalFiles = new AtomicInteger(3);

		CheckExtractionProgress cls = new CheckExtractionProgress();
		cls.onQuery(mockBrowser, 0, "true", false, mockCallback);

		double percentage = (1d / 3d) * 100;
		verify(mockCallback).success(
			String.format(RESPONSE, String.format("%.14f", percentage)));
	}
}
