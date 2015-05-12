package uk.me.mantas.eternity.handlers;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.SaveGameInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ListSavedGames extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		// Spawn a separate thread to handle all the file stuff so we don't
		// lock up the UI.
		Environment.getInstance().getWorkers().execute(
			new FileLister(request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}

	public class FileLister implements Runnable {
		private String savesLocation;
		private CefQueryCallback callback;

		public FileLister (String savesLocation, CefQueryCallback callback) {
			this.savesLocation = savesLocation;
			this.callback = callback;
		}

		@Override
		public void run () {
			File workingDirectory =
				Environment.getInstance().getWorkingDirectory();

			//noinspection ResultOfMethodCallIgnored
			workingDirectory.mkdir();
			Optional<SaveGameInfo[]> saves = unpackAllSaves(workingDirectory);
		}

		private Optional<SaveGameInfo[]> unpackAllSaves (File workingDirectory) {
			File savesDirectory = new File(savesLocation);

			if (!savesDirectory.exists()) {
				notFound(callback);
				return Optional.empty();
			}

			File[] saves = savesDirectory.listFiles();
			if (saves == null || saves.length < 1) {
				notFound(callback);
				return Optional.empty();
			}

			Function<File, File> unpackSave = save -> {
				String saveName = save.getName();
				String destinationPath =
					new File(workingDirectory, saveName).getAbsolutePath();

				try {
					ZipFile archive = new ZipFile(save);
					archive.extractAll(destinationPath);
					return new File(destinationPath);
				} catch (ZipException e) {
					System.err.printf(
						"Unable to unzip '%s': %s%n"
						, save.getAbsolutePath()
						, e.getMessage());
				}

				return null;
			};

			Function<File, SaveGameInfo> extractInfo = saveFolder -> {
				File[] contents = saveFolder.listFiles();

				if (contents == null) {
					System.err.printf(
						"Unzip resulted in 0 files for '%s'.%n"
						, saveFolder.getAbsolutePath());

					return null;
				}

				Optional<File> saveInfoXML = Arrays.stream(contents)
					.filter(f -> f.getName().equals("saveinfo.xml"))
					.findFirst();

				if (!saveInfoXML.isPresent()) {
					System.err.printf(
						"No saveinfo.xml present in extracted save game '%s'.%n"
						, saveFolder.getAbsolutePath());

					return null;
				}

				return null;
			};

			SaveGameInfo[] info = Arrays.stream(saves)
				.<File>map(unpackSave)
				.<File>filter(a -> a != null)
				.<SaveGameInfo>map(extractInfo)
				.<SaveGameInfo>filter(a -> a != null)
				.toArray(SaveGameInfo[]::new);

			return Optional.of(info);
		}
	}

	private void notFound (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error")
				.value("NO_RESULTS")
			.endObject()
			.toString();

		callback.success(json);
	}
}
