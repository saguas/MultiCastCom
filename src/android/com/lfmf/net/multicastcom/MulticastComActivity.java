package com.lfmf.net.multicastcom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;



public class MulticastComActivity extends Activity
{
    
    private static final String TAG = "MessageRecived";
    
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        Log.v(TAG, "MyActivityTeste onCreate");
        
        boolean isPushPluginActive = MulticastCom.isActive();
        Log.d(TAG, "MyActivityTeste onCreate : isPushPluginActive " + isPushPluginActive);
        processPushBundle(isPushPluginActive);
        
        MyServiceTeste.cancelNotification(this);
        
        finish();
        
        if (!isPushPluginActive) {
            Log.d(TAG, "MyActivityTeste onCreate : Força lançamento da aplicação ");
			forceMainActivityReload();
		}
        
    }
    
    /**
	 * Takes the pushBundle extras from the intent, 
	 * and sends it through to the PushPlugin for processing.
	 */
	private void processPushBundle(boolean isPushPluginActive)
	{
		Bundle extras = getIntent().getExtras();

		if (extras != null)	{
			
			//Bundle originalExtras = extras.getBundle("pushBundle");
            

			if ( !isPushPluginActive ) { 
				//originalExtras.putBoolean("coldstart", true);
                extras.putBoolean("coldstart", true);
			}

			//Echo.sendExtras(originalExtras);
            MulticastCom.sendExtras(extras);
		}
	}
    
    
    /**
	 * Forces the main activity to re-launch if it's unloaded.
	 */
	private void forceMainActivityReload()
	{
		PackageManager pm = getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());    		
		startActivity(launchIntent);
	}
    
    
}