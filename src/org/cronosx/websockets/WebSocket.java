package org.cronosx.websockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.BASE64Encoder;

/**
 * @author prior
 * This is a wrapped socket that parses the pure byte-input with the Websocketprotocol and return the decoded code
 * and the other way around encrypts messages and passes them to the client
 */
@SuppressWarnings("restriction")
public class WebSocket extends Thread
{
	private final Socket socket;
	private final BufferedInputStream inputStream;
	private final BufferedOutputStream outputStream;
	private boolean isInitialized;
	private WebSocketListener listener;
	private Pattern webSocketPatternOrigin;
	private Pattern webSocketPatternHost;
	private Pattern webSocketPatternKey;
	private BASE64Encoder base64Enc;
	private MessageDigest sha1;
	/**
	 * Creates a new websocket, sets up the streams and starts listening
	 * <p>
	 * @param socket socket to be used for communication
	 * @throws IOException when unabled to open streams
	 * @throws NoSuchAlgorithmException 
	 */
	public WebSocket(final Socket socket) throws IOException, NoSuchAlgorithmException
	{
		sha1 = MessageDigest.getInstance("SHA-1");
		base64Enc = new BASE64Encoder();
		webSocketPatternHost = Pattern.compile("Host: (.*)");
		webSocketPatternOrigin = Pattern.compile("Origin: (.*)");
		webSocketPatternKey = Pattern.compile("Sec-WebSocket-Key: (.*)");
		isInitialized = false;
		this.socket = socket;
		this.inputStream = new BufferedInputStream(socket.getInputStream(), 1024 * 256);
		this.outputStream = new BufferedOutputStream(socket.getOutputStream());
		this.start();
	}
	
	/**
	 * Initializes the socket by using the header containing the upgrade request from the client
	 * Parses the header and initializes everything
	 * Must be called before usage for proper functioning
	 * <p>
	 * @param header header the client sent
	 */
	private void init(String header)
	{
		if(listener != null) listener.onHandshake(this);
		try
		{
			Matcher matcher = webSocketPatternKey.matcher(header);
			matcher.find();
			final String key = matcher.group(1);
			matcher = webSocketPatternOrigin.matcher(header);
			matcher.find();
			final String origin = matcher.group(1);
			matcher = webSocketPatternHost.matcher(header);
			matcher.find();
			final String host = matcher.group(1);
			String response = ("HTTP/1.1 101 Switching Protocols\r\n"+
				"Upgrade: websocket\r\n"+
				"Connection: Upgrade\r\n"+
				"Sec-Websocket-Origin: " + origin + "\r\n"+
				"Sec-Websocket-Host: " + host + "\r\n"+
				"Sec-WebSocket-Accept: " + generateHandShakeKey(key) + "\r\n\r\n");
			outputStream.write(response.getBytes());
			outputStream.flush();
			isInitialized = true;
			if(listener != null) listener.onHandshakeSuccessfull(this);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public void run()
	{
		boolean stop = false;
		if(listener != null) listener.onOpen(this);
		while(!this.isInterrupted() && ! socket.isClosed() &&!stop)
		{
			try
			{
				byte[] b = new byte[8192];
				int i = 0;
				while((i+=inputStream.read(b, i, 1024)) == 1024)
				{
					if(i >= b.length)
						b = Arrays.copyOf(b, b.length * 2);
				}
				if(i == -1)
				{
					socket.close();
					stop = true;
				}
				else
				{
					b = Arrays.copyOf(b, i);
					if(!isInitialized()) init(new String(b));
					else
					{
						while(b.length >= 6)
						{
							int got = b.length;
							int length = calcLength(b);
							int offset = calcOffset(b);
							int missing = length - (got - offset);
							if(missing > 0)
							{
								b = Arrays.copyOf(b, length + offset);
								while(got < b.length)
								{
									int k;
									while((k = inputStream.read(b, got, b.length-got)) == -1) 
									{
										try {Thread.sleep(1L); }catch(Exception e){e.printStackTrace();}
									}
									got+=k;
								}
							}
							if(listener != null) listener.onMessage(decode(b), this);
							b = Arrays.copyOfRange(b, length + offset, b.length);
						}
					}
					try
					{
						Thread.sleep(5L);
					}
					catch(InterruptedException e)
					{
						
					}
				}
			}
			catch (IOException e)
			{
				stop = true;
			}
		}
		if(listener != null) listener.onClose(this);
	}
	
	/**
	 * Changes the websocketlistener. The websocketlisteners methods will be invoked at certain events
	 * <p>
	 * @param listener websocketlistener
	 */
	public void setWebSocketListener(WebSocketListener listener)
	{
		this.listener = listener;
	}
	
	/**
	 * Calculates the offset (Overhead of the framing before each message)
	 * <p>
	 * @param bytes raw input
	 * @return whole offset in bytes
	 */
	private int calcOffset(byte[] bytes)
	{
		if(bytes.length > 1)
		{
			int length = (bytes[1] & 127);
			int offset = 2 + (length == 126 ? 2 : length == 127 ? 8 : 0);
			return offset + 4;
		}
		return 0;
	}
	
	/**
	 * Calculates the messagelength specified by the framing
	 * <p>
	 * @param bytes raw input
	 * @return length of the message in bytes
	 */
	private int calcLength(byte[] bytes)
	{
		if(bytes.length > 1)
		{
			int length = (bytes[1] & 127);
			int offset = 2 + (length == 126 ? 2 : length == 127 ? 8 : 0);
			if(length == 126)
			{
				byte[] l = Arrays.copyOfRange(bytes, 2, offset);
				length = ByteBuffer.wrap(l).getShort();
				length = ((0x000000FF & l[0]) << 8 | (0x000000FF & l[1]));
			}
			if(length == 127)
			{
				byte[] l = Arrays.copyOfRange(bytes, 2, offset);
				length = (	(0x000000FF & l[0]) << 56 | 
							(0x000000FF & l[1]) << 48 | 
							(0x000000FF & l[2]) << 40 | 
							(0x000000FF & l[3]) << 32 | 
							(0x000000FF & l[4]) << 24 | 
							(0x000000FF & l[5]) << 16 | 
							(0x000000FF & l[6]) << 8 | 
							(0x000000FF & l[7]));
			}
			return length;
		}
		else return 0;
	}
	
	/**
	 * Decodes a message and returns a decoded String
	 * <p>
	 * @param bytes raw input including framing
	 * @return decoded string
	 */
	private String decode(byte[] bytes)
	{
		if(bytes.length >= 6)
		{
			//The first bit represents the type of data that will be delivered
			//The second bit represents the length
			int length = (bytes[1] & 127);
			 //If the second Bit (length) is  126 there are 2 additional bits used for the length
			 //If the second Bit (length) is  127 there are 8 additional bits used for the length
			int offset = 2 + (length == 126 ? 2 : length == 127 ? 8 : 0);
			length = calcLength(bytes);
			offset += 4;
			//The mask for the further parsing is from (offset + 2) to (offset + 6)
			//The first byte of the real data is from (offset + 7)
			byte[] decoded = new byte[length];
			//int j = 0;
			for(int i = offset, j = 0; i < length + offset && i < bytes.length; i++, j++)
			{
				decoded[j] = (byte) (bytes[i] ^  bytes[(j % 4) + offset - 4]);
			}
			if(bytes.length > offset + length)
			{
				/*byte[] input = Arrays.copyOfRange(bytes, length+offset, bytes.length);
				if(listener != null) listener.onMessage(decode(input), this);*/
			}
			return new String(decoded);
		}
		else return null;
	}
	
	/**
	 * Converts a byte[]-array to an integer by shifting
	 * <p>
	 * @param b bytearray to convert
	 * @return converted bytearray
	 */
	public static int byteArrayToInt(byte[] b) 
	{
		int value = 0;
		for (int i = 0; i < 4; i++) 
		{
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	/**
	 * Encodes a string, generates the framing and returns a byte[]-array containing the whole framed message that can be passed to the client
	 * <p>
	 * @param string string to encode
	 * @return prepared, proper framed message
	 */
	private byte[] encode(String string)
	{
		byte[] bytes = string.getBytes();
		int index = (bytes.length > 125 ? (bytes.length > 65535 ? 10 : 4) : 2);
		byte[] encoded = new byte[index + bytes.length];
		encoded[0] = (byte) 129;
		if(bytes.length <= 125)
		{
			encoded[1] = (byte) bytes.length;
		}
		else
		{
			if(bytes.length >= 126 && bytes.length <= 65535)
			{
				encoded[1] = 126;
				encoded[2] = (byte) ((bytes.length >>  8) & 255);
				encoded[3] = (byte) ((bytes.length	  ) & 255);
			}
			else
			{
				encoded[1] = 126;
				encoded[2] = (byte) ((bytes.length >> 56) & 255);
				encoded[3] = (byte) ((bytes.length >> 48) & 255);
				encoded[4] = (byte) ((bytes.length >> 40) & 255);
				encoded[5] = (byte) ((bytes.length >> 32) & 255);
				encoded[6] = (byte) ((bytes.length >> 24) & 255);
				encoded[7] = (byte) ((bytes.length >> 16) & 255);
				encoded[8] = (byte) ((bytes.length >>  8) & 255);
				encoded[9] = (byte) ((bytes.length	  ) & 255);
			}
			
		}
		for(int i = 0, j = index; i < bytes.length; i++, j++)
		{
			encoded[j] = bytes[i]; 
		}
		return encoded;
	}
	
	/**
	 * Closes the websocket
	 * @throws IOException 
	 */
	public void close() throws IOException
	{
		socket.close();
		this.interrupt();
	}
	
	/**
	 * Sends a string to the client
	 * <p>
	 * @param s string to send
	 * @return whether the transmission was successfull
	 */
	public boolean send(String s) 
	{
		try
		{
			if(!socket.isClosed())
			{
				outputStream.write(encode(s));
				outputStream.flush();
				return true;
			}
			else
				return false;
		}
		catch(IOException e)
		{
			if(listener != null )listener.onClose(this);
			e.printStackTrace();
			try
			{
				socket.close();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			return false;
		}
	}
	
	/**
	 * Generates the corresponding handshake key for the client
	 * <p>
	 * @param key key received from the client
	 * @return generated key
	 */
	private String generateHandShakeKey(String key)
	{
		return base64Enc.encode(sha1.digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
	}

	/**
	 * If the socket is set up and everything was initialized by a header, returns true otherwise returns false
	 * If returns true, the socket be used in both directions
	 * <p>
	 * @return whether the socket is initialized
	 */
	public boolean isInitialized()
	{
		return isInitialized;
	}
}
