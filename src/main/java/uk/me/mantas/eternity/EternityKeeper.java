package uk.me.mantas.eternity;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EternityKeeper extends JFrame {
	private EternityKeeper () {
		CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
			@Override
			public void stateHasChanged (CefAppState state) {
				if (state == CefAppState.TERMINATED)
					cefShutdown();
			}
		});

		CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = OS.isLinux();
		CefApp cefApp = CefApp.getInstance(settings);
		CefClient client = cefApp.createClient();
		CefBrowser browser = client.createBrowser(
			"file:///src/ui/index.html"
			, OS.isLinux()
			, false);

		Component browserUI = browser.getUIComponent();
		getContentPane().add(browserUI, BorderLayout.CENTER);
		pack();
		setSize(1024, 768);
		setVisible(true);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent e) {
				CefApp.getInstance().dispose();
				dispose();
			}
		});
	}

	private void cefShutdown () {
		System.exit(0);
	}

	public static void main (String[] args) {
		new EternityKeeper();
	}
}
