package uk.me.mantas.eternity;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class WindowCloser implements Runnable {
	private final JFrame frame;

	public WindowCloser (JFrame frame) {
		this.frame = frame;
	}

	@Override
	public void run () {
		frame.dispatchEvent(
			new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
}
