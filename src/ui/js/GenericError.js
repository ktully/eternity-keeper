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

var GenericError = function () {
	var self = this;

	var defaultState = {
		msg: ''
	};

	self.state = defaultState;
	self.html = {};
	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		if (self.state.msg.length > 0) {
			self.html.error.show();
			self.html.error.find('div').text(self.state.msg);
		} else {
			self.html.error.hide();
		}
	};
};

$.extend(GenericError.prototype, Renderer.prototype);
