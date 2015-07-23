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


var SavedGame = function (info) {
	var self = this;
	self.info = info;
	self.saveData = null;
	self.modifications = false;

	var getSelectedCharacter = function () {
		var guid = $('.characters .active').attr('data-guid');
		return self.saveData.characters.filter(function (c) {
			return c.GUID === guid;
		}).shift();
	};

	var switchCharacter = function (e) {
		var guid = $(e.currentTarget).attr('data-guid');
		$('.characters li').removeClass('active');
		$(e.currentTarget).addClass('active');
		var character = self.saveData.characters.filter(function (c) {
			return c.GUID === guid;
		}).shift();

		populateCharacter(character);
	};

	var populateCharacterList = function () {
		var items = self.saveData.characters.map(function (character) {
			return $('<li>')
				.attr('data-guid', character.GUID)
				.html('<i class="fa fa-heartbeat"></i> ' + character.name)
				.addClass(character.isDead ? 'dead' : '');
		});

		$('.characters').empty().append(items);
		$('.characters li').click(switchCharacter);
	};

	var updateData = function (e) {
		var el = $(e.currentTarget);
		var key = el.attr('data-key');
		var value = (el.prop('nodeName') === 'TD') ? el.text() : el.val();

		if (key === null || key.length < 1) {
			return;
		}

		var character = getSelectedCharacter();
		character.stats[key] = value.toString();
		self.modifications = true;
	};

	self.tabSwitch = function () {
		// We need to refresh values between tabs since they both modify the same source data.
		var character = getSelectedCharacter();
		populateCharacter(character);
	};

	var populateCharacter = function (character) {
		var portrait = $('.character .portrait');
		portrait
			.empty()
			.css('background-image', 'url(data:image/png;base64,' + character.portrait + ')')
			.css('background-repeat', 'no-repeat');

		if (character.isDead) {
			portrait.append($('<div>DEAD</div>'));
		}

		var row = $('<tr><td></td><td contenteditable></td></tr>');
		var rawTable = $('.raw').find('tbody');
		rawTable.empty();

		for (var stat in character.stats) {
			if (!character.stats.hasOwnProperty(stat)) {
				continue;
			}

			$('.character .stats')
				.find('input[data-key="' + stat + '"]')
				.val(character.stats[stat]);

			var rawRow = row.clone();
			rawRow.find('td:first-child').text(stat);
			rawRow.find('td:last-child')
				.text(character.stats[stat])
				.attr('data-key', stat);

			rawTable.append(rawRow);
		}

		var formInputs = $('.stats').find('input');
		formInputs.change(updateData);
		formInputs.keyup(updateData);
		$('.raw tr > td:last-child').keyup(updateData);
	};

	var populateCurrency = function (amount) {
		amount = parseInt(amount);
		$('#currency').val(amount);
	};

	self.loadUI = function (response) {
		savesManager.notOpening();
		var saveData = JSON.parse(response);

		if (saveData.characters.length < 1) {
			errorShow('No characters found in save game.');
			return;
		}

		self.saveData = saveData;
		self.saveData.characters = self.saveData.characters.map(function (character) {
			// Hate to lose type information here but HTML forms won't preserve it for us anyway.
			for (stat in character.stats) {
				if (!character.stats.hasOwnProperty(stat)) {
					continue;
				}

				character.stats[stat] = character.stats[stat].toString();
			}

			return character;
		});

		savesManager.switchToSavedGameContext();

		$('.character').show();
		populateCharacterList();
		$('.characters li').first().addClass('active');
		populateCharacter(self.saveData.characters[0]);
		populateCurrency(self.saveData.currency);
	};

	window.openSavedGame({
		request: info.absolutePath
		, onSuccess: self.loadUI
		, onFailure: console.error.bind(
			console
			, 'Error opening saved game %s.'
			, info.absolutePath)
	});
};
