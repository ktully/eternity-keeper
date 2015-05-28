var SavedGame = function (absolutePath) {
	var loadUI = function (response) {
		savesManager.notOpening();
	};

	window.openSavedGame({
		request: absolutePath
		, onSuccess: loadUI
		, onFailure: console.error.bind(
			console
			, 'Error opening saved game %s.'
			, absolutePath)
	});
};
