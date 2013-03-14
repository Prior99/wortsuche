function markInputOkay(input)
{
	$('#'+input).removeClass("inputWrong").addClass("inputOkay");
};

function markInputWrong(input)
{
	$('#'+input).removeClass("inputOkay").addClass("inputWrong");
};