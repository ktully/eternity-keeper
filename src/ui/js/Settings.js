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
