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

var CurrencyEditor = function () {
	var self = this;

	var defaultState = {
		enabled: false
		, amount: -1
	};

	self.state = $.extend({}, defaultState);
	self.html = {};

	self.init = () => {
		self.html.currencyEditorCancel.click(self.close.bind(self));
		self.html.currencyEditorSet.click(self.update.bind(self));
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.currency.val(parseInt(self.state.amount));
		self.html.menuCurrencyEditor.off();
		self.html.menuCurrencyEditor.click(self.open.bind(self));

		if (self.state.enabled) {
			self.html.menuCurrencyEditor.parent().removeClass('disabled');
		} else {
			self.html.menuCurrencyEditor.parent().addClass('disabled');
		}
	};
};

CurrencyEditor.prototype.open = function () {
	var self = this;
	self.html.currencyDialog.modal('show');
	self.transition({});
};

CurrencyEditor.prototype.close = function () {
	var self = this;
	self.html.currencyDialog.modal('hide');
};

CurrencyEditor.prototype.update = function () {
	var self = this;
	self.state.amount = parseInt(self.html.currency.val());
	Eternity.SavedGame.state.saveData.currency = self.state.amount;
	self.close();
};

$.extend(CurrencyEditor.prototype, Renderer.prototype);
