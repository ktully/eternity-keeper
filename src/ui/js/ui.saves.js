var SavesManager = function () {
	var savedGameLocationField = $('#savedGameLocation');

	var updateSaveLocation = function (response) {
		response = JSON.parse(response);
		if (response.error) {
			savedGameLocationField.attr(
				'placeholder'
				, 'Unable to locate save folder');
		} else if (response.location) {
			savedGameLocationField.val(response.location);
		}
	};

	var updateSaveLocationManual = function (response) {

	};

	var userSelectSaveLocation = function () {
		window.spawnFileDialog({
			request: 'Pillars of Eternity Saves Folder'
			, onSuccess: updateSaveLocationManual
			, onFailure: console.error.bind(
				console
				, 'Error prompting user for saves location.')
		});
	};

	window.getDefaultSaveLocation({
		request: 'default'
		, onSuccess: updateSaveLocation
		, onFailure: console.error.bind(
			console
			, 'Error getting default save location.')
	});

	$('#selectSavedGameDirectory').click(userSelectSaveLocation);
};

new SavesManager();
