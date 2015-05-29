var SavedGame = function (absolutePath) {
	var populateCharacterList = function (characters) {
		var items = characters.map(function (character) {
			return $('<li>')
				.attr('data-guid', character.GUID)
				.text(character.name);
		});

		$('.characters').empty().append(items);
	};

	var populateCharacter = function (character) {
		$('.character .portrait').empty().append(
			$('<img>').attr(
				'src'
				, 'data:image/png;base64,' + character.portrait));

		$('.character .stats form').empty();
		for (var stat in character.stats) {
			var statName = stat.replace(/Base/g, '');
			$('.character .stats form').append(
				$('<div>')
				.addClass('form-group form-group-sm')
				.append(
					$('<label>')
					.addClass('col-sm-2 control-label')
					.text(statName))
				.append(
                    $('<div>')
                    .addClass('col-sm-2')
                    .append(
                        $('<input>')
                        .attr('type', 'number')
                        .addClass('form-control'))
                        .val(character.stats[stat])));
		}
	};

	var loadUI = function (response) {
		savesManager.notOpening();
		var characters = JSON.parse(response);

		if (characters.length < 1) {
			errorShow('No characters found in save game.');
			return;
		}

		errorHide();
		$('.saved-game-locator').hide();
		$('.save-blocks').hide();
		$('.character').show();
		$('#menu-save-modification').removeClass('disabled');

		populateCharacterList(characters);
		$('.characters li').first().addClass('active');
		populateCharacter(characters[0]);
	};

	window.openSavedGame({
		request: absolutePath
		, onSuccess: loadUI
		, onFailure: console.error.bind(
			console
			, 'Error opening saved game %s.'
			, absolutePath)
	});
};
