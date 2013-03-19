function infoPopup(x, y, msg)
{
	var pop = $('<div class="infoPopup">')
		.appendTo('#wrapper')
		.text(msg)
		.css("left", x + "px")
		.css("top", y +"px")
		.animate({left: 0+"px", top: -30 + "px", opacity: 0}, 2000, 
			function()
			{
				pop.remove();
			});
		
};

function infoPopup2(x, y, msg)
{
	var pop = $('<div class="infoPopup2">')
		.appendTo('#wrapper')
		.text(msg)
		.css("left", x + "px")
		.css("top", y +"px")
		.animate({top: (y - 50) + "px", opacity: 0, fontWeight: "bold"}, 2000, 
			function()
			{
				pop.remove();
			});
		
};