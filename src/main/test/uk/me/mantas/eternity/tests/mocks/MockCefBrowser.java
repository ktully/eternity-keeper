package uk.me.mantas.eternity.tests.mocks;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefDialogHandler;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.network.CefRequest;

import java.awt.*;
import java.util.Vector;

public class MockCefBrowser implements CefBrowser {
	@Override
	public Component getUIComponent () {
		return null;
	}

	@Override
	public CefRenderHandler getRenderHandler () {
		return null;
	}

	@Override
	public CefWindowHandler getWindowHandler () {
		return null;
	}

	@Override
	public boolean canGoBack () {
		return false;
	}

	@Override
	public void goBack () {

	}

	@Override
	public boolean canGoForward () {
		return false;
	}

	@Override
	public void goForward () {

	}

	@Override
	public boolean isLoading () {
		return false;
	}

	@Override
	public void reload () {

	}

	@Override
	public void reloadIgnoreCache () {

	}

	@Override
	public void stopLoad () {

	}

	@Override
	public int getIdentifier () {
		return 0;
	}

	@Override
	public boolean isPopup () {
		return false;
	}

	@Override
	public boolean hasDocument () {
		return false;
	}

	@Override
	public void viewSource () {

	}

	@Override
	public void getSource (CefStringVisitor cefStringVisitor) {

	}

	@Override
	public void getText (CefStringVisitor cefStringVisitor) {

	}

	@Override
	public void loadRequest (CefRequest cefRequest) {

	}

	@Override
	public void loadURL (String s) {

	}

	@Override
	public void loadString (String s, String s1) {

	}

	@Override
	public void executeJavaScript (String s, String s1, int i) {

	}

	@Override
	public String getURL () {
		return null;
	}

	@Override
	public void close () {

	}

	@Override
	public void setFocus (boolean b) {

	}

	@Override
	public void setWindowVisibility (boolean b) {

	}

	@Override
	public double getZoomLevel () {
		return 0;
	}

	@Override
	public void setZoomLevel (double v) {

	}

	@Override
	public void runFileDialog (
		CefDialogHandler.FileDialogMode fileDialogMode
		, String s
		, String s1
		, Vector<String> vector
		, CefRunFileDialogCallback cefRunFileDialogCallback) {

	}

	@Override
	public void startDownload (String s) {

	}

	@Override
	public void print () {

	}

	@Override
	public void find (int i, String s, boolean b, boolean b1, boolean b2) {

	}

	@Override
	public void stopFinding (boolean b) {

	}

	@Override
	public CefBrowser getDevTools () {
		return null;
	}

	@Override
	public CefBrowser getDevTools (Point point) {
		return null;
	}

	@Override
	public void replaceMisspelling (String s) {

	}
}
