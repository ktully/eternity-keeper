/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 the authors.
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

var SavedGame = function () {
	var self = this;

	var defaultState = {
		saveData: {}
		, info: {}
		, activeCharacter: false
		, activeTab: 'characterAttributes'
	};

	var populateCharacterList = (container, characters) => {
		container.append(
			characters.map(
				character =>
					$('<li>')
					.data('guid', character.GUID)
					.html('<i class="fa fa-heartbeat"></i> ' + character.name)
					.addClass(character.isDead ? 'dead' : '')
					.click(self.switchCharacter.bind(self, character.GUID))));
	};

	var populateCharacter = (container, data) => {
		container.find('.portrait')
			.empty()
			.css('background-image', 'url(data:image/png;base64,' + data.portrait + ')')
			.css('background-repeat', 'no-repeat')
			.html((data.isDead) ? '<div>DEAD</div>' : '');

		var row = $('<tr><td></td><td contenteditable></td></tr>');
		var rawTable = self.html.rawTable.find('tbody');
		rawTable.empty();

		for (var stat in data.stats) {
			if (!data.stats.hasOwnProperty(stat)) {
				continue;
			}

			container
				.find('.stats')
				.find('input[data-key="' + stat + '"]')
				.val(data.stats[stat].toString());

			var rawRow = row.clone();
			rawRow.find('td:first-child').text(stat);
			rawRow.find('td:last-child')
				.text(data.stats[stat])
				.data('key', stat);

			rawTable.append(rawRow);
		}

		container
			.find('.stats')
			.find('input')
			.change(self.update.bind(self))
			.keyup(self.update.bind(self));

		self.html.rawTable.find('tr > td:last-child').keyup(self.update.bind(self));
	};

	self.state = defaultState;
	self.html = {};

	self.init = () => {
		self.html.characterTabs.find('li').click(self.switchTab.bind(self));
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.characterList.empty();
		self.html.characterAttributes.hide();
		self.html.rawTable.hide();
		self.html[self.state.activeTab].show();
		self.html.characterTabs.find('li').removeClass('active');
		self.html.characterTabs.find('a[target=' + self.state.activeTab + ']')
			.parent().addClass('active');
		Eternity.render({saveView: true});
		Eternity.CurrencyEditor.render({enabled: true, amount: self.state.saveData.currency});
		Eternity.Modifications.html.newSaveName.val(
			Eternity.Modifications.suggestSaveName(self.state.info));
		populateCharacterList(self.html.characterList, self.state.saveData.characters);

		if (self.state.activeCharacter) {
			self.html.characterList.find('li')
				.removeClass('active')
				.filter((i, li) => $(li).data('guid') === self.state.activeCharacter)
				.addClass('active');
			var character =
				self.state.saveData.characters.filter(c => c.GUID == self.state.activeCharacter);
			if (character.length > 0) {
				populateCharacter(self.html.character, character[0]);
			}
		} else {
			self.switchCharacter(self.state.saveData.characters[0].GUID);
		}
	};
};

SavedGame.prototype.switchCharacter = function (guid) {
	var self = this;
	self.transition({activeCharacter: guid});
};

SavedGame.prototype.update = function (e) {
	var self = this;
	var element = $(e.currentTarget);
	var key = element.data('key');
	var value = (element.prop('nodeName') === 'TD') ? element.text() : element.val();

	if (key === null || key.length < 1) {
		return;
	}

	var character =
		self.state.saveData.characters.filter(c => c.GUID === self.state.activeCharacter)[0];
	character.stats[key] = value;
	Eternity.Modifications.transition({modifications: true});
};

SavedGame.prototype.switchTab = function (e) {
	var self = this;
	var tab = $(e.currentTarget);
	self.transition({activeTab: tab.find('a').attr('target')});
};

$.extend(SavedGame.prototype, Renderer.prototype);
