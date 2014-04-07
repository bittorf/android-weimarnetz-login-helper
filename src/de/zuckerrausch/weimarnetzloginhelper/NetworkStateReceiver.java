package de.zuckerrausch.weimarnetzloginhelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    private static final String PATH = "/cgi-bin/luci/freifunk/captive";
    //private static final String PATH = "/cgi-bin-welcome.sh";
    
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    DhcpInfo d;
    WifiManager wifii;
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
     
        wifii= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        d = wifii.getDhcpInfo();

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                
                if ( ni.getTypeName().equals("mobile") ){
                	Log.i(TAG, "Detected mobile, exiting app.");
                	return;
                }
                
                final String gateway = intToIp(d.gateway);    
                final String fullUrl = "http://" + gateway + PATH;
                
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                long lastUse = settings.getLong("lastUse",0);
                
                long now = System.currentTimeMillis() / 1000;
                
                if ( lastUse + 20 < now && settings.getBoolean("accepted", false)){
                	
                	Editor e = settings.edit();
		        	e.putLong("lastUse", now);
		        	e.commit();
                	
                	Runnable task = new Runnable() {
                        public void run() {
                        	HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(fullUrl);
                           	try {
                           		
                           		Log.d( TAG, "Url: " + fullUrl );
            				
            					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            			        nameValuePairs.add(new BasicNameValuePair("REDIRECTED", "1"));
            			        nameValuePairs.add(new BasicNameValuePair("FORM_RULES", "on"));
            			        nameValuePairs.add(new BasicNameValuePair("ORIGIN", "http://" + gateway + "/"));
            			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            					
            			        HttpResponse response = httpclient.execute(httppost);
            			        
            			        Log.d( TAG, "Response: "+response.getStatusLine().getStatusCode());
            			        
            				} catch (MalformedURLException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				} catch (IOException e) {
            					// TODO Auto-generated catch block
            					e.printStackTrace();
            				}
                        }
                      };
                      worker.schedule(task, 5, TimeUnit.SECONDS);
                }
                
               
                
                
                
                
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
            }
        }
    }
    
    public String intToIp(int addr) {
        return  ((addr & 0xFF) + "." + 
                ((addr >>>= 8) & 0xFF) + "." + 
                ((addr >>>= 8) & 0xFF) + "." + 
                ((addr >>>= 8) & 0xFF));
    }
}
