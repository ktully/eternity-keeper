/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.environment.Environment;
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

		environment.state().currentSaveLister(mockLister);
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

		environment.state().currentSaveLister(mockLister);
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
