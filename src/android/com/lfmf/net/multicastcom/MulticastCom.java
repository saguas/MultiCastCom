package com.lfmf.net.multicastcom;

    //import sun.net.*;
    import java.net.*;
    import java.io.*;
    import java.util.Enumeration;
    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;
    //import java.io.BufferedReader;
    //import java.io.IOException;
    //import java.io.InputStreamReader;
    //import java.net.ServerSocket;
    //import java.net.Socket;

    import org.apache.cordova.CordovaPlugin;
    import org.apache.cordova.CallbackContext;
    import org.apache.cordova.CordovaWebView;
    import org.apache.cordova.PluginResult;
    import android.content.Context;
    import android.os.Bundle;
    import android.content.Intent;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import android.os.Handler;
    import android.util.Log;
    import android.widget.Toast;
    import java.util.Iterator;

    /**
     * This class echoes a string called from JavaScript.
     */
    public class MulticastCom extends CordovaPlugin {

        private static ServerSocket serverSocket;
        private static MulticastSocket s;
        private static DatagramPacket pack;
        private static Socket clientSocket;
        private static InputStreamReader inputStreamReader;
        private static BufferedReader bufferedReader;
        private Socket client;
        private PrintWriter printwriter;
        private static String message;
        private Handler handler = new Handler();
        private Thread t;
        private static CordovaWebView gwebView;
        private static String gECB;
        private static Bundle gCachedExtras = null;
        private static final String TAG = "MessageRecived";
        private static boolean gForeground = true;
        private static CallbackContext callbackContext;
        
        
        @Override
        public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
            if (action.equals("multicast")) {
                gwebView = this.webView;
                this.callbackContext = callbackContext;
                final String message = args.getString(0);
                gECB = "app.testeNotifications";
                //cordova.getThreadPool().execute(new Runnable() {        
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run(){
                        
                        echo(message, callbackContext);
                    }
                 });
                return true;
            }
            return false;
        }
        
        /**
         * Called when an activity you launched exits, giving you the requestCode you started it with,
         * the resultCode it returned, and any additional data from it.
         *
         * @param requestCode		The request code originally supplied to startActivityForResult(),
         * 							allowing you to identify who this result came from.
         * @param resultCode		The integer result code returned by the child activity through its setResult().
         * @param intent				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
         */
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
             Log.d(TAG, "echo - onActivityResult : ");
        }
        
        public void onReset() {
             Log.d(TAG, "echo - onReset : ");
        }
        
        public Object onMessage(String id, Object data) {
            Log.d(TAG, "echo - onMessage : ");
            return null;
        }

        protected void echo(final String message, final CallbackContext callbackContext) {
            if (message != null && message.length() > 0) {
                //this.connect(message,callbackContext);
                //this.serverNormal(callbackContext);
                //this.server(callbackContext);
                Context context = getApplicationContext();
                Log.d(TAG, "echo - context: " + context);
                Intent service = new Intent(context, MulticastComService.class);
                context.startService(service);
                //this.multicastServer(callbackContext);
                //callbackContext.success(message);
            } else {
                callbackContext.error("Expected one non-empty string argument.");
            }
        }
        
        private void connect(String message,CallbackContext callbackContext){
            
            try {
                 client = new Socket("192.168.1.4", 4444);  //connect to server
                 printwriter = new PrintWriter(client.getOutputStream(),true);
                 printwriter.write(message);  //write the message to output stream
             
                 printwriter.flush();
                 printwriter.close();
                 client.close();   //closing the connection
                
                 callbackContext.success(message);
                
            }catch (UnknownHostException e) {
                e.printStackTrace();
                callbackContext.error("UnknownHostException. " + e);
            } catch (IOException e) {
                e.printStackTrace();
                callbackContext.error("IOException. " + e);
            }
        }
        
        private void server(final CallbackContext callbackContext){
            
                try {
                     serverSocket = new ServerSocket(4444);  //Server socket
     
                } catch (IOException e) {
                    System.out.println("Could not listen on port: 4444");
                    callbackContext.error("IOException. " + e);
                }
            
                Log.i("ServerActivity", "Server started. Listening to the port 4444");
                System.out.println("Server started. Listening to the port 4444");
                
         
                //while (true) {
                    try {
         
                        clientSocket = serverSocket.accept();   //accept the client connection
                        
                        //handler.postDelayed(new Runnable(){
                          //  @Override
                            //public void run(){
                                try{
                                    inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                                    bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                                    message = bufferedReader.readLine();
                                    Log.i("MessageRecived", "a menssagem recebida é: " + message);
                                    System.out.println(message);
                                    callbackContext.success(message);
                                    inputStreamReader.close();
                                    clientSocket.close();
                                }catch (IOException ex) {
                                    System.out.println("Problem in message reading");
                                    callbackContext.error("IOException. " + ex);
                                }
                            //}
                        //}, 1000);
         
                    } catch (IOException ex) {
                        System.out.println("Problem in message reading");
                        callbackContext.error("IOException. " + ex);
                    }
                //}
        }
        
        private void serverNormal(final CallbackContext callbackContext){
            
                try {
                     serverSocket = new ServerSocket(4444);  //Server socket
     
                } catch (IOException e) {
                    System.out.println("Could not listen on port: 4444");
                    callbackContext.error("IOException. " + e);
                }
         
                System.out.println("Server started. Listening to the port 4444");
                Log.i("ServerActivity", "Server started. Listening to the port 4444");
         
                while (true) {
            
                    try {
         
                        clientSocket = serverSocket.accept();   //accept the client connection
                        
                        t = new Thread(new Runnable(){
                        //cordova.getThreadPool().execute(new Runnable() {
                            
                            public void run(){
                                try{
                                    inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                                    bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                                    message = bufferedReader.readLine();
                                    
                                    Log.i("MessageRecived", "a menssagem recebida é: " + message);
                                    System.out.println(message);
                                    inputStreamReader.close();
                                    clientSocket.close();
                                    callbackContext.success(message);
                                }catch (IOException ex) {
                                    System.out.println("Problem in message reading");
                                    callbackContext.error("IOException. " + ex);
                                }
                            }
                        });
                        
                        t.start();
         
                    } catch (IOException ex) {
                        System.out.println("Problem in message reading");
                        callbackContext.error("IOException. " + ex);
                    }
                }
        }
        
        private void multicastServer(final CallbackContext callbackContext){
            
                //MulticastSocket s = null;
                //DatagramPacket pack = null;
                String group = "225.4.5.6";
                final byte[] buf = new byte[1024];
                int port = 5000;
            
            try{
                    // Which port should we listen to
                    
                    //String message;
                    // Which address
                    
                    // Create the socket and bind it to port 'port'.
                    Log.i("ServerActivity", "Server started. Listening to the port 5000");
                    s = new MulticastSocket(port);
                    s.joinGroup(InetAddress.getByName(group));
                    
                    // join the multicast group
                   
                    // Now the socket is set up and we are ready to receive packets
                    // Create a DatagramPacket and do a receive
                    
                    Log.i("MessageRecived", "antes do ciclo infinito while");  
                
            while(true){
                
                    Log.i("MessageRecived", "antes de esperar por datagramas myip: " + getLocalIpAddress() );  
                    pack = new DatagramPacket(buf, buf.length);
                    s.receive(pack);
                    final String message = new String(pack.getData(), 0, pack.getLength(),"UTF-8");
                
                t = new Thread(new Runnable(){
                        //cordova.getThreadPool().execute(new Runnable() {
                            
                    public void run(){
                        
                            
                            Log.i("MessageRecived", "depois de esperar por datagramas");
                            // Finally, let us do something useful with the data we just received,
                            // like print it on stdout :-)
                            /*System.out.println("Received data from: " + pack.getAddress().toString() +
                                        ":" + pack.getPort() + " with length: " +
                                        pack.getLength());
                                        */
                            //String message = new String(pack.getData(), "UTF-8");
                            //String message = new String(pack.getData(), 0, pack.getLength(),"UTF-8");
                            //System.out.write(pack.getData(),0,pack.getLength());
                            //System.out.println(message);
                            Log.i("MessageRecived", "a menssagem recebida é: " + message);
                            // And when we have finished receiving data leave the multicast group and
                            // close the socket
                            //s.leaveGroup(InetAddress.getByName(group));
                            //s.close();
                                                                     //this.gwebView.loadUrl("javascript:try{cordova.fireDocumentEvent('serverReturn');}catch(e){console.log('exception firing serverReturn event from native');};");
                            //callbackContext.success(message + " MyIP: " + this.getLocalIpAddress());
                        String _d = new String("javascript:app.testeNotifications({msg:'" +message+"'})");
                        //gECB = "app.testeNotifications".toString();
                        //String _d = "javascript:" + gECB + "(" + message.toString() + ")";
                        Log.v("MessageRecived", "chamar " + _d);
                        //Toast.makeText(/*cordova.getActivity()*/getApplicationContext(), "Wifi Connected", Toast.LENGTH_SHORT).show();
                        gwebView.sendJavascript(_d); 
                    
                    }});
                
                t.start();
            }
        }catch(UnknownHostException ue){
            callbackContext.error("UnknownHostException. " + ue);
        }catch(java.net.BindException b){
            callbackContext.error("java.net.BindException. " + b);
        }catch(IOException e){
            callbackContext.error("IOException. " + e);
        }
            
        }
        
    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); 
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e("ServerActivity", ex.toString());
            }
            return null;
        
    }
        
    /*
	 * Sends a json object to the client as parameter to a method which is defined in gECB.
	 */
	public static void sendJavascript(JSONObject _json) {
		String _d = "javascript:" + gECB + "(" + _json.toString() + ")";
		

		if (gECB != null && gwebView != null) {
            Log.v(TAG, "sendJavascript: " + _d);
			gwebView.sendJavascript(_d); 
		}
	}
        
    public static void sendBackMsg(JSONObject _json){
        
        //String _d = "javascript:" + gECB + "(" + _json.toString() + ")";
        PluginResult progressResult = new PluginResult(PluginResult.Status.OK, _json.toString());
        progressResult.setKeepCallback(true);
        callbackContext.sendPluginResult(progressResult);
    }
        
    /*
	 * Sends the pushbundle extras to the client application.
	 * If the client application isn't currently active, it is cached for later processing.
	 */
	public static void sendExtras(Bundle extras)
	{
		if (extras != null) {
			extras.putBoolean("foreground", gForeground);
			if (gECB != null && gwebView != null) {
				//sendJavascript(convertBundleToJson(extras));
                sendBackMsg(convertBundleToJson(extras));
			} else {
				Log.v(TAG, "sendExtras: caching extras to send at a later time.");
				gCachedExtras = extras;
			}
		}
	}
        
    /*
     * serializes a bundle to JSON.
     */
    private static JSONObject convertBundleToJson(Bundle extras)
    {
		try
		{
			JSONObject json;
			json = new JSONObject().put("event", "message");
        
			JSONObject jsondata = new JSONObject();
			Iterator<String> it = extras.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next();
				Object value = extras.get(key);	
        	
				// System data from Android
				if (key.equals("from") || key.equals("collapse_key"))
				{
					json.put(key, value);
				}
				else if (key.equals("foreground"))
				{
					json.put(key, extras.getBoolean("foreground"));
				}
				else if (key.equals("coldstart"))
				{
					json.put(key, extras.getBoolean("coldstart"));
				}
				else
				{
					// Maintain backwards compatibility
					if (key.equals("message") || key.equals("msgcnt") || key.equals("soundname"))
					{
						json.put(key, value);
					}
        		
					if ( value instanceof String ) {
					// Try to figure out if the value is another JSON object
						
						String strValue = (String)value;
						if (strValue.startsWith("{")) {
							try {
								JSONObject json2 = new JSONObject(strValue);
								jsondata.put(key, json2);
							}
							catch (Exception e) {
								jsondata.put(key, value);
							}
							// Try to figure out if the value is another JSON array
						}
						else if (strValue.startsWith("["))
						{
							try
							{
								JSONArray json2 = new JSONArray(strValue);
								jsondata.put(key, json2);
							}
							catch (Exception e)
							{
								jsondata.put(key, value);
							}
						}
						else
						{
							jsondata.put(key, value);
						}
					}
				}
			} // while
			json.put("payload", jsondata);
        
			Log.v(TAG, "extrasToJSON: " + json.toString());

			return json;
		}
		catch( JSONException e)
		{
			Log.e(TAG, "extrasToJSON: JSON exception");
		}        	
		return null;      	
    }
        
    public static boolean isActive()
    {
    	return gwebView != null;
    }
        
    public static boolean isInForeground()
    {
      return gForeground;
    }
        
    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause ");
        super.onPause(multitasking);
        gForeground = false;
    }
    
    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume ");
        super.onResume(multitasking);
        gForeground = true;
    }
        
     /**
     * Called when the activity receives a new intent.
     */
        public void onNewIntent(Intent intent) {
            Log.d(TAG, "onNewIntent ");
        }
           
    /**
	 * Gets the application context from cordova's main activity.
	 * @return the application context
	 */
	public Context getApplicationContext() {
		return this.cordova.getActivity().getApplicationContext();
	}
    
    @Override    
    public void onDestroy() 
	{
		gwebView = null;
		Log.i("MessageRecived", "onDestroyd ");
		super.onDestroy();
	}
        
}
