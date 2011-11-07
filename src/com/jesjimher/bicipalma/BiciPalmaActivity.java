package com.jesjimher.bicipalma;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BiciPalmaActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Establecer los handlers apropiados
        Button b=(Button)findViewById(R.id.mapa);
        b.setOnClickListener(this);
        
        b=(Button)findViewById(R.id.mespropera);
        b.setOnClickListener(this);
    }

    // Handlers de botones
    public void onClick(View v) {
		if (v.getId()==R.id.mespropera) {
			//Toast.makeText(getApplicationContext(), "Més propera", Toast.LENGTH_SHORT).show();
			Intent i=new Intent(this,MesProperesActivity.class);
			startActivity(i);
		}
		if (v.getId()==R.id.mapa) {
			//Toast.makeText(getApplicationContext(), "Mapa", Toast.LENGTH_SHORT).show();			
		}		
	}
        
}