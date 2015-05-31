var SavedGame = function (info) {
	var self = this;
	self.info = info;
	self.characterData = null;
	self.modifications = false;

	var switchCharacter = function (e) {
		var guid = $(e.currentTarget).attr('data-guid');
		$('.characters li').removeClass('active');
		$(e.currentTarget).addClass('active');
		var character = self.characterData.filter(function (c) {
			return c.GUID === guid;
		}).shift();

		populateCharacter(character);
	};

	var populateCharacterList = function () {
		var items = self.characterData.map(function (character) {
			return $('<li>')
				.attr('data-guid', character.GUID)
				.text(character.name);
		});

		$('.characters').empty().append(items);
		$('.characters li').click(switchCharacter);
	};

	var updateData = function (e) {
		var el = $(e.currentTarget);
		var type = el.attr('type');
		var key = el.attr('data-key');
		var value = el.val();

		if (key === null || key.length < 1) {
			return;
		}

		if (type === 'number') {
			value = parseInt(value);
			if (isNaN(value)) {
				return;
			}
		}

		var guid = $('.characters .active').attr('data-guid');
		var character = self.characterData.filter(function (c) {
			return c.GUID === guid;
		}).shift();

		character.stats[key] = value;
		self.modifications = true;
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
                        .attr('value', character.stats[stat])
                        .attr('data-key', stat)
                        .addClass('form-control'))));
		}

		$('.stats input').change(updateData);
		$('.stats input').keyup(updateData);
	};

	var loadUI = function (response) {
		savesManager.notOpening();
		var characters = JSON.parse(response);

		if (characters.length < 1) {
			errorShow('No characters found in save game.');
			return;
		}

		self.characterData = characters;
		savesManager.switchToSavedGameContext();

		$('.character').show();
		populateCharacterList();
		$('.characters li').first().addClass('active');
		populateCharacter(characters[0]);
	};

	window.openSavedGame({
		request: info.absolutePath
		, onSuccess: loadUI
		, onFailure: console.error.bind(
			console
			, 'Error opening saved game %s.'
			, info.absolutePath)
	});
};
