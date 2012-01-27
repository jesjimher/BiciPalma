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
//TODO: Mostrar estado del servicio (bicis en circulación, averiadas, etc.)
//TODO: Ir directamente a lista de estaciones
        
		// APAÑO, habría que quitar esta pantalla
        Intent i=new Intent(this,MesProperesActivity.class);
		startActivity(i);
    }

    // Handlers de botones
    public void onClick(View v) {
		if (v.getId()==R.id.mespropera) {
			Intent i=new Intent(this,MesProperesActivity.class);
			startActivity(i);
		}
		if (v.getId()==R.id.mapa) {
		}		
	}
        
}