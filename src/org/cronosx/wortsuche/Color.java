package org.cronosx.wortsuche;

import org.json.*;

public class Color
{
	private short r, g, b;
	public Color(short r, short g, short b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public short getR()
	{
		return r;
	}
	
	public short getG()
	{
		return g;
	}
	
	public short getB()
	{
		return b;
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		obj.put("r", r);
		obj.put("g", g);
		obj.put("b", b);
		return obj;
	}
}
