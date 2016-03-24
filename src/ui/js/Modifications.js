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

var Modifications = function () {
	var self = this;

	var defaultState = {
		modifications: false
		, saveName: null
		, savedYet: false
		, saving: false
		, switching: false
		, closing: false
	};

	self.state = $.extend({}, defaultState);
	self.html = {};

	self.init = () => {
		self.html.menuSaveModifications.click(self.save.bind(self));
		self.html.saveChanges.click(self.save.bind(self));
		self.html.dontSaveChanges.click(self.discardChanges.bind(self));
		self.html.saveNameBtn.click(self.saveName.bind(self));
		self.html.saveNameDialog.on('shown.bs.modal', () => self.html.newSaveName[0].select());
		self.html.menuOpen.click(self.switchPrompt.bind(self));

		$(document).keyup(e => {
			if (e.ctrlKey === true && e.keyCode === 83) { // Ctrl+s
				self.save();
			}
		});
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);

		if (self.state.modifications) {
			self.html.menuSaveModifications.parent().removeClass('disabled');
		} else {
			self.html.menuSaveModifications.parent().addClass('disabled');
		}

		if (self.state.saving) {
			self.html.saveNameBtn.prop('disabled', true);
		} else {
			self.html.saveNameBtn.prop('disabled', false);
		}
	};
};

Modifications.prototype.suggestSaveName = function (info) {
	return ((info.userSaveName) ? info.userSaveName : info.systemName) + ' (edited)';
};

Modifications.prototype.saveName = function () {
	var self = this;
	self.state.saveName = self.html.newSaveName.val();
	self.save();
};

Modifications.prototype.save = function () {
	var self = this;

	var success = () => {
		self.html.saveNameDialog.modal('hide');
		self.html.saveChangesDialog.modal('hide');

		if (self.state.switching) {
			Eternity.SaveSearch.transition({});
			self.render({});
		} else if (self.state.closing) {
			window.closeWindow({
				request: 'true'
				, onSuccess: () => {}
				, onFailure: () => {}
			});
		} else {
			self.transition({modifications: false, savedYet: true, saving: false});
		}
	};

	var failure = (errno, response) => {
		self.html.saveNameDialog.modal('hide');
		self.html.saveChangesDialog.modal('hide');
		self.transition({saving: false});
		Eternity.GenericError.render({msg: JSON.parse(response).error});
	};

	var prepareData = (data) => {
		var newData = $.extend(true, {}, data);
		newData.characters = data.characters.map(character => {
			for (var stat in character.stats) {
				//noinspection JSUnfilteredForInLoop
				character.stats[stat].value = character.stats[stat].value.toString();
			}

			return character;
		});

		for (var p1 in data.globals) {
			if (!data.globals.hasOwnProperty(p1)) {
				continue;
			}

			for (var p2 in data.globals[p1]) {
				if (!data.globals[p1].hasOwnProperty(p2)) {
					continue;
				}

				for (var p3 in data.globals[p1][p2]) {
					if (!data.globals[p1][p2].hasOwnProperty(p3)) {
						continue;
					}

					newData.globals[p1][p2][p3].value = data.globals[p1][p2][p3].value.toString();
				}
			}
		}

		return newData;
	};

	self.html.saveChangesDialog.modal('hide');

	if (self.state.saving) {
		return;
	}

	if (self.state.saveName == null) {
		self.html.saveNameDialog.modal('show');
		return;
	}

	var request = {
		savedYet: self.state.savedYet
		, saveName: self.state.saveName
		, absolutePath: Eternity.SavedGame.state.info.absolutePath
		, saveData: prepareData(Eternity.SavedGame.state.saveData)
	};

	self.transition({saving: true});
	window.saveChanges({
		request: JSON.stringify(request)
		, onSuccess: success
		, onFailure: failure
	});
};

Modifications.prototype.switchPrompt = function () {
	var self = this;
	self.state.switching = true;

	if (self.state.modifications) {
		self.html.saveChangesDialog.modal('show');
	} else {
		self.discardChanges();
	}
};

Modifications.prototype.closePrompt = function () {
	var self = this;
	self.state.closing = true;

	if (self.state.modifications) {
		self.html.saveChangesDialog.modal('show');
	} else {
		self.discardChanges();
	}
};

Modifications.prototype.discardChanges = function () {
	var self = this;

	if (self.state.switching) {
		Eternity.SaveSearch.transition({});
		self.render({});
		self.html.saveChangesDialog.modal('hide');
	} else if (self.state.closing) {
		window.closeWindow({
			request: 'true'
			, onSuccess: () => {}
			, onFailure: () => {}
		});
	}
};

$.extend(Modifications.prototype, Renderer.prototype);

// Called directly from Java code.
var checkForModifications = function () {
	Eternity.Modifications.closePrompt();
};
