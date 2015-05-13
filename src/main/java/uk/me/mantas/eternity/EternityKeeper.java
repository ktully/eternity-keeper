package uk.me.mantas.eternity;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefAppHandlerAdapter;
import org.json.JSONObject;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.handlers.ListSavedGames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static org.cef.browser.CefMessageRouter.CefMessageRouterConfig;

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
		settings.remote_debugging_port = 13001;

		cefApp = CefApp.getInstance(settings);
		cefClient = cefApp.createClient();
		addJSHandlers();

		browser = cefClient.createBrowser(
			"file:///src/ui/index.html"
			, OS.isLinux()
			, false);

		getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
		pack();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent e) {
				CefApp.getInstance().dispose();
				dispose();
			}
		});
	}

	private void addJSHandlers () {
		CefMessageRouter openSavedGameRouter = CefMessageRouter.create(
			new CefMessageRouterConfig(
				"getDefaultSaveLocation"
				, "getDefaultSaveLocationCancel")
			, new GetDefaultSaveLocation());

		CefMessageRouter listSavedGamesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("listSavedGames", "listSavedGamesCancel")
			, new ListSavedGames());

		cefClient.addMessageRouter(openSavedGameRouter);
		cefClient.addMessageRouter(listSavedGamesRouter);
	}

	private void shutdown () {
		saveWindowState();
		cleanupTempDirs();
		Environment.joinAllWorkers();
		System.exit(0);
	}

	private void cleanupTempDirs () {
		Environment.getInstance().deleteWorkingDirectory();
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
		ImageIcon icon = new ImageIcon(
			EternityKeeper.class.getResource("/icon.png"));

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
