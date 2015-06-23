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


var SavesManager = function () {
	var self = this;
	var updateInterval = null;
	self.currentSavedGame = null;
	self.windowClosing = false;

	var saveBlock =
		$('<div class="save-info">'
			+ '<table>'
				+ '<tbody>'
					+ '<tr>'
						+ '<td rowspan="2" class="screenshot"></td>'
						+ '<td colspan="2" class="name"></td>'
					+ '</tr>'
					+ '<tr>'
						+ '<td class="portraits"></td>'
						+ '<td class="date"></td>'
					+ '</tr>'
				+ '</tbody>'
			+ '</table>'
			+ '<i class="fa fa-spinner fa-pulse"></i>'
		+ '</div>');

	var savedGameLocationField = $('#savedGameLocation');
	var gameLocationField = $('#gameLocation');
	var isProcessing = false;

	var searching = function () {
		isProcessing = true;
		$('#searchForSavedGames')
		.prop('disabled', true)
		.html('<i class="fa fa-spinner fa-pulse" style="display: inline-block;">'
			+ '</i> Searching...');

		updateInterval = setInterval(pollForUpdate, 1000);
	};

	var notSearching = function () {
		clearInterval(updateInterval);
		isProcessing = false;
		$('#searchForSavedGames')
		.prop('disabled', false)
		.html('<i class="fa fa-spinner fa-pulse" style="display: none;">'
			+ '</i> Search');
	};

	var listSavedGamesFailed = function () {
		resetProgress();
		notSearching();
		console.error('Error listing saved games.');
	};

	var opening = function (el) {
		isProcessing = true;
		$(el).find('i').show();
	};

	self.notOpening = function () {
		isProcessing = false;
		$('.save-info i').hide();
	};

	var openSavedGame = function (info, e) {
		if (isProcessing) {
			return;
		}

		opening(e.currentTarget);
		self.currentSavedGame = new SavedGame(info);
	};

	var updateProgress = function (responseText) {
		var response = JSON.parse(responseText);
		if (response.update) {
			$('#progress div').css('width', response.update + 'px');
		}
	};

	var pollForUpdate = function () {
		window.checkExtractionProgress({
			request: "true"
			, onSuccess: updateProgress
			, onFailure: console.error.bind(
				console
				, "Error checking for extraction progress")
		});
	};

	var generateSaveGameTiles = function (response) {
		resetProgress();
		notSearching();
		$('.save-blocks').empty();

		var saveInfo = JSON.parse(response);
		if (saveInfo.error) {
            resetProgress();
            errorShow('No saves found.');
            return;
        }

		saveInfo.forEach(function (info) {
			var saveTile = saveBlock.clone();

			saveTile.find('.screenshot').append(
				$('<img>').attr(
					'src'
					, 'data:image/png;base64,' + info.screenshot));

			saveTile.find('.name').text(
				info.playerName
				+ ' - '
				+ info.systemName
				+ ((info.userSaveName) ? ' (' + info.userSaveName + ')' : ''));

			saveTile.find('.date').text(info.date);

			var portraits = info.portraits.map(function (portrait) {
				return '<img src="data:image/png;base64,' + portrait + '">';
			});

			saveTile.find('.portraits').html(portraits.join(' '));
			saveTile.click(openSavedGame.bind(self, info));
			$('.save-blocks').append(saveTile);
		});
	};

	var resetProgress = function () {
		$('#progress div').css('width', '0px');
	};

	var listSavedGames = function () {
		if (isProcessing) {
			return;
		}

		resetProgress();
		errorHide();
		searching();
		window.listSavedGames({
			request: $('#savedGameLocation').val()
			, onSuccess: generateSaveGameTiles
			, onFailure: listSavedGamesFailed
		});
	};

	var updateSaveLocation = function (response) {
		response = JSON.parse(response);
		if (!response.savesLocation || response.savesLocation.length < 1) {
			savedGameLocationField.attr(
				'placeholder'
				, 'Unable to locate save folder');
		} else {
			savedGameLocationField.val(response.savesLocation);
			listSavedGames();
		}

		if (!response.gameLocation || response.gameLocation.length < 1) {
			gameLocationField.attr(
				'placeholder'
				, 'Unable to locate installation folder');

			$('#settings').modal('show');
		} else {
			gameLocationField.val(response.gameLocation);
		}
	};

	self.switchToSavesManagerContext = function () {
		$('.character').hide();
		$('.characters').empty();

		$('#menu-save-modification'
			+ ', #menu-export-character'
			+ ', #menu-import-character')
			.parent().addClass('disabled');

		$('#menu-save-modification'
			+ ', #menu-export-character'
			+ ', #menu-import-character')
			.off();

		$('.saved-game-locator, .save-blocks').show();
		self.currentSavedGame = null;
		saveModifications.previousName = null;
		saveModifications.savedYet = false;
	};

	self.switchToSavedGameContext = function () {
		errorHide();
		$('.saved-game-locator').hide();
		$('.save-blocks').hide();
		$('#menu-save-modification'
			+ ', #menu-export-character'
			+ ', #menu-import-character')
			.parent().removeClass('disabled');

		$('#menu-save-modification').click(saveModifications.saveChanges);
	};

	$(document).keyup(function (e) {
		if (e.ctrlKey === true && e.keyCode === 83) { // Ctrl+s
			saveModifications.saveChanges();
		}
	});

	window.getDefaultSaveLocation({
		request: 'default'
		, onSuccess: updateSaveLocation
		, onFailure: console.error.bind(
			console
			, 'Error getting default save location.')
	});

	$('#searchForSavedGames').click(listSavedGames);
};

var savesManager = new SavesManager();
