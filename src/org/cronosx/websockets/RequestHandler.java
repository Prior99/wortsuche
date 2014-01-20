package org.cronosx.websockets;

import org.json.*;

public interface RequestHandler
{
	public JSONObject invoke(JSONObject jObj);
}
