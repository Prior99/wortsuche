var WIDTH = 67;
var HEIGHT = 29;
var CHAR_SIZE = 20;
var MAX_TRYS = 50000;

function Wordsearch(canvas)
{
	this.allWords = new Array();
	this.allSolutions = new Array();
	this.nextColor = generateColor(0.18);
	this.canvas = canvas;
	this.width = WIDTH;
	this.height = HEIGHT;
	this.charSize = CHAR_SIZE;
	canvas.width = WIDTH * CHAR_SIZE;
	canvas.height = HEIGHT * CHAR_SIZE;
	this.ctx = canvas.getContext("2d");
	this.refreshWordDiv();
	var self = this;
	this.mouseXend = -1;
	this.mouseYend = -1;
	this.pressed = false;
	this.canvas.addEventListener("mousemove", function(event) { self.mouseMove(Math.floor((event.clientX + CHAR_SIZE / 2) / CHAR_SIZE) - 1, 
		Math.floor((event.clientY + CHAR_SIZE / 2) / CHAR_SIZE) - 1); }, false);
	this.canvas.addEventListener("mousedown", function(event) { self.mouseDown(Math.floor((event.clientX + CHAR_SIZE / 2) / CHAR_SIZE) - 1, 
		Math.floor((event.clientY + CHAR_SIZE / 2) / CHAR_SIZE) - 1); }, false);
	this.canvas.addEventListener("mouseup", function(event) { self.mouseUp(); }, false);
};

Wordsearch.prototype.initialize = function()
{
	this.initDictonary();
	this.initArray();
	this.drawArray();
	this.solveAll();
};

Wordsearch.prototype.solveAll = function()
{
	var ctx = this.ctx;
	for(var i = 0; i < this.allSolutions.length; i++)
	{
		var color = generateColor(0.18);
		var p = this.allSolutions[i];
		var xdir = getDir(p.x1, p.x2);
		var ydir = getDir(p.y1, p.y2);
		var mX = p.x1;
		var mY = p.y1;
		while(mX != p.x2 || mY != p.y2)
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
	this.snapshot();
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

function generateColor(alpha)
{
	return "rgba("+randomInt(10,256)+","+randomInt(10,256)+","+randomInt(10,256)+","+alpha+")";
};

Wordsearch.prototype.mouseUp = function()
{
	this.pressed = false;
	this.mouseXend = -1;
	this.mouseYend = -1;
	this.nextColor = generateColor(0.18);
	//this.redraw();
};

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
		if(findAndRemoveFromArray(this.allWords, sel))
		{
			this.snapshot();
			this.mouseUp();
			this.refreshWordDiv();
		}
	}
}

Wordsearch.prototype.refreshWordDiv = function()
{
	var string = "";
	for(var i = 0; i < this.allWords.length; i++)
		string += this.allWords[i]+ "  ";
	document.getElementById("words").innerHTML = string;
};

function getDir(x1, x2)
{
	return  x1 < x2 ? 1 : x1 == x2 ? 0 : -1;
};

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

Wordsearch.prototype.mouseDown = function(mouseX, mouseY)
{
	this.pressed = true;
	this.mouseXstart = mouseX;
	this.mouseYstart = mouseY;
	this.redraw();
};

Wordsearch.prototype.initArray = function()
{
	var allWords = 0;
	var wordsFailed = 0;
	this.array = new Array();
	/*** Array vorbelegen ***/
	for(var i = 0; i < this.width; i++)
	{
		this.array[i] = new Array();
		for(var j = 0; j < this.height; j++)
			this.array[i][j] = ' ';
	}
	var diag = Math.sqrt(this.width*this.width + this.height*this.height);
	var stlen = Math.round(this.maxWordLen > diag / 4 ? diag / 4 : this.maxWordLen);
	for(var i = stlen; i >= 2; i--)
	{
		/*** Absteigend der gr�sse nach w�rter ausw�hlen ***/
		var bestPos = null;
		var words = this.dict[i]; //Alle W�rter dieser L�nge
		for(var j = 0; j < words.length; j++) //Alle W�rter durchlaufen
		{	
			bestPos = null;
			allWords ++;
			var word = words[j]; //Aktuelles Wort
			var sx = randomInt(0, this.width -1);
			var sy = randomInt(0, this.height -1);
			outer:
			for(var x = 0; x < this.width; x++)
			{
				for(var y = 0; y < this.height; y++)
				{
					sx += x;
					sy += y;
					sx = sx >= this.width ? sx - this.width : sx;
					sy = sy >= this.width ? sy - this.width : sy;
					if(Math.random() >= 0.5) { sdx = -1; edx = 1; cdx = 1} else { sdx = 1; edx = -1; cdx = -1}
					if(Math.random() >= 0.5) { sdy = -1; edy = 1; cdy = 1} else { sdy = 1; edy = -1; cdy = -1}
					for(var xdir= sdx; xdir != edx; xdir+=cdx)
						for(var ydir= sdy; ydir != edy; ydir+=cdy)
							if(xdir!= 0 || ydir!=0)
							{
								var cors = this.match(word, sx, sy, xdir, ydir);
								if(cors != -1)
								{
									/*** Wort hat hierhin gepasst ***/
									if(bestPos == null || bestPos.cor < cors) 
									{
										bestPos = new Possibility(sx, sy, xdir, ydir, cors);
									}
								}
							}
				}
			}
			if(bestPos == null) wordsFailed++;
			else
			{
				if(bestPos.cors != 0 || Math.random() >= 0.7)
				{
					this.apply(word, bestPos.x, bestPos.y, bestPos.xdir, bestPos.ydir);
					this.allWords.push(word);
					this.allSolutions.push(new Solution(word, bestPos.x, bestPos.y, bestPos.x + bestPos.xdir * (word.length-1), bestPos.y + bestPos.ydir * (word.length-1)));
				}
				else  wordsFailed++;
			}
		}
	}
	console.log(wordsFailed+"/"+allWords);
	/*** Array füllen ***/
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";
	for(var i = 0; i < this.width; i++)
	{
		for(var j = 0; j < this.height; j++)
			if(this.array[i][j] == ' ')
			{
				this.array[i][j] = chars.charAt(randomInt(0,chars.length-1));
			}
	}
};

function randomInt(start, end)
{
	return Math.round(Math.random() * (end - start)) + start;
};

Wordsearch.prototype.match = function(word, x, y, xdir, ydir)
{
	var cor = 0;
	for(var i = 0; i < word.length; i++)
	{
		var cx = x + xdir * i;
		var cy = y + ydir * i;
		if(cx < 0 || cx >= this.width || cy < 0 || cy >= this.height) return -1;
		if(this.array[cx][cy] != ' ' && this.array[cx][cy] != word.charAt(i)) return -1;
		if(this.array[cx][cy] == word.charAt(i)) cor++;
	}
	return cor;
};

Wordsearch.prototype.apply = function(word, x, y, xdir, ydir)
{
	for(var i = 0; i < word.length; i++)
	{
		this.array[x + xdir * i][y + ydir * i] = word.charAt(i);
	}
};

Wordsearch.prototype.drawArray = function()
{
	var ctx = this.ctx;
	/*** draw field ***/
	ctx.font= (this.charSize-2)+"px Arial";
	for(var i = 0; i < this.width; i ++)
		for(var j = 0; j < this.height; j ++)
		{
			ctx.fillStyle = 'rgba(0,0,0,1.0)';
			ctx.fillText(this.array[i][j],2 + i * this.charSize, 14 + j * this.charSize);
		}
	this.origImgData = ctx.getImageData(0,0,this.canvas.width,this.canvas.height);
	this.snapshot();
};

Wordsearch.prototype.snapshot = function()
{
	this.origImgData = this.ctx.getImageData(0,0,this.canvas.width,this.canvas.height);
};

function randomFromChar(c, mod, seed)
{
	var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";
	var i = chars.indexOf(c);
	i += seed * 20;
	return i % mod;
};

Wordsearch.prototype.redraw = function()
{
	var ctx = this.ctx;
	ctx.putImageData(this.origImgData,0,0);
	/*** draw selection ***/
	if(this.mouseXstart != -1 && this.mouseYstart != -1 && this.mouseXend != -1 && this.mouseYend != -1)
	{
		var xdir = getDir(this.mouseXstart, this.mouseXend);
		var ydir = getDir(this.mouseYstart, this.mouseYend);
		var mX = this.mouseXstart;
		var mY = this.mouseYstart;
		while(mX != this.mouseXend || mY != this.mouseYend)
		{
			var aX = mX - (mX % this.charSize);
			var aY = mY - (mY % this.charSize);
			var x = mX * this.charSize + this.charSize/2 + randomFromChar(this.array[aX][aY], 3, mX+mY/2)-2;
			var y = mY * this.charSize + this.charSize/2 + randomFromChar(this.array[aX][aY], 3, mX/2+mY)-2;
			ctx.beginPath();
			ctx.arc(x,y, this.charSize/2, 0, 2 * Math.PI, false);
			ctx.fillStyle = this.nextColor;
			ctx.fill();
			mX += xdir/8;
			mY += ydir/8;
		}
	}
};

function Possibility(x, y, xdir, ydir, cor, len)
{
	this.len=len;
	this.x=x;
	this.y=y;
	this.xdir=xdir;
	this.ydir=ydir;
	this.cor=cor;
};

function Solution(word, x1, y1, x2, y2)
{
	this.x1 = x1;
	this.x2 = x2;
	this.y1 = y1;
	this.y2 = y2;
	this.word = word;
};


function shuffle(myArray) 
{
	var i = myArray.length, j, tempi, tempj;
	if(i == 0) return false;
	while(--i) 
	{
		j = Math.floor(Math.random() * (i + 1));
		tempi = myArray[i];
		tempj = myArray[j];
		myArray[i] = tempj;
		myArray[j] = tempi;
	}
}


Wordsearch.prototype.initDictonary = function()
{
	this.dict = new Array();
	this.maxWordLen = 0;
	for(var i = 0; i < WORDS.length; i++)
	{
		var word = WORDS[i];
		var len = word.length;
		if(len > this.maxWordLen) this.maxWordLen = len;
		if(this.dict[len] == undefined)
			this.dict[len] = new Array();
		this.dict[len].push(word);
	}
	for(var i = 2; i <= this.maxWordLen; i++)
	{
		shuffle(this.dict[i]);
	}
	//this.dict = unsorted;
	//shuffle(this.dict);
};