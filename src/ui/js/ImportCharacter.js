var ImportCharacter = function () {
	var self = this;

	var importFailure = function (id, response) {
		if (response === 'NO_SAVE') {
			return;
		}

		errorShow(response);
		$('#error').find('div').append(
			'<button type="button" class="close" data-dismiss="alert">'
			+ '<span>&times;</span></button>');
	};

	var importSuccess = function (newSave) {
		savesManager.currentSavedGame.loadUI(newSave);
		savesManager.currentSavedGame.modifications = true;
	};

	var selectCharacterToImport = function () {
		var request = {
			oldSave: savesManager.currentSavedGame.info.absolutePath
			, savedYet: saveModifications.savedYet
		};

		window.importCharacter({
			request: JSON.stringify(request)
			, onSuccess: importSuccess
			, onFailure: importFailure
		});
	};

	$('#menu-import-character').click(selectCharacterToImport);
};

new ImportCharacter();
