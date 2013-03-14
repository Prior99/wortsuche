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
			Math.floor(((event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - canvas.offsetLeft) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5), 
			Math.floor(((event.clientY + document.body.scrollTop + document.documentElement.scrollTop - canvas.offsetTop) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5)); }, false);
	this.canvas.addEventListener("mousedown", function(event) { self.mouseDown(
			Math.floor(((event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - canvas.offsetLeft) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5), 
			Math.floor(((event.clientY + document.body.scrollTop + document.documentElement.scrollTop - canvas.offsetTop) + CHAR_SIZE / 2) / CHAR_SIZE - 0.5)); }, false);
	this.canvas.addEventListener("mouseup", function(event) { self.mouseUp(); }, false);
};
/*** Called from websocket when it is open. Registers all Listeners to the websockets and asks it for initialization ***/
Wordsearch.prototype.initialize = function()
{
	self = this;
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
	this.overAllWords = count;
	this.allWords = param;
	this.refreshWordDiv();
	this.updateInfo();
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
	this.mouseXend = -1;
	this.mouseYend = -1;
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
			if(difY > difX)
				mouseX = this.mouseXstart + difY;
			else
				mouseY = this.mouseYstart + difX;
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
		}
	}
}

/*** refreshs the div containing all words to find ***/
Wordsearch.prototype.refreshWordDiv = function()
{
	var string = "";
	for(var i = 0; i < this.allWords.length; i++)
		string += this.allWords[i]+ "  ";
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
	this.redraw();
};

/*** draws the array to the canvas ***/
Wordsearch.prototype.drawArray = function()
{
	this.canvas.width = this.width * this.charSize;
	this.canvas.height = this.height * this.charSize;
	var ctx = this.ctx;
	/*** draw field ***/
	ctx.font= (this.charSize)+"px FontinSans";
	for(var i = 0; i < this.width; i ++)
		for(var j = 0; j < this.height; j ++)
		{
			ctx.fillStyle = 'rgba(0,0,0,1.0)';
			ctx.fillText(this.array[i][j],2 + i * this.charSize, 14 + j * this.charSize);
		}
	this.snapshot();
};

/*** initializes the array ***/
Wordsearch.prototype.createArray = function(param)
{
	var width = parseInt(param[0]);
	var height = parseInt(param[1]);
	var string = param[2];
	this.array = new Array();
	this.width = width;
	this.height = height;
	var k = 0;
	for(var i = 0; i < width; i++)
	{
		this.array[i] = new Array();
		for(var j = 0; j < height; j++)
		{
			this.array[i][j] = string.charAt(k);
			k++;
		}
	}
	this.drawArray();
	this.refreshWordDiv();
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
	this.restoreImage();
	/*** draw selection ***/
	if(this.isPressed())
	{
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
