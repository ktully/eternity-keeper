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
