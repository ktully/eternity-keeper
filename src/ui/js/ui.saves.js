var SavesManager = function () {
	var savedGameLocationField = $('#savedGameLocation');
	var isSearching = false;

	var searching = function () {
		isSearching = true;
		$('#searchForSavedGames')
		.prop('disabled', true)
		.text('Searching')
		.find('.ellipsis')
			.show();
	};

	var notSearching = function () {
		isSearching = false;
		$('#searchForSavedGames')
		.prop('disabled', false)
		.text('Search')
		.find('.ellipsis')
			.hide();
	};

	var listSavedGamesFailed = function () {
		notSearching();
		console.error('Error listing saved games.');
	};

	var generateSaveGameTiles = function (response) {
		notSearching();
	};

	var listSavedGames = function () {
		if (isSearching) {
			return;
		}

		searching();
		window.listSavedGames({
			request: $('#savedGameLocation').val()
			, onSuccess: generateSaveGameTiles
			, onFailure: listSavedGamesFailed
		});
	};

	var updateSaveLocation = function (response) {
		response = JSON.parse(response);
		if (response.error) {
			savedGameLocationField.attr(
				'placeholder'
				, 'Unable to locate save folder');
		} else if (response.location) {
			savedGameLocationField.val(response.location);
			listSavedGames();
		}
	};

	window.getDefaultSaveLocation({
		request: 'default'
		, onSuccess: updateSaveLocation
		, onFailure: console.error.bind(
			console
			, 'Error getting default save location.')
	});

	$('#searchForSavedGames').click(listSavedGames);
};

new SavesManager();
