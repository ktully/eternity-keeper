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

var Editor = function () {
	var self = this;

	var defaultState = {
		listView: false
		, saveView: false
	};

	var bindDOM = () => {
		// Do one pass over the DOM at startup to bind all the UI elements that we need.
		$('[data-bound]').each((i, element) => {
			var boundTo = $(element).data('bound');
			var id = $(element).attr('id');

			if (boundTo == null || boundTo.length < 1 || id == null || id.length < 1) {
				console.error('Element ', element, ' was incorrectly bound.');
				return;
			}

			if (!self[boundTo]) {
				console.warn(
					'Element ', element, ' was bound to component'
					, boundTo, ' which does not exist');
				return;
			}

			if (!self[boundTo]['html']) {
				console.error('Component ', boundTo, ' does not have the "html" property.');
				return;
			}

			self[boundTo]['html'][id] = $('#' + id);
		});
	};

	var initialise = () => {
		// Call the init method on all of our components to allow them to set up their internal
		// state now that the editor has started up.
		for (var component in self) {
			if (!self.hasOwnProperty(component)	|| typeof self[component].init !== 'function') {
				continue;
			}

			self[component].init.call(self[component]);
		}
	};

	var getDirectoryPaths = () => {
		var updateDirectoryPaths = response => {
			var searchPath = '';
			var gameLocation = '';
			response = JSON.parse(response);

			if (response.savesLocation && response.savesLocation.length > 0) {
				searchPath = response.savesLocation;
			}

			if (response.gameLocation && response.gameLocation.length > 0) {
				gameLocation = response.gameLocation;
			}

			self.SaveSearch.render({searchPath: searchPath});
			self.Settings.render({gameLocation: gameLocation});
			self.SaveSearch.search();
		};

		window.getDefaultSaveLocation({
			request: 'default'
			, onSuccess: updateDirectoryPaths
			, onFailure: console.error.bind(console, 'Error detecting directory paths.')
		});
	};

	var getStructures = () => {
		var storeStructures = response => {
			self.structures = JSON.parse(response);
		};

		window.getGameStructures({
			request: 'true'
			, onSuccess: storeStructures
			, onFailure: console.error.bind(console, 'Error getting game structures.')
		});
	};

	var disableMenu = menuID => $('#' + menuID).find('li').addClass('disabled').find('a').off();
	var enableMenu = menuID => $('#' + menuID).find('li').removeClass('disabled');

	self.state = $.extend({}, defaultState);
	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.SaveSearch.html.searchContainer.hide();
		self.SaveSearch.html.saveBlocks.hide();
		self.SavedGame.html.character.hide();
		self.SavedGame.html.rawTable.hide();
		self.SavedGame.html.globalsTable.hide();
		self.SavedGame.html.characterList.empty();

		if (self.state.listView) {
			self.SaveSearch.html.searchContainer.show();
			self.SaveSearch.html.saveBlocks.show();
			self.CurrencyEditor.transition({enabled: false});
			self.SavedGame.html.menuEditGlobals.off().parent().addClass('disabled');
			disableMenu('menuCharacter');
		}

		if (self.state.saveView) {
			self.SavedGame.html.menuEditGlobals.parent().removeClass('disabled');
			enableMenu('menuCharacter');
		}
	};

	// Component instantiation goes here:
	self.GenericError = new GenericError();
	self.Progress = new Progress();
	self.Settings = new Settings();
	self.Updates = new Updates();
	self.SaveSearch = new SaveSearch();
	self.SavedGame = new SavedGame();
	self.CurrencyEditor = new CurrencyEditor();
	self.Modifications = new Modifications();

	// Client startup tasks go here:
	bindDOM();
	initialise();
	getDirectoryPaths();
	getStructures();
};

var Eternity = new Editor();
