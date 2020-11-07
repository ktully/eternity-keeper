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

var SaveSearch = function () {
	var self = this;

	var defaultState = {
		searchPath: ''
		, saves: []
		, searching: false
		, opening: false
	};

	var populateSaveBlocks = (container, template, data, opening) => {
		data.forEach((info, i) => {
			var tile = CloneFactory.clone(template);
			var userSaveName = info.userSaveName ? '(' + info.userSaveName + ')' : '';
			var portraits = info.portraits.map(
					portrait => '<img src="data:image/png;base64,' + portrait + '">');

			if (opening === i) {
				tile.find('i').show();
			}

			tile.find('.screenshot img').attr('src', 'data:image/png;base64,' + info.screenshot);
			tile.find('.name').text(info.playerName + ' - ' + info.systemName + userSaveName);
			tile.find('.date').text(info.date);
			tile.find('.portraits').html(portraits.join(' '));
			tile.click(self.open.bind(self, info, i));
			container.append(tile);
		});
	};

	self.state = $.extend({}, defaultState);
	self.html = {};

	self.init = () => {
		self.html.searchForSavedGames.click(self.search.bind(self));
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.savedGameLocation.val(self.state.searchPath);
		self.html.saveBlocks.empty();
		self.html.saveBlocks.show();
		Eternity.render({listView: true});
		populateSaveBlocks(
			self.html.saveBlocks
			, self.html.saveBlockClone
			, self.state.saves
			, self.state.opening);

		if (self.state.searching) {
			self.html.searchForSavedGames.prop('disabled', true);
			self.html.searchForSavedGames.find('i').css('display', 'inline-block');
			self.html.searchForSavedGames.find('span').text('Searching...');
		} else {
			self.html.searchForSavedGames.prop('disabled', false);
			self.html.searchForSavedGames.find('i').hide();
			self.html.searchForSavedGames.find('span').text('Search');
		}
	};
};

SaveSearch.prototype.search = function () {
	var self = this;
	var interval = null;
	var searchPath = self.html.savedGameLocation.val();

	var success = response => {
		clearInterval(interval);
		Eternity.Progress.render({});
		self.transition({searching: false});

		var saves = JSON.parse(response);
		if (saves.error) {
			Eternity.GenericError.render({msg: 'No saves found.'});
			return;
		}

		self.transition({saves: saves});
	};

	var failure = () => {
		clearInterval(interval);
		Eternity.Progress.render({});
		self.transition({searching: false});
		console.error('Listing saved games failed.');
	};

	var pollForUpdate = () => {
		window.checkExtractionProgress({
			request: "true"
			, onSuccess: response =>
				Eternity.Progress.render({percentage: JSON.parse(response).update})
			, onFailure: console.error.bind(console, 'Error checking for extraction progress.')
		});
	};

	if (searchPath.length < 1 || self.state.searching) {
		return;
	}

	Eternity.GenericError.render({});
	self.transition({searchPath: searchPath, searching: true});
	interval = setInterval(pollForUpdate, 1000);
	window.listSavedGames({
		request: searchPath
		, onSuccess: success
		, onFailure: failure
	});
};

SaveSearch.prototype.open = function (info, i) {
	var self = this;

	var success = response => {
		self.transition({opening: false});
		response = JSON.parse(response);

		if (response.error) {
			var msg;
			if (response.error === 'DESERIALIZATION_ERR') {
				msg = 'Error deserializing save file, please report your eternity.log file.';
			} else if (response.error === 'NOT_EXISTS') {
				msg = 'Save file "' + info.absolutePath + '" does not exist.';
			} else if (response.msg) {
				msg = response.msg;
			} else {
				msg = 'Unknown error when opening save file, please report your eternity.log file.';
			}

			Eternity.GenericError.render({msg: msg});

			// TODO: move this when flow changes to allow user confirm whether to convert or not
			if (response.error === 'WINDOWS_STORE_SAVE') {
				self.search();
			}
		} else if (response.characters.length < 1) {
			Eternity.GenericError.render({msg: 'No characters found in save game.'});
		} else if (response.isWindowStoreSave) {
			// TODO: redirect to new modal dialog which optionally kicks off the conversion and blocks until conversion is complete
			Eternity.GenericError.render({msg: 'Windows Store save selected'});
		} else {
			Eternity.SavedGame.render({saveData: response, info: info});
		}
	};

	var failure = () => {
		self.transition({opening: false});
		Eternity.GenericError.render({msg: 'Error opening saved game file: ' + info.absolutePath});
	};

	if (self.state.opening !== false) {
		return;
	}

	Eternity.GenericError.render({});
	self.transition({opening: i});
	window.openSavedGame({
		request: info.absolutePath
		, onSuccess: success
		, onFailure: failure
	});
};

$.extend(SaveSearch.prototype, Renderer.prototype);
