var websocket;

function getWebsocket()
{
	if(websocket == undefined || websocket == null) createWS();
	return websocket;
}

$(document).ready(function(){
	if(websocket == undefined) createWS();
});

function createWS()
{
	websocket = new WebsocketConnection(HOST);
	websocket.connect();
};

function addWebsocketHandler(command, func)
{
	if(websocket == undefined)
		createWS();
	websocket.addHandler(command, func);
};

window.onbeforeunload = function() 
{
	getWebsocket().closeOK = true;
	getWebsocket().runCommand("close");
};

function WebsocketConnection(host)
{
	this.closeOk = false;
	this.host = host;
	this.handler = new Array();
	this.connHandler = new Array();
	this.socket;
	this.connectionRetrys = 0;
};

WebsocketConnection.prototype.connect = function()
{
	var me = this;
	this.socket = new WebSocket("ws://"+this.host); 
	this.socket.onopen = function(evt) {  me.onOpen(evt); };
	this.socket.onclose = function(evt) { me.onClose(evt); };
	this.socket.onmessage = function(evt) { me.onMessage(evt); };
	this.socket.onerror = function(evt) { me.onError; };
};

WebsocketConnection.prototype.addHandler = function(command, func)
{
	if(this.handler == null) this.handler = new Array();
	this.handler[command] = func;
};

WebsocketConnection.prototype.addConnHandler = function(func)
{
	if(this.connHandler == null) this.connHandler = new Array();
	this.connHandler.push(func);
};

WebsocketConnection.prototype.onOpen = function(evt) 
{ 
	if(user == undefined) user = new User();
	for(var i = 0; i < this.connHandler.length; i++)
	{
		this.connHandler[i]();
	}
	
	this.connectionRetrys = 0;
}; 

WebsocketConnection.prototype.onClose = function(evt) 
{ 
	this.connect();
	this.connectionRetrys++;
	//if(!this.closeOk)
	//showMessage(MSG_ERROR, "Verbindung zum Server ist abgebrochen! Verbindung wiederherstellen?", "refreshPage();");
};

WebsocketConnection.prototype.onMessage = function(evt) 
{ 
	console.log("Received:"+evt.data);
	var cmd = evt.data.split(":");
	if(cmd.length > 1) 
	{
		if(this.handler[cmd[0]] != null) this.handler[cmd[0]](cmd[1].split(";"));
		//websocketAction(cmd[0], cmd[1].split(";"));
	}
	else 
	{
		if(this.handler[cmd[0]] != null) this.handler[cmd[0]]();
		//websocketAction(cmd[0], new Array());
	}
};

WebsocketConnection.prototype.onError = function(evt) 
{ 
	console.log(evt)
	this.connect();
};

WebsocketConnection.prototype.runCommand = function(command, param)
{
	var paramString = "";
	if(param != null)
	{
		for(var i = 0; i < param.length - 1; i++)
		{
			paramString += param[i] + ";";
		}
		paramString += param[param.length - 1];
	}
	console.log("Sending:"+command+":"+paramString);
	if(this.socket.readyState != 1) this.connect();
	if(this.socket != null) 
		this.socket.send(command+":"+paramString);
};
WebsocketConnection.prototype.send = function(value)
{
	if(this.socket.readyState != 1) this.connect();
	if(this.socket != null) 
		this.socket.send(value);
};

