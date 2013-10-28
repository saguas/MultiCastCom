package com.lfmf.net.multicastcom;

import java.net.*;
import java.io.*;
import java.util.Enumeration;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.app.Service;
import android.os.IBinder;
import android.content.res.Configuration;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;



public class MulticastComService extends Service {
    
        private boolean started;
        private Thread t;
        private Thread t2;  
        private Intent i;
        private Context context;
        private static final String TAG = "MessageRecived";
        public  static final int NOTIFICATION_ID = 238;
    
        private byte[] buf = null;
        private int port = 5000;
        private String group = "225.4.5.6";
        private static ServerSocket serverSocket;
        private static MulticastSocket multicastSocket = null;
        private static DatagramPacket pack;
        private static Socket clientSocket;
        private static InputStreamReader inputStreamReader;
        private static BufferedReader bufferedReader;
        private Socket client;
        private PrintWriter printwriter;
        private WifiManager wm;
        private WifiManager.MulticastLock multicastLock; 
        private WifiManager.WifiLock wifilock;
        
    
        @Override
        public void onCreate() {
            // Start up the thread running the service.  Note that we create a
            // separate thread because the service normally runs in the process's
            // main thread, which we don't want to block.  We also make it
            // background priority so CPU-intensive work will not disrupt our UI.
            
            started = false;
            /*
            Allows an application to receive Wifi Multicast packets. Normally the Wifi stack filters out
            packets not explicitly addressed to this device. Acquring a MulticastLock will cause the stack  
            to receive packets addressed to multicast addresses. Processing these extra packets can cause a 
            noticable battery drain and should be disabled when not needed.
            */
            
            wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            wifilock = wm.createWifiLock(TAG + "2");
            wifilock.acquire();
            multicastLock = wm.createMulticastLock(TAG);
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            
            Log.d(TAG, "onCreate - server created: wifilock end multicastLock adquireds ");
            
            // reopen the socket
            try {
                openSocket();
            } catch (IOException e1) {
                //activity.ipc.error(new RuntimeException("socket reopen: "+e1.getMessage()));
                //return;
            }
            
            t = new MyThread();
            t.start();
            
        }
    
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
        
            Log.d(TAG, "onStartCommand - server flags : " + flags);
            if(!started){
                context = getApplicationContext();
                Log.d(TAG, "onStartCommand - server context : " + context);
                started = true;
                
                i = intent;
                //t = new MyThread();
                //t.start();
                
            }
            
            return Service.START_STICKY;
        }
    
        protected void onMessage(String message) 
        {
            
            Log.d(TAG, "onMessage - context: " + context);
    
            // Extract the payload from the message
            
            Log.d(TAG, "onMessage - extras: " + message);
            if (message != null)
            {
                
                // Send a notification if there is a message and not in foreground
                if (!MulticastCom.isInForeground() && message.length() != 0) {
                    createNotification(context, message);
                }else if(message.length() != 0){
                    
                    //Echo.sendExtras(extras);
                    try{
                        //Echo.sendJavascript(new JSONObject("{payload:{msg:'" + message +"'}}"));
                        MulticastCom.sendBackMsg(new JSONObject("{payload:{msg:'" + message +"'}}"));
                    }catch(org.json.JSONException e){
                        
                        
                    }
                }
            }
        }
    
        private static String getAppName(Context context)
        {
            CharSequence appName = 
                    context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());
            
            Log.d(TAG, "getAppName - appName: " + appName);
            
            return (String)appName;
        }
    
    
        public void createNotification(Context context, String message)
        {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String appName = getAppName(this);
    
            Intent notificationIntent = new Intent(this, MulticastComActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            notificationIntent.putExtra("msg", message);
    
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            
            NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("Notifications Title")
                    .setTicker("Ticker Title")
                    .setContentIntent(contentIntent);
    
            //String message = extras.getString("message");
            if (message != null) {
                mBuilder.setContentText(message);
            } else {
                mBuilder.setContentText("<missing message content>");
            }
    
            String msgcnt = "5";
            if (msgcnt != null) {
                mBuilder.setNumber(Integer.parseInt(msgcnt));
            }
            
            mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
        }
    
    
        @Override
        public IBinder onBind(Intent intent) {
            // We don't provide binding, so return null
            return null;
        }
    
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            Log.d(TAG, "Server onConfigurationChanged ");
            super.onConfigurationChanged(newConfig);
        }
        
        @Override
        public void onLowMemory() {
            Log.d(TAG, "Server onLowMemory ");
            //started = false;
            /*multicastLock.release();
            wifilock.release();
           try{
                if(s != null){
                    s.leaveGroup(InetAddress.getByName(group));
                    s.close();
                }
            }catch(UnknownHostException e){
                
                
            }catch (IOException e){ 
                
            }*/
            super.onLowMemory();
        }
    
        @Override
        public void onDestroy() {
            started = false;
            Log.v(TAG,"Server destroyed");
            // Once your finish using it, release multicast lock
            if (multicastLock != null) {
                multicastLock.release();
                multicastLock = null;
            }
            /*multicastLock.release();
            wifilock.release();
            try{
                if(s != null){
                    s.leaveGroup(InetAddress.getByName(group));
                    s.close();
                }
            }catch(UnknownHostException e){
                
                
            }catch (IOException e){ 
                
            }*/
            super.onDestroy();
        }
    
      
        public void onTerminate(){
            
            Log.v(TAG,"Server destroyed");
            //super.onTerminate();
        }
    
        public static void cancelNotification(Context context)
        {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel((String)getAppName(context), NOTIFICATION_ID);	
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
    
        /**
         * Open a multicast socket on the mDNS address and port.
         * @throws IOException
         */
        private void openSocket() throws IOException {
            //int port = 5000;
            multicastSocket = new MulticastSocket(port);
            multicastSocket.setTimeToLive(2);
            multicastSocket.setReuseAddress(true);
            //multicastSocket.setNetworkInterface(networkInterface);
            multicastSocket.joinGroup(InetAddress.getByName(group));
        }
    
    
        private final class MyThread extends Thread{
        
            public void run(){ 
                //String group = "225.4.5.6";
                buf = new byte[1024];
                //int port = 5000;
                
                 try{
                        // Which port should we listen to
                        
                        //String message;
                        // Which address
                        
                        // Create the socket and bind it to port 'port'.
                        Log.i(TAG, "Server started. Listening to the port 5000");
                        //s = new MulticastSocket(port);
                        //s.setReuseAddress(true);
                        //s.setTimeToLive(2);
                        
                        //s.joinGroup(InetAddress.getByName(group));
                        
                        // join the multicast group
                       
                        // Now the socket is set up and we are ready to receive packets
                        // Create a DatagramPacket and do a receive
                        
                        Log.i("MessageRecived", "antes do ciclo infinito while");
                        pack = new DatagramPacket(buf, buf.length);
                    
                while(true){
                        
                        java.util.Arrays.fill(buf, (byte) 0); // clear buffer
                        Log.i("MessageRecived", "a esperar por datagramas myip: " + getLocalIpAddress() );  
                        // zero the incoming buffer for good measure.
                        //java.util.Arrays.fill(pack, (byte) 0); // clear buffer
                        multicastSocket.receive(pack);
                        final String message = new String(pack.getData(), 0, pack.getLength(),"UTF-8");
                        Log.i("MessageRecived", "depois de esperar por datagramas "+ message);    
                    
                        t2 = new Thread(new Runnable(){
                            //cordova.getThreadPool().execute(new Runnable() {
                                
                            public void run(){
                            
                                
                                Log.i("MessageRecived", "começou um novo thread");
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
                                //String _d = new String("javascript:app.testeNotifications({msg:'" +message+"'})");
                                //gECB = "app.testeNotifications".toString();
                                //String _d = "javascript:" + gECB + "(" + message.toString() + ")";
                                Log.v("MessageRecived", "chamar " + message);
                                //Toast.makeText(/*cordova.getActivity()*/getApplicationContext(), "Wifi Connected", Toast.LENGTH_SHORT).show();
                                //Echo.sendJavascript(_d); 
                                onMessage(message);
                        
                        }});
                   
                    t2.start();
                }
            }catch(UnknownHostException ue){
                //callbackContext.error("UnknownHostException. " + ue);
                Log.v("MessageRecived", "UnknownHostException " + ue);
            }catch(java.net.BindException b){
                //callbackContext.error("java.net.BindException. " + b);
                Log.v("MessageRecived", "java.net.BindException " + b);
            }catch(IOException e){
                //callbackContext.error("IOException. " + e);
                // reopen the socket
                try {
                    openSocket();
                } catch (IOException e1) {
                    //activity.ipc.error(new RuntimeException("socket reopen: "+e1.getMessage()));
                    //return;
                    Log.v("MessageRecived", "resume server from IOException " + e);
                }
                Log.v("MessageRecived", "IOException " + e);
            }
                
                
            }
    }
    
    
    //private void serverNormal(){
    private final class MyThread2 extends Thread{
        
            public void run(){ 
            
                try {
                     serverSocket = new ServerSocket(4444);  //Server socket
     
                } catch (IOException e) {
                    System.out.println("Could not listen on port: 4444");
                    //callbackContext.error("IOException. " + e);
                }
         
                System.out.println("Server started. Listening to the port 4444");
                Log.i("ServerActivity", "Server started. Listening to the port 4444");
         
                while (true) {
            
                    try {
         
                        clientSocket = serverSocket.accept();   //accept the client connection
                        
                        t2 = new Thread(new Runnable(){
                        //cordova.getThreadPool().execute(new Runnable() {
                            
                            public void run(){
                                try{
                                    inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                                    bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                                   String message = bufferedReader.readLine();
                                    
                                    Log.i("MessageRecived", "a menssagem recebida é: " + message);
                                    //System.out.println(message);
                                    inputStreamReader.close();
                                    clientSocket.close();
                                    //callbackContext.success(message);
                                    onMessage(message);
                                    
                                }catch (IOException ex) {
                                    System.out.println("Problem in message reading");
                                    //callbackContext.error("IOException. " + ex);
                                }
                            }
                        });
                        
                        t2.start();
         
                    } catch (IOException ex) {
                        System.out.println("Problem in message reading");
                        //callbackContext.error("IOException. " + ex);
                    }
                }
            }
        }
    
    
}