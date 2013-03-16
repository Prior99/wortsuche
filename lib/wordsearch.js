var CHAR_SIZE = 15;

function Wordsearch(canvas)
{
	this.overAllWords = 0;
	this.allWords = new Array();
	this.color = "black";
	this.canvas = canvas;
	this.width = 0;
	this.height = 0;
	this.charSize = CHAR_SIZE;
	this.ctx = canvas.getContext("2d");
	var self = this;
	this.mouseXend = -1;
	this.mouseYend = -1;
	this.pressed = false;
	this.updateInfo();
	this.canvas.addEventListener("mousemove", function(event) { self.mouseMove(
			Math.floor(((event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - canvas.offsetParent.offsetLeft) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5), 
			Math.floor(((event.clientY + document.body.scrollTop + document.documentElement.scrollTop - canvas.offsetParent.offsetTop) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5)); event.preventDefault();}, false);
	this.canvas.addEventListener("mousedown", function(event) { self.mouseDown(
			Math.floor(((event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - canvas.offsetParent.offsetLeft) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5), 
			Math.floor(((event.clientY + document.body.scrollTop + document.documentElement.scrollTop - canvas.offsetParent.offsetTop) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5)); event.preventDefault();}, false);
	this.canvas.addEventListener("mouseup", function(event) { self.mouseUp(); event.preventDefault();}, false);
};
/*** Called from websocket when it is open. Registers all Listeners to the websockets and asks it for initialization ***/
Wordsearch.prototype.initialize = function()
{
	$('#canvas').css('display','none');
	$('#info').css('display','none');
	$('#words').css('display','none');
	$('#clear').css('display','none');
	$('#chat').css('display','none');
	$('#players').css('display','none');
	$('#selection').css('display','none');
	$('#loading').css('display','block');
	$('#timer').css('display','none');
	self = this;
	$('#status').html("Warten auf Server");
	websocket.runCommand("getGame");
	websocket.addHandler("game", function (param) 
	{
		self.createArray(param);
	});
	websocket.addHandler("words", function (param) 
	{
		self.setWords(param[0].split(","), parseInt(param[1]));
	});
	websocket.addHandler("remove", function (param) 
	{
		self.removeWord(param[0], parseInt(param[1]), parseInt(param[2]), parseInt(param[3]), parseInt(param[4]), param[5]);
	});
	websocket.addHandler("select", function (param) 
	{
		self.select(parseInt(param[0]), parseInt(param[1]), parseInt(param[2]), parseInt(param[3]), param[4]);
	});
	websocket.addHandler("color", function (param) 
	{
		self.color = param[0];
	});
	websocket.addHandler("ready", function () 
	{
	});
	websocket.addHandler("newGame", function () 
	{
		self.initialize();
	});
	websocket.addHandler("scoreinc", function (param) 
	{
		infoPopup2(self.mouseXend*self.charSize, self.mouseYend*self.charSize - 15, "+"+param[0]);
	});
	websocket.addHandler("users", function (param) 
	{
		self.refreshUserDiv(param);
	});
	$('#send').keyup(function(e)
	{
		if(e.which == 13)
		{
			websocket.runCommand("chat",[$('#send').val()]);
			$('#send').val("");
		}
	});
	websocket.addHandler("chat", function (param) 
	{
		chat(param[0], param[1], param[2]);
	});
};

function chat(username, color, msg)
{
	var date = new Date();
	var hours = date.getHours();
	hours = hours < 10 ? "0"+hours : hours;
	var minutes = date.getMinutes();
	minutes = minutes < 10 ? "0"+minutes : minutes;
	var seconds = date.getSeconds();
	seconds = seconds < 10 ? "0"+seconds : seconds;
	$('<div class="chatMsg">')
		.appendTo('#chatcontent')
		.html("<span style='font-weight: bold; color: "+color+";'>"+username+"</span> <span style='color:grey;font-style: italic;'>"+hours+":"+minutes+":"+seconds+"</span>" +
				"<div style='margin-left: 10px;'>"+msg+"</siv>");
	var objDiv = document.getElementById("chatcontent");
	objDiv.scrollTop = objDiv.scrollHeight;
};

Wordsearch.prototype.refreshUserDiv = function(param)
{
	var content = "";
	for(var i = 0; i< param.length; i++)
	{
		var comp = param[i].split("#");
		content += "<div class='user'><div class='colorpreview' style='background-color: "+comp[1]+";'></div>"+comp[0]+"</div>";
	}
	document.getElementById("players").innerHTML = content;
};

Wordsearch.prototype.loaded = function()
{
	$('#canvas').css('display','block');
	$('#info').css('display','block');
	$('#words').css('display','block');
	$('#clear').css('display','block');
	$('#chat').css('display','block');
	$('#players').css('display','block');
	$('#selection').css('display','block');
	$('#timer').css('display','block');
	$('#loading').css('display','none');
	websocket.runCommand("getGameMeta");
	this.timer();
};

Wordsearch.prototype.timer = function()
{
	var self = this;
	if(this.timerEvent != undefined)window.clearTimeout(this.timerEvent)
	this.timerEvent = setTimeout(function() {self.timer()}, 1000);
	var date = new Date(self.gameStarted*1000 - 3600*1000);
	var hours = date.getHours();
	hours = hours < 10 ? "0"+hours : hours;
	var minutes = date.getMinutes();
	minutes = minutes < 10 ? "0"+minutes : minutes;
	var seconds = date.getSeconds();
	seconds = seconds < 10 ? "0"+seconds : seconds;
	document.getElementById("timer").innerHTML = "Noch "+hours+":"+minutes+":"+seconds;
	self.gameStarted--;
};

Wordsearch.prototype.updateInfo = function()
{
	document.getElementById("info").innerHTML = "Insgesamt: " + this.overAllWords + "<br>" +
			"Gefunden: " + (this.overAllWords - this.allWords.length) + "<br>" +
			"Ausstehend: " + (this.allWords.length);
};
/*** Sets the list of all words to find. Parameter contains Array of all words ***/
Wordsearch.prototype.setWords = function(param, count)
{
	$('#status').html("Lade Wörterbuch");
	var self = this;
	setTimeout(function (){
		self.overAllWords = count;
		self.allWords = param;
		self.refreshWordDiv();
		self.updateInfo();
	}, 500);
}

/*** Called from websocketlistener. Removes a word from the list and highlites it on the playing field ***/
Wordsearch.prototype.removeWord = function(word, x1, y1, x2, y2, color)
{
	findAndRemoveFromArray(this.allWords, word);
	this.select(x1, y1, x2, y2, color);
	this.updateInfo();
	this.refreshWordDiv();
}

Wordsearch.prototype.select = function(x1, y1, x2, y2, color)
{
	var ctx = this.ctx;
	tmpImgData = ctx.getImageData(0,0,this.canvas.width,this.canvas.height);
	this.restoreImage();
	this.drawLine(ctx, x1, y1, x2, y2, color);
	this.snapshot();
	this.restoreImage();
	this.tmpImgData = null;
	if(this.isPressed()) this.drawLine(ctx, this.mouseXstart, this.mouseYstart, this.mouseXend, this.mouseYend, this.color);
}

Wordsearch.prototype.restoreImage = function()
{
	this.ctx.putImageData(this.origImgData,0,0);
};

/*** draws a line on the playing field ***/
Wordsearch.prototype.drawLine = function(ctx, x1, y1, x2, y2, color)
{
	var xdir = getDir(x1, x2);
	var ydir = getDir(y1, y2);
	var mX = x1;
	var mY = y1;
	while(mX != x2 || mY != y2)
	{
		var aX = mX - (mX % this.charSize);
		var aY = mY - (mY % this.charSize);
		var x = mX * this.charSize + this.charSize/2 + randomFromChar(this.array[aX][aY], 3, mX+mY/2)-2;
		var y = mY * this.charSize + this.charSize/2 + randomFromChar(this.array[aX][aY], 3, mX/2+mY)-2;
		ctx.beginPath();
		ctx.arc(x,y, this.charSize/2, 0, 2 * Math.PI, false);
		ctx.fillStyle = color;
		ctx.fill();
		mX += xdir/8;
		mY += ydir/8;
}
}

/*** called when mouse is released. ***/
Wordsearch.prototype.mouseUp = function()
{
	this.pressed = false;
	this.mouseXstart = -1;
	this.mouseYstart = -1;
	document.getElementById("selection").innerHTML="";
	this.restoreImage();
};

/*** Called when mouse was moved ***/
Wordsearch.prototype.mouseMove = function(mouseX, mouseY)
{
	if(this.pressed && mouseX >= 0 && mouseY >= 0 && mouseX < this.width && mouseY < this.height)
	{
		var difX = Math.abs(this.mouseXstart - mouseX);
		var difY = Math.abs(this.mouseYstart - mouseY);
		if(difX != 0 && difY != 0 && difX != difY)
		{
			return;
		}
		this.mouseXend = mouseX;
		this.mouseYend = mouseY;
		this.redraw();
		var sel = this.getSelection();
		document.getElementById("selection").innerHTML = sel;
		if(this.allWords.indexOf(sel) != -1)
		{
			this.restoreImage();
			getWebsocket().runCommand("remove",[sel, this.mouseXstart, this.mouseYstart, this.mouseXend, this.mouseYend]);
			this.mouseUp();
			//infoPopup(this.mouseXstart*this.charSize, this.mouseYstart*this.charSize, sel)
		}
	}
}

/*** refreshs the div containing all words to find ***/
Wordsearch.prototype.refreshWordDiv = function()
{
	var string = "";
	for(var i = 0; i < this.allWords.length; i++)
		string += this.allWords[i]+ " <br>";
	document.getElementById("words").innerHTML = string;
};

/*** returns the currently by the player selected word ***/
Wordsearch.prototype.getSelection = function()
{
	var string = "";
	var xdir = getDir(this.mouseXstart, this.mouseXend);
	var ydir = getDir(this.mouseYstart, this.mouseYend);
	var mX = this.mouseXstart;
	var mY = this.mouseYstart;
	while(mX != this.mouseXend || mY != this.mouseYend)
	{
		string += this.array[mX][mY];
		mX += xdir;
		mY += ydir;
	}
	string += this.array[this.mouseXend][this.mouseYend];
	return string;
};

/*** called when mouse is pressed ***/
Wordsearch.prototype.mouseDown = function(mouseX, mouseY)
{
	this.pressed = true;
	this.mouseXstart = mouseX;
	this.mouseYstart = mouseY;
};

/*** draws the array to the canvas ***/
Wordsearch.prototype.drawArray = function()
{
	$('#status').html("Zeichne Spielfeld");
	var self = this;
	setTimeout(function () {
		self.canvas.width = self.width * self.charSize;
		self.canvas.height = self.height * self.charSize;
		var ctx = self.ctx;
		/*** draw field ***/
		ctx.font= (self.charSize)+"px FontinSans";
		for(var i = 0; i < self.width; i ++)
			for(var j = 0; j < self.height; j ++)
			{
				ctx.fillStyle = 'rgba(0,0,0,1.0)';
				ctx.fillText(self.array[i][j],2 + i * self.charSize, 14 + j * self.charSize);
			}
		self.snapshot();
		self.loaded();
	}, 500);
};

/*** initializes the array ***/
Wordsearch.prototype.createArray = function(param)
{
	$('#status').html("Erzeuge Spielfeld");
	var self = this;
	setTimeout(function() {
		self.gameStarted = parseInt(param[3]);
		var width = parseInt(param[0]);
		var height = parseInt(param[1]);
		var string = param[2];
		self.array = new Array();
		self.width = width;
		self.height = height;
		var k = 0;
		for(var i = 0; i < width; i++)
		{
			self.array[i] = new Array();
			for(var j = 0; j < height; j++)
			{
				self.array[i][j] = string.charAt(k);
				k++;
			}
		}
		self.drawArray();
		self.refreshWordDiv();
	}, 500);
};

/*** saves the current canvas to a temporary variable used later ***/
Wordsearch.prototype.snapshot = function()
{
	this.origImgData = this.ctx.getImageData(0,0,this.canvas.width,this.canvas.height);
};

/*** generates a pseudo-random number from the array ***/
function randomFromChar(c, mod, seed)
{
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";
	var i = chars.indexOf(c);
	i += seed * 20;
	return i % mod;
};

/*** redraws the array with the current selection ***/
Wordsearch.prototype.redraw = function()
{
	var ctx = this.ctx;
	//this.restoreImage();
	if(this.tmpImgData != undefined && this.tmpImgData != undefined)
	{
		this.ctx.putImageData(this.tmpImgData,this.oldDrawX1,this.oldDrawY1);
	}
	else
	{
		this.restoreImage();
	}
	/*** draw selection ***/
	if(this.isPressed())
	{
		var txs = this.mouseXstart < this.mouseXend ? this.mouseXstart : this.mouseXend;
		var tys = this.mouseYstart < this.mouseYend ? this.mouseYstart : this.mouseYend;
		var txe = this.mouseXstart > this.mouseXend ? this.mouseXstart : this.mouseXend;
		var tye = this.mouseYstart > this.mouseYend ? this.mouseYstart : this.mouseYend;
		if(txs > 0) txs--;
		if(tys > 0) tys--;
		txs *= this.charSize;
		tys *= this.charSize;
		if(txe < this.width) txe++;
		if(tye < this.height) tye++;
		txe *= this.charSize;
		tye *= this.charSize;
		this.oldDrawX1 = txs;
		this.oldDrawY1 = tys;
		this.tmpImgData = this.ctx.getImageData(this.oldDrawX1,this.oldDrawY1,txe-txs,tye-tys);
		this.drawLine(ctx, this.mouseXstart, this.mouseYstart, this.mouseXend, this.mouseYend, this.color);
	}
};

Wordsearch.prototype.isPressed = function()
{
	return this.mouseXstart != -1 && this.mouseYstart != -1 && this.mouseXend != -1 && this.mouseYend != -1;
};

function findAndRemoveFromArray(array, element)
{
	var iOf = array.indexOf(element);
	if(iOf == -1) return false;
	else
	{
		array.splice(iOf, 1);
		return true;
	}
}

function randomInt(start, end)
{
	return Math.round(Math.random() * (end - start)) + start;
};

function getDir(x1, x2)
{
	return  x1 < x2 ? 1 : x1 == x2 ? 0 : -1;
};
