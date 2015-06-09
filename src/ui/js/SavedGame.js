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
		var key = el.attr('data-key');
		var value = el.val();

		if (key === null || key.length < 1) {
			return;
		}

		var guid = $('.characters .active').attr('data-guid');
		var character = self.characterData.filter(function (c) {
			return c.GUID === guid;
		}).shift();

		character.stats[key] = value.toString();
		self.modifications = true;
	};

	var populateCharacter = function (character) {
		$('.character .portrait').empty().append(
			$('<img>').attr(
				'src'
				, 'data:image/png;base64,' + character.portrait));

		for (var stat in character.stats) {
			$('.character .stats')
				.find('input[data-key="' + stat + '"]')
				.val(character.stats[stat]);
		}

		$('.stats input').change(updateData);
		$('.stats input').keyup(updateData);
	};

	self.loadUI = function (response) {
		savesManager.notOpening();
		var characters = JSON.parse(response);

		if (characters.length < 1) {
			errorShow('No characters found in save game.');
			return;
		}

		self.characterData = characters.map(function (character) {
			// Hate to lose type information here but HTML forms won't preserve it for us anyway.
			for (stat in character.stats) {
				character.stats[stat] = character.stats[stat].toString();
			}

			return character;
		});

		savesManager.switchToSavedGameContext();

		$('.character').show();
		populateCharacterList();
		$('.characters li').first().addClass('active');
		populateCharacter(characters[0]);
	};

	window.openSavedGame({
		request: info.absolutePath
		, onSuccess: self.loadUI
		, onFailure: console.error.bind(
			console
			, 'Error opening saved game %s.'
			, info.absolutePath)
	});
};
