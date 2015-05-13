var SavesManager = function () {
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
		+ '</div>');

	var savedGameLocationField = $('#savedGameLocation');
	var isSearching = false;

	var searching = function () {
		isSearching = true;
		$('#searchForSavedGames')
		.prop('disabled', true)
		.html('<i class="fa fa-spinner fa-pulse" style="display: inline-block;">'
			+ '</i> Searching...');
	};

	var notSearching = function () {
		isSearching = false;
		$('#searchForSavedGames')
		.prop('disabled', false)
		.html('<i class="fa fa-spinner fa-pulse" style="display: none;">'
			+ '</i> Search');
	};

	var listSavedGamesFailed = function () {
		notSearching();
		console.error('Error listing saved games.');
	};

	var generateSaveGameTiles = function (response) {
		notSearching();

		var saveInfo = JSON.parse(response);
		$('.save-blocks').empty();

		if (saveInfo.error) {
			$('.save-blocks')
				.append($('<div class="alert alert-warning">'
					+ 'No saves found</div>'));

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
			$('.save-blocks').append(saveTile);
		});
	};

	var listSavedGames = function () {
		if (isSearching) {
			return;
		}

		searching();
		window.listSavedGames({
			request: $('#savedGameLocation').val()
			, onSuccess: generateSaveGameTiles
			, onFailure: listSavedGamesFailed
		});
	};

	var updateSaveLocation = function (response) {
		response = JSON.parse(response);
		if (response.error) {
			savedGameLocationField.attr(
				'placeholder'
				, 'Unable to locate save folder');
		} else if (response.location) {
			savedGameLocationField.val(response.location);
			listSavedGames();
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

new SavesManager();
