function settings()
{
	$("#red, #green, #blue").slider({
		orientation : "horizontal",
		range : "min",
		max : 255,
		min: 0,
		value : 127,
		slide : refreshSwatch,
		change : refreshSwatch
	});
	$("#red").slider("value", 255);
	$("#green").slider("value", 140);
	$("#blue").slider("value", 60);
	getWebsocket().addConnHandler(function(){
		getWebsocket().runCommand("requestColor");
	});
	getWebsocket().addHandler("color", function(param)
	{
		$("#red").slider("value", parseInt(param[0]));
		$("#green").slider("value",parseInt(param[1]));
		$("#blue").slider("value", parseInt(param[2]));
	});
};

function saveColor()
{
	websocket.runCommand("color", [$("#red").slider("value"), $("#green").slider("value"), $("#blue").slider("value")]);

	showMessage(MSG_OK, "Farbe gespeichert!", "removeMessage();");
};

function hexFromRGB(r, g, b)
{
	var hex = [r.toString(16), g.toString(16), b.toString(16)];
	$.each(hex, function(nr, val)
	{
		if(val.length === 1)
		{
			hex[nr] = "0" + val;
		}
	});
	return hex.join("").toUpperCase();
}
function refreshSwatch()
{
	var red = $("#red").slider("value"), green = $("#green").slider("value"), blue = $("#blue").slider("value"), hex = hexFromRGB(
			red, green, blue);
	$("#color").css("background-color", "#" + hex);
}