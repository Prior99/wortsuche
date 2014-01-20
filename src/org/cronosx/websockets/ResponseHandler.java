package org.cronosx.websockets;

import org.json.*;

public interface ResponseHandler
{
	public void invoke(JSONObject jObj);
}
