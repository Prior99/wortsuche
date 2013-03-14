var MSG_OK = 0;
var MSG_ERROR = 1;

function showMessage(style, message, href)
{
	var blend;
	if(style == MSG_OK) 
	{
		blend = "msgBlendOk";
		style = "msgOk";
	}
	else 
	{
		blend = "msgBlendError";
		style = "msgError";
	}
	var code = "<div id=\"msg\" class=\"" + style + "\">" + message;
	if(href != null) code += '  <button style=\"float: right;\" onclick="javascript: ' + href + ';">OK</button>';
	else
		code += '  <button style=\"float: right;\" onclick="javascript: $(\'#blend\').remove(); $(\'#msg\').remove();">OK</button>';
	code += "</div>";
	$('body').append(code).append("<div id=\"blend\" class=\"" + blend + "\"></div>");
};

function removeMessage()
{
	$('#msg').remove();
	$('#blend').fadeOut('fast');
};