var ExportCharacter = function () {
	var self = this;
	var isExporting = false;

	var exporting = function (el) {
		isExporting = true;
		el.find('i').show();
	};

	var notExporting = function () {
		isExporting = false;
		$('#exportCharacterDialog').find('i').hide();
	};

	var exportFailed = function () {
		notExporting();
		$('#exportCharacterDialog').find('.alert-danger').show();
	};

	var exportComplete = function () {
		notExporting();
		$('#exportCharacterDialog').find('.alert-success').show();
	};

	var doExport = function (e) {
		if (isExporting) {
			return;
		}

		var el = $(e.currentTarget);
		exporting(el);

		var request = {
			GUID: el.attr('data-guid')
			, absolutePath: savesManager.currentSavedGame.info.absolutePath
		};

		window.exportCharacter({
			request: JSON.stringify(request)
			, onSuccess: exportComplete
			, onFailure: exportFailed
		});
	};

	var selectCharacterToExport = function (e) {
		if ($(e.currentTarget).prop('disabled') || savesManager.currentSavedGame == null) {
			return;
		}

		var lis = savesManager.currentSavedGame.characterData.map(function (character) {
			return $('<li>')
				.text(character.name)
				.append($('<i>').addClass('fa fa-spinner fa-pulse'))
				.attr('data-guid', character.GUID);
		});

		var dialog = $('#exportCharacterDialog');
		dialog.find('.alert').hide();
		dialog.find('.multi-selector').empty().append(lis);
		dialog.find('li').click(doExport);
		dialog.modal('show');
	};

	$('#menu-export-character').click(selectCharacterToExport);
};

new ExportCharacter();
