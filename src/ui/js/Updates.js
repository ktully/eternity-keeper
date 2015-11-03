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

var Updates = function () {
	var self = this;

	var defaultState = {
		checking: false
		, checkProgress: 0
		, checkingError: false
		, downloading: false
		, downloadProgress: 0
		, downloadError: false
		, downloadComplete: false
		, legacyMessage: false
		, upToDate: false
		, updateAvailable: false
		, updateTimestamp: -1
	};

	self.state = defaultState;
	self.html = {};

	self.init = () => {
		self.html.menuCheckUpdates.click(self.open.bind(self));
		self.html.updatesTryAgain.click(self.check.bind(self));
		self.html.updatesDownload.click(self.download.bind(self));
	};

	self.render = newState => {
		self.state = $.extend({}, defaultState, newState);
		self.html.updatesDialogButtons.hide();
		self.html.updatesDialogButtons.find('button').hide();

		var contents = '';

		if (self.state.checking) {
			contents = 'Checking... ' + self.state.checkProgress + '%';
		}

		if (self.state.downloading) {
			contents = 'Downloading...' + self.state.downloadProgress + '%';
		}

		if (self.state.upToDate
			|| self.state.legacyMessage
			|| self.state.checkingError
			|| self.state.downloadError) {

			self.html.updatesDialogButtons.show();
			self.html.updatesTryAgain.show();
		}

		if (self.state.checkingError) {
			contents = '<span class="danger">There was an error checking for updates...</span>';
		}

		if (self.state.downloadError) {
			contents = '<span class="danger"><strong>Error!</strong> '
				+ 'Download failed, please download manually.</span>'
		}

		if (self.state.legacyMessage) {
			contents = 'This update requires a manual download and reinstall of Eternity Keeper. '
				+ 'Please make sure you completely delete the old Eternity Keeper directory.'
		}

		if (self.state.upToDate) {
			contents = 'Eternity Keeper is up to date!';
		}

		if (self.state.downloadComplete) {
			contents = 'Eternity Keeper was successfully updated, please restart it when ready.';
		}

		if (self.state.updateAvailable) {
			self.html.updatesDialogButtons.show();
			self.html.updatesDownload.show();
			contents = 'There is an update available!';
		}

		self.html.updatesDialogContents.html(contents);
	};
};

Updates.prototype.open = function () {
	var self = this;

	self.html.updatesDialog.modal('show');
	if (!self.state.checking && !self.state.downloading) {
		self.check();
	}
};

Updates.prototype.check = function () {
	var self = this;
	var interval = null;

	var success = response => {
		clearInterval(interval);
		response = JSON.parse(response);

		if (response.legacy) {
			self.render({legacyMessage: true});
		} else if (response.available) {
			self.render({updateAvailable: true, updateTimestamp: response.timestamp});
		} else {
			self.render({upToDate: true});
		}
	};

	var failure = () => {
		clearInterval(interval);
		self.render({checkingError: true});
	};

	var progress = () => {
		var percentage = self.state.checkProgress;
		percentage += 6;

		if (percentage > 96) {
			percentage = 96;
		}

		self.transition({checkProgress: percentage});
	};

	self.render({checking: true});
	interval = setInterval(progress, 400);
	window.checkForUpdates({
		request: 'true'
		, onSuccess: success
		, onFailure: failure
	});
};

Updates.prototype.download = function () {
	var self = this;
	var interval = null;

	var success = () => {
		interval = setInterval(checkProgress, 1000);
	};

	var failure = () => {
		clearInterval(interval);
		self.render({downloadError: true});
	};

	var checkProgress = () => {
		window.checkDownloadProgress({
			request: 'true'
			, onSuccess: progress
			, onFailure: failure
		});
	};

	var progress = response => {
		response = JSON.parse(response);

		if (response == 100) {
			clearInterval(interval);
			self.render({downloadComplete: true});
		} else {
			self.transition({downloadProgress: response.percentage.toFixed(2)});
		}
	};

	if (self.updateTimestamp < 0) {
		return;
	}

	self.render({downloading: true});
	window.downloadUpdate({
		request: self.state.updateTimestamp.toString()
		, onSuccess: success
		, onFailure: failure
	});
};

$.extend(Updates.prototype, Renderer.prototype);
