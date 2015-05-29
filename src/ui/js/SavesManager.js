var SavesManager = function () {
	var self = this;

	var saveBlock =
		$('<div class="save-info">'
			+ '<table>'
				+ '<tbody>'
					+ '<tr>'
						+ '<td rowspan="2" class="screenshot"></td>'
						+ '<td colspan="2" class="name"></td>'
					+ '</tr>'
					+ '<tr>'
						+ '<td class="portraits"></td>'
						+ '<td class="date"></td>'
					+ '</tr>'
				+ '</tbody>'
			+ '</table>'
			+ '<i class="fa fa-spinner fa-pulse"></i>'
		+ '</div>');

	var savedGameLocationField = $('#savedGameLocation');
	var gameLocationField = $('#gameLocation');
	var isProcessing = false;

	var searching = function () {
		isProcessing = true;
		$('#searchForSavedGames')
		.prop('disabled', true)
		.html('<i class="fa fa-spinner fa-pulse" style="display: inline-block;">'
			+ '</i> Searching...');
	};

	var notSearching = function () {
		isProcessing = false;
		$('#searchForSavedGames')
		.prop('disabled', false)
		.html('<i class="fa fa-spinner fa-pulse" style="display: none;">'
			+ '</i> Search');
	};

	var listSavedGamesFailed = function () {
		notSearching();
		console.error('Error listing saved games.');
	};

	var opening = function (el) {
		isProcessing = true;
		$(el).find('i').show();
	};

	self.notOpening = function () {
		isProcessing = false;
		$('.save-info i').hide();
	};

	var openSavedGame = function (info, e) {
		if (isProcessing) {
			return;
		}

		opening(e.currentTarget);
		new SavedGame(info.absolutePath)
	};

	var generateSaveGameTiles = function (response) {
		notSearching();

		var saveInfo = JSON.parse(response);
		$('.save-blocks').empty();

		if (saveInfo.error) {
			errorShow('No saves found.');
			return;
		}

		saveInfo.forEach(function (info) {
			var saveTile = saveBlock.clone();

			saveTile.find('.screenshot').append(
				$('<img>').attr(
					'src'
					, 'data:image/png;base64,' + info.screenshot));

			saveTile.find('.name').text(
				info.playerName
				+ ' - '
				+ info.systemName
				+ ((info.userSaveName) ? ' (' + info.userSaveName + ')' : ''));

			saveTile.find('.date').text(info.date);

			var portraits = info.portraits.map(function (portrait) {
				return '<img src="data:image/png;base64,' + portrait + '">';
			});

			saveTile.find('.portraits').html(portraits.join(' '));
			saveTile.click(openSavedGame.bind(self, info));
			$('.save-blocks').append(saveTile);
		});
	};

	var listSavedGames = function () {
		if (isProcessing) {
			return;
		}

		errorHide();
		searching();
		window.listSavedGames({
			request: $('#savedGameLocation').val()
			, onSuccess: generateSaveGameTiles
			, onFailure: listSavedGamesFailed
		});
	};

	var updateSaveLocation = function (response) {
		response = JSON.parse(response);
		if (!response.savesLocation || response.savesLocation.length < 1) {
			savedGameLocationField.attr(
                'placeholder'
                , 'Unable to locate save folder');
		} else {
			savedGameLocationField.val(response.savesLocation);
            listSavedGames();
		}

		if (!response.gameLocation || response.gameLocation.length < 1) {
			gameLocationField.attr(
				'placeholder'
				, 'Unable to locate installation folder');

			$('#settings').modal('show');
		} else {
			gameLocationField.val(response.gameLocation);
		}
	};

	window.getDefaultSaveLocation({
		request: 'default'
		, onSuccess: updateSaveLocation
		, onFailure: console.error.bind(
			console
			, 'Error getting default save location.')
	});

	$('#searchForSavedGames').click(listSavedGames);
};

var savesManager = new SavesManager();
