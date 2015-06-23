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
	var currentProgess = 0;

	var updateProgress = function () {
		$('#checkUpdatesProgress').find('div').css('width', currentProgess * 10 + '%');
	};

	var resetProgress = function () {
		currentProgess = 0;
		updateProgress();
	};

	var progress = function () {
		currentProgess++;
		if (currentProgess > 9) {
			currentProgess = 9;
		}

		updateProgress();
	};

	var notChecking = function () {
		isChecking = false;
		clearInterval(checkInterval);
	};

	var tryAgain = function () {
		//noinspection JSJQueryEfficiency
		$('#checkUpdatesDialog .modal-footer'
		+ ', #checkUpdatesTryAgain'
		+ ', #checkUpdatesError'
		+ ', #checkUpdatesNoUpdates'
		+ ', #checkUpdatesAvailable').hide();

		$('#checkUpdatesProgress').show().find('div').text('Checking...');
		resetProgress();
		doCheckForUpdates();
	};

	var checkingFailed = function () {
		notChecking();
		$('#checkUpdatesDialog').find('.modal-footer').show();
		$('#checkUpdatesTryAgain').show();
		$('#checkUpdatesError').show();
		$('#checkUpdatesProgress').hide();
	};

	var notDownloading = function () {
		isDownloading = false;
		clearInterval(downloadingInterval);
	};

	var downloadFailed = function () {
		notDownloading();
		$('#checkUpdatesProgress').hide();
		$('#checkUpdatesDialog').find('.modal-footer').show();
		$('#checkUpdatesDownloadFailed').show();
		$('#checkUpdatesTryAgain');
	};

	var downloadComplete = function () {
		notDownloading();
		$('#checkUpdatesProgress').hide();
		$('#checkUpdatesDownloadSuccess').show();
	};

	var updateDownloadProgress = function (responseText) {
		var response = JSON.parse(responseText);
		if (response.percentage == 100) {
			downloadComplete();
			return;
		}

		var percentage = response.percentage.toFixed(2) + '%';
		$('#checkUpdatesProgress').find('div').css('width', percentage).text(percentage);
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

	var download = function (jar, e) {
		resetProgress();
		$('#checkUpdatesAvailable, #checkUpdatesDialog .modal-footer').hide();
		$('#checkUpdatesProgress').show().find('div').text('0%');

		isDownloading = true;
		window.downloadUpdate({
			request: jar
			, onSuccess: startChecking
			, onFailure: downloadFailed
		});
	};

	var updatesAvailable = function (jar) {
		$('#checkUpdatesProgress').hide();
		$('#checkUpdatesDialog').find('.modal-footer').show();
		$('#checkUpdatesAvailable').show();

		var downloadBtn = $('#checkUpdatesDownload');
		downloadBtn.show();
		downloadBtn.off();
		downloadBtn.click(download.bind(self, jar));
	};

	var alreadyUpToDate = function () {
		$('#checkUpdatesProgress').hide();
		$('#checkUpdatesDialog').find('.modal-footer').show();
		$('#checkUpdatesTryAgain').show();
		$('#checkUpdatesNoUpdates').show();
	};

	var doneChecking = function (responseText) {
		notChecking();
		var response = JSON.parse(responseText);
		if (response.available === true) {
			updatesAvailable(response.jar);
		} else {
			alreadyUpToDate();
		}
	};

	var doCheckForUpdates = function () {
		isChecking = true;
		checkInterval = setInterval(progress, 1000);

		window.checkForUpdates({
			request: 'true'
			, onSuccess: doneChecking
			, onFailure: checkingFailed
		});
	};

	var openDialog = function () {
		$('#checkUpdatesDialog').modal('show');
		if (!isChecking && !isDownloading) {
			resetProgress();
			doCheckForUpdates();
		}
	};

	$('#menu-check-updates').click(openDialog);
	$('#checkUpdatesTryAgain').click(tryAgain);
};

var checkUpdates = new CheckUpdates();
