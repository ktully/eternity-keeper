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

var Settings = function () {
	var self = this;

	var defaultState = {
		gameLocation: ''
		, saving: false
	};

	self.state = $.extend({}, defaultState);
	self.html = {};

	self.init = () => {
		self.html.saveSettings.click(self.save.bind(self));
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.gameLocation.val(self.state.gameLocation);

		if (self.state.saving) {
			self.html.saveSettings.prop('disabled', true);
			self.html.saveSettings.find('i').show();
			self.html.saveSettings.find('span').text('Saving...');
		} else {
			self.html.saveSettings.prop('disabled', false);
			self.html.saveSettings.find('i').hide();
			self.html.saveSettings.find('span').text('Save');
		}

		if (self.state.gameLocation.length < 1) {
			self.html.settingsDialog.modal('show');
		}
	};
};

Settings.prototype.save = function () {
	var self = this;

	var data = {
		gameLocation: self.html.gameLocation.val()
	};

	var success = () => {
		self.transition({saving: false});
		self.html.settingsDialog.modal('hide');
	};

	var failure = () => {
		self.transition({saving: false});
		console.error('Error saving settings.');
	};

	if (self.state.saving) {
		return;
	}

	self.transition({gameLocation: data.gameLocation, saving: true});
	window.saveSettings({
		request: JSON.stringify(data)
		, onSuccess: success
		, onFailure: failure
	});
};

$.extend(Settings.prototype, Renderer.prototype);
