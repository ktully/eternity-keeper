/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
		var dialog = $('#exportCharacterDialog');
		dialog.find('.alert-danger').show();
		dialog.find('.multi-selector').empty();
	};

	var exportComplete = function () {
		notExporting();
		var dialog = $('#exportCharacterDialog');
		dialog.find('.alert-success').show();
		dialog.find('.multi-selector').empty();
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

	// Disabling this feature until it's working.
	//$('#menu-export-character').click(selectCharacterToExport);
};

new ExportCharacter();
