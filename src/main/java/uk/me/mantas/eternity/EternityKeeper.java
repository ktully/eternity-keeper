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


package uk.me.mantas.eternity;

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
import java.io.File;

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

		CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = OS.isLinux();
		settings.remote_debugging_port = 13002;

		cefApp = CefApp.getInstance(settings);
		cefClient = cefApp.createClient();
		JSHandlers.register(cefClient, this);

		String index = "/src/ui/index.html";
		if (OS.isLinux()) {
			index = new File("src/ui/index.html").getAbsolutePath();
		}

		browser = cefClient.createBrowser(
			String.format("file://%s", index)
			, OS.isLinux()
			, false);

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
					browser.executeJavaScript(
						"checkForModifications();"
						, browser.getURL()
						, 0);
				}
			}
		});
	}

	private void shutdown () {
		saveWindowState();
		cleanupTempDirs();
		Environment.joinAllWorkers();
		System.exit(0);
	}

	private static void cleanupOldLogFile () {
		//noinspection ResultOfMethodCallIgnored
		new File("eternity.log").delete();
	}

	private void cleanupTempDirs () {
		Environment.getInstance().directory().deleteWorking();
	}

	private void saveWindowState () {
		Rectangle windowBounds = getBounds();
		JSONObject settings = Settings.getInstance().json;

		settings.put("width", windowBounds.width);
		settings.put("height", windowBounds.height);
		settings.put("x", windowBounds.x);
		settings.put("y", windowBounds.y);
		Settings.getInstance().save();
	}

	public static void main (String[] args) {
		cleanupOldLogFile();
		ImageIcon icon = new ImageIcon(EternityKeeper.class.getResource("/icon.png"));

		// We set up various environment properties and dependency injections
		// here in order to make it easier to test classes later.
		Environment.initialise();
		Settings.initialise();
		Rectangle windowBounds = EKUtils.getDefaultWindowBounds();

		Frame frame = new EternityKeeper();
		frame.setTitle("Eternity Keeper");
		frame.setBounds(windowBounds);
		frame.setVisible(true);
		frame.setIconImage(icon.getImage());
	}
}
