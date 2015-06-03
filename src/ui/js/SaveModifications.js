var SaveModifications = function () {
	var self = this;
	var switchingContexts = false;
	self.previousName = null;
	self.savedYet = false;

	var saving = function () {
		$('#saveFile').prop('disabled', true);
		$('#saveFile i').show();
	};

	var notSaving = function () {
		$('#saveFile').prop('disabled', false);
		$('#saveFile i').hide();
	};

	var gameSaved = function (saveName, response) {
		if (savesManager.windowClosing) {
			window.closeWindow({
				request: 'true'
				, onSuccess: function () {}
				, onFailure: function () {}
			});

			return;
		}

		notSaving();
		$('#saveFileDialog').modal('hide');
		$('#saveChangesDialog').modal('hide');
		self.savedYet = true;
		self.previousName = saveName;
		savesManager.currentSavedGame.modifications = false;

		if (switchingContexts) {
			switchingContexts = false;
			savesManager.switchToSavesManagerContext();
		}
	};

	var failedToSave = function (errID, response) {
		notSaving();
		$('#saveFileDialog').modal('hide');
		$('#saveChangesDialog').modal('hide');
		errorShow(JSON.parse(response).error);
	};

	var suggestSaveName = function () {
		var info = savesManager.currentSavedGame.info;
        var saveName = (info.userSaveName)
            ? info.userSaveName
            : info.systemName;

        return saveName + ' (edited)';
	};

	var doSave = function (saveName) {
		if (typeof saveName !== 'string') {
			saveName = $('#newSaveName').val();
		}

		if (saveName.length < 1) {
			saveName = suggestSaveName();
		}

		var request = {
			savedYet: self.savedYet
			, saveName: saveName
			, absolutePath: savesManager.currentSavedGame.info.absolutePath
			, characterData: savesManager.currentSavedGame.characterData
		};

		saving();
		window.saveChanges({
			request: JSON.stringify(request)
			, onSuccess: gameSaved.bind(self, saveName)
			, onFailure: failedToSave
		});
	};

	self.saveChanges = function () {
		if (self.savedYet) {
			doSave(self.previousName);
			return;
		}

		$('#saveFileDialog').off();
		$('#saveChangesDialog').modal('hide');
		$('#saveFileDialog').modal('show');
		$('#saveFileDialog').on('shown.bs.modal', function () {
			var saveName = suggestSaveName();
            if (self.previousName !== null) {
                saveName = self.previousName;
            }

            $('#newSaveName').val(saveName);
            $('#newSaveName')[0].select();
		});
	};

	var dontSaveChanges = function () {
		switchingContexts = false;

		if (savesManager.windowClosing) {
			window.closeWindow({
				request: 'true'
				, onSuccess: function () {}
				, onFailure: function () {}
			});
		} else {
			$('#saveChangesDialog').modal('hide');
			savesManager.switchToSavesManagerContext();
		}
	};

	var checkBeforeSwitching = function () {
		if (savesManager.currentSavedGame.modifications) {
			switchingContexts = true;
			$('#saveChangesDialog').modal('show');
		} else {
			savesManager.switchToSavesManagerContext();
		}
	};

	var notSwitchingContexts = function () {
		switchingContexts = false;
	};

	$('#dontSaveChanges').click(dontSaveChanges);
	$('#saveChanges').click(self.saveChanges);
	$('#saveFile').click(doSave);
	$('#menu-open-saved-game').click(checkBeforeSwitching);
	$('#saveChangesDialog .close').click(notSwitchingContexts);
	$('#saveFileDialog .close').click(notSwitchingContexts);
};

var checkForModifications = function () {
	if (savesManager.currentSavedGame
		&& savesManager.currentSavedGame.modifications) {

		savesManager.windowClosing = true;
		$('#saveChangesDialog').modal('show');
	} else {
		window.closeWindow({
			request: "true"
			, onSuccess: function () {}
			, onFailure: function () {}
		});
	}
};

var saveModifications = new SaveModifications();
