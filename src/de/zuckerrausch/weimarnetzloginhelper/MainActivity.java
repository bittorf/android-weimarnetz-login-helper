package de.zuckerrausch.weimarnetzloginhelper;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final CheckBox chk = (CheckBox) findViewById(R.id.checkRules);
		chk.setChecked(settings.getBoolean("accepted",false));

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		WebView wv;  
        wv = (WebView) findViewById(R.id.webview);  
        wv.loadUrl("file:///android_asset/rules.html");
        
        Button btn = (Button) findViewById(R.id.btn_enable);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editor e = settings.edit();
				
				if ( chk.isChecked() ){
		        	e.putBoolean("accepted", true);
		        	
		        	
		        	builder.setTitle("Der Zugang ist freigeschaltet");
		        	builder.setMessage("Dein Geraet akzeptiert die Lizenzbedingungen nun automatisch.");
		        	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int id) {
		                	System.exit(0);
		                }
		            });
		        	AlertDialog dialog = builder.show();
		        	
				} else {
					e.putBoolean("accepted", false);
				}
				e.commit();
			}
		});
	}
}
