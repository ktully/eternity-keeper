var CurrencyEditor = function () {
	var self = this;

	self.open = function () {
		$('#currencyEditor').modal('show');
	};

	var cancel = function () {
		$('#currencyEditor').modal('hide');
	};

	var updateCurrency = function () {
		savesManager.currentSavedGame.saveData.currency = parseInt($('#currency').val());
	};

	$('#currencyEditorCancel').click(cancel);
	$('#currencyEditorSet').click(updateCurrency);
};

var currencyEditor = new CurrencyEditor();
