/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 the authors.
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


package uk.me.mantas.eternity;

import org.apache.commons.io.FileUtils;
import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;
import org.json.JSONObject;
import uk.me.mantas.eternity.environment.Environment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EternityKeeper extends JFrame {
	private CefApp cefApp;
	private CefClient cefClient;
	private CefBrowser browser;

	private EternityKeeper () {
		CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
			@Override
			public void stateHasChanged (CefAppState state) {
				if (state == CefAppState.TERMINATED) {
					shutdown();
				}
			}
		});

		final CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = OS.isLinux();
		settings.remote_debugging_port = 13002;

		cefApp = CefApp.getInstance(settings);
		cefClient = cefApp.createClient();
		JSHandlers.register(cefClient, this);

		String index = "/src/ui/index.html";
		if (OS.isLinux()) {
			index = new File("src/ui/index.html").getAbsolutePath();
		}

		browser = cefClient.createBrowser(String.format("file://%s", index), OS.isLinux(), false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
		pack();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent e) {
				if (Environment.getInstance().state().closing) {
					cefApp.dispose();
					dispose();
				} else {
					browser.executeJavaScript("checkForModifications();", browser.getURL(), 0);
				}
			}
		});
	}

	private void shutdown () {
		saveWindowState();
		Environment.getInstance().directory().deleteWorking();
		Environment.joinAllWorkers();
		System.exit(0);
	}

	private static void rolloverLogFile (final File log) {
		final long maxSize = Environment.getInstance().config().maxLogSize();
		if (!log.exists() || !log.isFile() || log.length() < maxSize) {
			return;
		}

		// We want to delete the first half of the file's lines.
		final long halfMax = maxSize / 2;
		final List<String> storedLines = new ArrayList<>();
		String line;
		long currentByteCount = 0L;

		try (final BufferedReader reader =
			new BufferedReader(new InputStreamReader(new FileInputStream(log), "UTF-8"))) {

			while ((line = reader.readLine()) != null) {
				currentByteCount += line.length();
				if (currentByteCount >= halfMax) {
					storedLines.add(line);
				}
			}

			FileUtils.writeLines(log, "UTF-8", storedLines, "\n");
		} catch (final IOException e) {
			// We obviously don't have access to the log file to write to here so the best we can
			// do is to output to the console.
			System.err.printf("Error rolling over log file: %s%n", e.getMessage());
		}
	}

	private void saveWindowState () {
		final Rectangle windowBounds = getBounds();
		final JSONObject settings = Settings.getInstance().json;

		settings.put("width", windowBounds.width);
		settings.put("height", windowBounds.height);
		settings.put("x", windowBounds.x);
		settings.put("y", windowBounds.y);
		Settings.getInstance().save();
	}

	public static void main (final String[] args) {
		Locale.setDefault(Locale.UK);
		final ImageIcon icon = new ImageIcon(EternityKeeper.class.getResource("/icon.png"));
		final File log = new File("eternity.log");

		// We set up various environment properties and dependency injections
		// here in order to make it easier to test classes later.
		Environment.initialise();
		Settings.initialise();
		rolloverLogFile(log);

		final Rectangle windowBounds = EKUtils.getDefaultWindowBounds();
		final Frame frame = new EternityKeeper();
		frame.setTitle("Eternity Keeper");
		frame.setBounds(windowBounds);
		frame.setVisible(true);
		frame.setIconImage(icon.getImage());
	}
}
