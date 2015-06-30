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
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.handlers.OpenSavedGame;
import uk.me.mantas.eternity.tests.TestHarness;

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
	public void requestFileExists () throws IOException {
		Environment mockEnvironment = mockEnvironment();
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
