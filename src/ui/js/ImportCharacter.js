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

	// Disabling this feature until it's working.
	//$('#menu-import-character').click(selectCharacterToImport);
};

new ImportCharacter();
