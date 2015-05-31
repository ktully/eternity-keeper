package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.save.SaveGameExtractor;
import uk.me.mantas.eternity.save.SaveGameInfo;

import java.util.Arrays;
import java.util.Optional;

public class ListSavedGames extends CefMessageRouterHandlerAdapter {
	private static final DateTimeFormatter dateFormatter =
		DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Settings.getInstance().json.put("savesLocation", request);
		Environment environment = Environment.getInstance();

		// Spawn a separate thread to handle all the file stuff so we don't
		// lock up the UI.
		SaveInfoLister lister = new SaveInfoLister(request, callback);
		environment.setCurrentSaveLister(lister);
		environment.getWorkers().execute(lister);

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
		Environment.joinAllWorkers();
	}

	public static class SaveInfoLister implements Runnable {
		private String savesLocation;
		private CefQueryCallback callback;
		public SaveGameExtractor extractor = null;

		public SaveInfoLister (String savesLocation, CefQueryCallback callback) {
			this.savesLocation = savesLocation;
			this.callback = callback;
		}

		@Override
		public void run () {
			Environment.getInstance().emptyWorkingDirectory();
			extractor = new SaveGameExtractor(
				savesLocation
				, Environment.getInstance().getWorkingDirectory());

			Optional<SaveGameInfo[]> info = extractor.unpackAllSaves();

			if (!info.isPresent() || info.get().length < 1) {
				notFound(callback);
				return;
			}

			callback.success(saveInfoToJSON(info.get()));
		}
	}

	private static String saveInfoToJSON (SaveGameInfo[] info) {
		JSONObject[] infoJSONObjects = Arrays.stream(info).map(
			saveInfo -> new JSONObject()
				.put("guid", saveInfo.guid)
				.put("systemName", saveInfo.systemName)
				.put("absolutePath", saveInfo.absolutePath)
				.put("playerName", saveInfo.playerName)
				.put("sceneTitle", saveInfo.sceneTitle)
				.put("chapter", saveInfo.chapter)
				.put("trialOfIron", saveInfo.trialOfIron)
				.put("date", dateFormatter.print(saveInfo.timestamp))
				.put("userSaveName", saveInfo.userSaveName)
				.put("difficulty", saveInfo.difficulty)
				.put("screenshot", saveInfo.screenshot)
				.put("portraits", saveInfo.portraits)
		).toArray(JSONObject[]::new);

		return new JSONArray(infoJSONObjects).toString();
	}

	private static void notFound (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error").value("NO_RESULTS")
			.endObject()
			.toString();

		callback.success(json);
	}
}
