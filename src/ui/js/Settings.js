var Settings = function () {
	var self = this;
	var inProgress = false;

	var saving = function () {
		inProgress = true;
		$('#saveSettings i').show();
		$('#saveSettings').prop('disabled', true);
	};

	var notSaving = function () {
		inProgress = false;
		$('#saveSettings i').hide();
		$('#saveSettings').prop('disabled', false);
	};

	var settingsSaved = function () {
		notSaving();
		$('#settings').modal('hide');
	};

	var settingsNotSaved = function () {
		notSaving();
		console.error('Error saving settings');
	};

	var saveSettings = function () {
		if (inProgress) {
			return;
		}

		saving();

		var data = {
			gameLocation: $('#gameLocation').val()
		};

		window.saveSettings({
			request: JSON.stringify(data)
			, onSuccess: settingsSaved
			, onFailure: settingsNotSaved
		});
	};

	$('#saveSettings').click(saveSettings);
};

var settings = new Settings();
