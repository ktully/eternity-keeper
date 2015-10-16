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


var CheckUpdates = function () {
	var self = this;
	var isChecking = false;
	var isDownloading = false;
	var checkInterval = null;
	var downloadingInterval = null;
	var currentProgress = 0;

	var contents = $('#checkUpdatesContents');
	var buttons = $('#checkUpdatesDialog').find('.modal-footer');
	var btnTryAgain = $('#checkUpdatesTryAgain');
	var btnDownload = $('#checkUpdatesDownload');

	var updateProgress = function () {
		contents.find('span').text(currentProgress + '%');
	};

	var resetProgress = function (msg) {
		buttons.hide();
		contents.html(msg + '... <span></span>');
		currentProgress = 0;
		updateProgress();
	};

	var progress = function () {
		currentProgress++;
		if (currentProgress > 9) {
			currentProgress = 9;
		}

		updateProgress();
	};

	var notChecking = function () {
		isChecking = false;
		clearInterval(checkInterval);
	};

	var tryAgain = function () {
		buttons.hide();
		resetProgress();
		doCheckForUpdates();
	};

	var checkingFailed = function () {
		notChecking();
		buttons.show();
		btnTryAgain().show();
		btnDownload.hide();
		contents.html('<span class="danger">There was an error checking for updates...</span>');
	};

	var notDownloading = function () {
		isDownloading = false;
		clearInterval(downloadingInterval);
	};

	var downloadFailed = function () {
		notDownloading();
		buttons.show();
		btnTryAgain.show();
		btnDownload.hide();
		contents.html(
			'<span class="danger"><strong>Error!</strong> '
			+ 'Download failed, please download manually.</span>');
	};

	var downloadComplete = function () {
		notDownloading();
		buttons.hide();
		contents.html('Eternity Keeper was successfully updated, please restart it when ready.');
	};

	var updateDownloadProgress = function (responseText) {
		console.log(responseText);
		var response = JSON.parse(responseText);
		if (response.percentage == 100) {
			downloadComplete();
			return;
		}

		currentProgress = response.percentage.toFixed(2);
		updateProgress();
	};

	var downloadCheck = function () {
		window.checkDownloadProgress({
			request: 'true'
			, onSuccess: updateDownloadProgress
			, onFailure: downloadFailed
		});
	};

	var startChecking = function (responseText) {
		downloadingInterval = setInterval(downloadCheck, 4000);
	};

	var download = function (timestamp, e) {
		resetProgress('Downloading');
		buttons.hide();

		isDownloading = true;
		window.downloadUpdate({
			request: timestamp
			, onSuccess: startChecking
			, onFailure: downloadFailed
		});
	};

	var updatesAvailable = function (timestamp) {
		buttons.show();
		contents.html('There is an update available!');
		btnTryAgain.hide();
		btnDownload.show();
		btnDownload.off();
		btnDownload.click(download.bind(self, timestamp));
	};

	var alreadyUpToDate = function () {
		buttons.show();
		$('#checkUpdatesTryAgain').show();
		contents.html('Eternity Keeper is up to date!');
	};

	var doneChecking = function (responseText) {
		notChecking();
		var response = JSON.parse(responseText);
		if (response.available === true) {
			updatesAvailable(response.timestamp);
		} else {
			alreadyUpToDate();
		}
	};

	var doCheckForUpdates = function () {
		isChecking = true;
		checkInterval = setInterval(progress, 400);

		window.checkForUpdates({
			request: 'true'
			, onSuccess: doneChecking
			, onFailure: checkingFailed
		});
	};

	var openDialog = function () {
		$('#checkUpdatesDialog').modal('show');
		if (!isChecking && !isDownloading) {
			resetProgress('Checking');
			doCheckForUpdates();
		}
	};

	$('#menu-check-updates').click(openDialog);
	btnTryAgain.click(tryAgain);
};

var checkUpdates = new CheckUpdates();
