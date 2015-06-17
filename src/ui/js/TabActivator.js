var TabActivator = function () {
	var activateTab = function (e) {
		var target = $(e.currentTarget).attr('target');
		$('.character').find('.section').hide();
		$(target).show();
		$('.nav-tabs').find('li').removeClass('active');
		$(e.currentTarget).parent().addClass('active');
		savesManager.currentSavedGame.tabSwitch();
	};

	$('.nav-tabs').find('a').click(activateTab);
};

var tabActivator = new TabActivator();
