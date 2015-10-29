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

var SaveSearch = () => {
	var self = this;

	var defaultState = {
		searchPath: ''
		, saves: []
		, searching: false
	};

	var populateSaveBlocks = (container, template, data) => {
		data.forEach(info => {
			var tile = CloneFactory.clone(template);
			var userSaveName = info.userName ? '(' + info.userName + ')' : '';
			var portraits = info.portraits.map(
					portrait => '<img src="data:image/png;base64,' + portrait + '">');
			tile.find('.screenshot img').attr('src', 'data:image/png;base64,' + info.screenshot);
			tile.find('.name').text(info.playerName + ' - ' + info.systemName + userSaveName);
			tile.find('.date').text(info.date);
			tile.find('.portraits').html(portraits.join(' '));
			container.append(tile);
		});
	};

	self.state = defaultState;
	self.html = {};
	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.savedGameLocation.val(self.state.searchPath);
		self.html.saveBlocks.empty();
		self.html.saveBlocks.show();
		populateSaveBlocks(self.html.saveBlocks, self.html.saveBlockClone, self.state.saves);
	};
};

SaveSearch.prototype.search = () => {
	var self = this;
	self.transition({searching: true});
};

$.extend(SaveSearch.prototype, Renderer.prototype);
