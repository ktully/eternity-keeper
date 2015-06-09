var ImportCharacter = function () {
	var self = this;

	var importFailure = function (id, response) {
		errorShow(response);
		$('#error').find('div').append(
			'<button type="button" class="close" data-dismiss="alert">'
			+ '<span>&times;</span></button>');
	};

	var importSuccess = function (newSave) {
		savesManager.currentSavedGame.loadUI(newSave);
	};

	var selectCharacterToImport = function () {
		window.importCharacter({
			request: savesManager.currentSavedGame.info.absolutePath
			, onSuccess: importSuccess
			, onFailure: importFailure
		});
	};

	$('#menu-import-character').click(selectCharacterToImport);
};

new ImportCharacter();
