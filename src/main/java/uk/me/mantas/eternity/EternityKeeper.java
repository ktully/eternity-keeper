package uk.me.mantas.eternity;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefAppHandlerAdapter;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.handlers.SpawnFileDialog;

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

		CefMessageRouter spawnFileDialogRouter = CefMessageRouter.create(
			new CefMessageRouterConfig(
				"spawnFileDialog"
				, "spawnFileDialogCancel")
			, new SpawnFileDialog());

		cefClient.addMessageRouter(openSavedGameRouter);
		cefClient.addMessageRouter(spawnFileDialogRouter);
	}

	private void shutdown () {
		System.exit(0);
	}

	public static void main (String[] args) {
		ImageIcon icon = new ImageIcon(
			EternityKeeper.class.getResource("/icon.png"));

		// We set up various environment properties and dependency injections
		// here in order to make it easier to test classes later.
		Harness.initialise();
		Dimension windowSize = EKUtils.getBestWindowSize();

		Frame frame = new EternityKeeper();
		frame.setTitle("Eternity Keeper");
		frame.setSize(windowSize.width, windowSize.height);
		frame.setVisible(true);
		frame.setIconImage(icon.getImage());
	}
}
