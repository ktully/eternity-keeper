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
	var disabledForCompanions = [
		'BaseMight', 'BaseConstitution', 'BaseDexterity', 'BaseIntellect', 'BasePerception'
		, 'BaseResolve'];

	self.views = Object.freeze({ATTR: 0, RAW: 1, GLOBALS: 2});

	var defaultState = {
		saveData: {}
		, info: {}
		, activeCharacter: false
		, view: self.views.ATTR
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

	var sortData = unsorted => {
		var sortable = [];
		for (var k in unsorted) {
			if (!unsorted.hasOwnProperty(k)) {
				continue;
			}

			sortable.push([k, unsorted[k]]);
		}

		sortable.sort((a, b) => a[0].toLowerCase() > b[0].toLowerCase() ? 1 : -1);
		return sortable;
	};

	var createBooleanEditor = initialValue => {
		return createEnumEditor(['true', 'false'], initialValue);
	};

	var createEnumEditor = (enumValues, initialValue) => {
		return $('<select></select>')
			.addClass('form-control')
			.append(enumValues.map(v =>
				$('<option></option>').prop('selected', v == initialValue).text(v).val(v)));
	};

	var createRawEditor = (key, fullkey, value) => {
		var row = $('<tr></tr>');
		var keyCol = $('<td></td>');
		var valCol = $('<td></td>');
		var editor;

		keyCol.text(key);
		valCol.data('key', key);
		valCol.data('fullkey', fullkey);

		if (value.type === 'java.lang.Boolean') {
			editor = createBooleanEditor(value.value);
			valCol.append(editor);
			editor.change(self.update.bind(self));
		} else if (Eternity.structures[value.type] !== undefined) {
			editor = createEnumEditor(Eternity.structures[value.type], value.value);
			valCol.append(editor);
			editor.change(self.update.bind(self));
		} else {
			valCol.prop('contenteditable', true);
			valCol.text(value.value);
			valCol.keyup(self.update.bind(self));
		}

		row.append(keyCol, valCol);
		return row;
	};

	var populateGlobals = globals => {
		var globalsTable = self.html.globalsTable.find('tbody');
		globalsTable.empty();

		var flat = flattenObject(globals, 2);
		var tuples = [];
		flat.forEach(tuple => {
			for (var key in tuple[1]) {
				if (!tuple[1].hasOwnProperty(key)) {
					continue;
				}

				tuples.push([tuple[0], key, tuple[1][key]]);
			}
		});

		tuples.sort((a, b) => a[1].toLowerCase() > b[1].toLowerCase() ? 1 : -1);
		tuples.forEach(tuple => {
			var row = createRawEditor(tuple[1], tuple[0] + '.' + tuple[1], tuple[2]);
			globalsTable.append(row);
		});
	};

	var populateCharacter = (container, data) => {
		container.find('.portrait')
			.empty()
			.css('background-image', 'url(data:image/png;base64,' + data.portrait + ')')
			.css('background-repeat', 'no-repeat')
			.html((data.isDead) ? '<div>DEAD</div>' : '');

		var sortedData = sortData(data.stats);
		var rawTable = self.html.rawTable.find('tbody');
		rawTable.empty();

		sortedData.forEach(tuple => {
			var stat = tuple[0];
			var value = tuple[1];

			container
				.find('.stats')
				.find('input[data-fullkey="' + stat + '"]')
				.val(value.value.toString())
				.prop('disabled', data.isCompanion && disabledForCompanions.indexOf(stat) > -1);

			rawTable.append(createRawEditor(stat, stat, value));
		});

		container
			.find('.stats')
			.find('input')
			.change(self.update.bind(self))
			.keyup(self.update.bind(self));
	};

	var filterTable = (search, table) => {
		var searchString = search.val().toLowerCase();
		if (searchString.length < 1) {
			table.find('tbody tr').show();
			return;
		}

		var matches =
			table.find('td:last-child')
				.filter((i, el) => $(el).data('key').toLowerCase().includes(searchString));

		table.find('tbody tr').hide();
		matches.each((i, el) => $(el).parent().show());
	};

	self.init = () => {
		self.html.searchRaw.keyup(filterTable.bind(self, self.html.searchRaw, self.html.rawTable));
		self.html.searchGlobals.keyup(
			filterTable.bind(self, self.html.searchGlobals, self.html.globalsTable));
	};

	self.state = $.extend({}, defaultState);
	self.html = {};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.characterList.empty();
		Eternity.render({saveView: true});

		self.html.menuCharacterAttributes.off();
		self.html.menuCharacterAttributes.click(self.switchView.bind(self, self.views.ATTR));
		self.html.menuCharacterRaw.off();
		self.html.menuCharacterRaw.click(self.switchView.bind(self, self.views.RAW));
		self.html.menuEditGlobals.off();
		self.html.menuEditGlobals.click(self.switchView.bind(self, self.views.GLOBALS));

		Eternity.CurrencyEditor.render({enabled: true, amount: self.state.saveData.currency});
		Eternity.Modifications.html.newSaveName.val(
			Eternity.Modifications.suggestSaveName(self.state.info));
		populateCharacterList(self.html.characterList, self.state.saveData.characters);
		populateGlobals(self.state.saveData.globals);

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

		$('.view').hide();
		switch(self.state.view) {
			case self.views.RAW:
				self.html.rawTable.show();
				filterTable(self.html.searchRaw, self.html.rawTable);
				break;

			case self.views.GLOBALS:
				self.html.globalsTable.show();
				filterTable(self.html.searchGlobals, self.html.globalsTable);
				break;

			default:
				self.html.character.show();
		}
	};
};

SavedGame.prototype.switchCharacter = function (guid) {
	var self = this;
	self.transition({activeCharacter: guid});
};

SavedGame.prototype.switchView = function (view) {
	var self = this;
	self.transition({view: view});
};

SavedGame.prototype.update = function (e) {
	var self = this;
	var element = $(e.currentTarget);
	var isCol = element.prop('nodeName') === 'TD';
	var isDropdown = element.prop('nodeName') === 'SELECT';
	var value = isCol ? element.text() : element.val();
	var key = isDropdown ? element.parent().data('fullkey') : element.data('fullkey');

	if (key === null || key.length < 1) {
		return;
	}

	if (key.indexOf('.') < 0) {
		var character =
			self.state.saveData.characters.filter(c => c.GUID === self.state.activeCharacter)[0];
		character.stats[key].value = value;
	} else {
		var ex = key.split('.');
		self.state.saveData.globals[ex[0]][ex[1]][ex[2]].value = value;
	}

	Eternity.Modifications.transition({modifications: true});
};

$.extend(SavedGame.prototype, Renderer.prototype);
