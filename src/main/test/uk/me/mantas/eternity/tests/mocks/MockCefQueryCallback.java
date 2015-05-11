package uk.me.mantas.eternity.tests.mocks;

import org.cef.callback.CefQueryCallback;

import java.util.function.Consumer;

public class MockCefQueryCallback implements CefQueryCallback {
	private Consumer<String> onSuccess;

	public MockCefQueryCallback (Consumer<String> onSuccess) {
		this.onSuccess = onSuccess;
	}

	@Override
	public void success (String s) {
		onSuccess.accept(s);
	}

	@Override
	public void failure (int i, String s) {

	}
}
