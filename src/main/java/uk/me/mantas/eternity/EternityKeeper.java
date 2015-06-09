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
import uk.me.mantas.eternity.handlers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;

import static org.cef.browser.CefMessageRouter.CefMessageRouterConfig;

public class EternityKeeper extends JFrame {
	private CefApp cefApp;
	private CefClient cefClient;
	private CefBrowser browser;

	private EternityKeeper () {
		cleanupOldUpdates();

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
		addJSHandlers();

		browser = cefClient.createBrowser(
			"file:///src/ui/index.html"
			, OS.isLinux()
			, false);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
		pack();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent e) {
				if (Environment.getInstance().closing) {
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

	private void addJSHandlers () {
		CefMessageRouter getDefaultSaveLocationRouter = CefMessageRouter.create(
			new CefMessageRouterConfig(
				"getDefaultSaveLocation"
				, "getDefaultSaveLocationCancel")
			, new GetDefaultSaveLocation());

		CefMessageRouter listSavedGamesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("listSavedGames", "listSavedGamesCancel")
			, new ListSavedGames());

		CefMessageRouter openSavedGameRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("openSavedGame", "openSavedGameCancel")
			, new OpenSavedGame());

		CefMessageRouter saveSettingsRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveSettings", "saveSettingsCancel")
			, new SaveSettings());

		CefMessageRouter saveChangesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveChanges", "saveChangesCancel")
			, new SaveChanges());

		CefMessageRouter closeWindowRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("closeWindow", "closeWindowCancel")
			, new CloseWindow(this));

		CefMessageRouter checkExtractionProgressRouter =
			CefMessageRouter.create(
				new CefMessageRouterConfig(
					"checkExtractionProgress"
					, "checkExtractionProgressCancel")
				, new CheckExtractionProgress());

		CefMessageRouter checkForUpdatesRouter =
			CefMessageRouter.create(
				new CefMessageRouterConfig(
					"checkForUpdates"
					, "checkForUpdatesCancel")
				, new CheckForUpdates());

		CefMessageRouter downloadUpdateRouter =
			CefMessageRouter.create(
				new CefMessageRouterConfig(
					"downloadUpdate"
					, "downloadUpdateCancel")
				, new DownloadUpdate());

		CefMessageRouter checkDownloadProgressRouter =
			CefMessageRouter.create(
				new CefMessageRouterConfig(
					"checkDownloadProgress"
					, "checkDownloadProgressCancel")
				, new CheckDownloadProgress());

		CefMessageRouter exportCharacterRouter =
			CefMessageRouter.create(
				new CefMessageRouterConfig(
					"exportCharacter"
					, "exportCharacterCancel")
				, new ExportCharacter());

		cefClient.addMessageRouter(getDefaultSaveLocationRouter);
		cefClient.addMessageRouter(listSavedGamesRouter);
		cefClient.addMessageRouter(openSavedGameRouter);
		cefClient.addMessageRouter(saveSettingsRouter);
		cefClient.addMessageRouter(saveChangesRouter);
		cefClient.addMessageRouter(closeWindowRouter);
		cefClient.addMessageRouter(checkExtractionProgressRouter);
		cefClient.addMessageRouter(checkForUpdatesRouter);
		cefClient.addMessageRouter(downloadUpdateRouter);
		cefClient.addMessageRouter(checkDownloadProgressRouter);
		cefClient.addMessageRouter(exportCharacterRouter);
	}

	private void shutdown () {
		saveWindowState();
		cleanupTempDirs();
		Environment.joinAllWorkers();
		System.exit(0);
	}

	private void cleanupOldUpdates () {
		File jarDirectory = Environment.getInstance().getJarDirectory();
		if (!jarDirectory.exists()) {
			//noinspection ResultOfMethodCallIgnored
			jarDirectory.mkdirs();
			return;
		}

		File[] jars = jarDirectory.listFiles();
		if (jars == null || jars.length < 2) {
			return;
		}

		long latestTimestamp = EKUtils.getTimestampOfLatestJar(jars);
		String latestFilename = String.format("%d.jar", latestTimestamp);

		// Delete all files that have .jar extensions which aren't the latest
		// one (which we should be using).
		Arrays.stream(jars)
			.filter(jar -> !jar.getName().equals(latestFilename))
			.filter(File::isFile)
			.filter(jar ->
				EKUtils.getExtension(jar.getName())
					.map(s -> s.equals("jar")).orElse(false))
			.forEach(File::delete);
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
