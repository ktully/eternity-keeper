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

var Editor = () => {
	var self = this;

	var bindDOM = () => {
		// Do one pass over the DOM at startup to bind all the UI elements that we need.
		$('[data-bound]').each(element => {
			var boundTo = element.data('bound');
			var id = element.attr('id');

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

	self.state = {};

	// Component instantiation goes here:
	self.SaveSearch = new SaveSearch();

	// Client startup tasks go here:
	bindDOM();
	initialise();
};

var Eternity = new Editor();
