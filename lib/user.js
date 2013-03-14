var user;// = new User();


function getUser()
{
	console.log(user);
	if(user == undefined) user = new User();
	return user;
};

function User()
{
	var username = getCookie("username");
	var password = getCookie("password");
	if(username != null && password != null)
	{
		getWebsocket().runCommand("login", [username, password]);
		this.performLogin(username, password);
	}
};


User.prototype.performLogin = function(username, password)
{
	this.username = username;
	this.password = password;
	setCookie("username", username, 3);
	setCookie("password", password, 3);
};

function logout()
{
	eraseCookie("username");
	eraseCookie("password");
};
