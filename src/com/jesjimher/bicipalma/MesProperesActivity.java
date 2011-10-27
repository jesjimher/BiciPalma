package com.jesjimher.bicipalma;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MesProperesActivity extends Activity implements LocationListener,DialogInterface.OnDismissListener {
	LocationManager locationManager;
	long numUpdates=0;
	Location lBest;
	// Tiempo inicial de búsqueda de ubicación
	long tIni;
	ProgressDialog dBuscaUbic;
	private String mUbic;
	
	private static final int DIEZ_SEGS=10*1000;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesproperes);
        
    	dBuscaUbic=ProgressDialog.show(this, "","Buscando ubicación",true,true);
//        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Comprobar si está el GPS activado, y dar la opción
/*        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	AlertDialog.Builder b=new AlertDialog.Builder(this);
        	b.setMessage("El GPS está desactivado. ¿Quiere activarlo para tener mayor precisión?")
        	 .setCancelable(false)
        	 .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                 public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                	 startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));; 
                 }
             })
             .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                     dialog.cancel();
                }
            });
        	AlertDialog alert=b.create();
        	b.show();
        }*/
        // Comprobar si se ha activado o no el GPS, y decidir el método para ubicarse
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	mUbic=LocationManager.GPS_PROVIDER;
        else {
        	Toast.makeText(getApplicationContext(), "Usando posicionamiento por red. Active el GPS para mayor precisión", Toast.LENGTH_LONG).show();
        	mUbic=LocationManager.NETWORK_PROVIDER;
        }
        // Activar búsqueda de ubicación        
        locationManager.requestLocationUpdates(mUbic, 0, 0, this);        
    	//Toast.makeText(getApplicationContext(), "Activado", Toast.LENGTH_SHORT).show();

    	// Guardar el inicio de búsqueda de ubicación para no pasarse de tiempo
    	//tIni=new Date().getTime();
        tIni=System.currentTimeMillis();
    }
        
    // Nuevo ajuste de ubicación
    // Guardamos 3 ajustes, o 10s
    public void onLocationChanged(Location location) {
    	// Mirar si corresponde procesar la ubicación
    	if ((numUpdates<3) && ((location.getTime()-tIni)<DIEZ_SEGS)) {
    		// Si la nueva ubicación es mejor, guardarla
    		if (isBetterLocation(location, lBest)) {
    			lBest=location;
		    	Toast.makeText(getApplicationContext(), "Mejor ubicación encontrada", Toast.LENGTH_SHORT).show();
		    	TextView tv=(TextView)findViewById(R.id.prova);
		    	tv.setText(location.toString());
    		} else {
		    	Toast.makeText(getApplicationContext(), "Ignorando ubicación chunga", Toast.LENGTH_SHORT).show();
    		}
    		numUpdates++;
    	} else {
    		Toast.makeText(getApplicationContext(), "Fin de búsqueda", Toast.LENGTH_SHORT).show();
    		locationManager.removeUpdates(this);
    		dBuscaUbic.dismiss();
    	}    	
    	TextView tv=(TextView)findViewById(R.id.prova2);
    	tv.setText(new Date(tIni).toString());
    	tv=(TextView)findViewById(R.id.prova3);
    	tv.setText(new Date(location.getTime()).toString());
    }
    
    /** Determines whether one Location reading is better than the current Location fix
     *  (MODIFICADO PARA ADAPTARLO A UBICACIÓN RÁPIDA)
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
   protected boolean isBetterLocation(Location location, Location currentBestLocation) {
       if (currentBestLocation == null) {
           // A new location is always better than no location
           return true;
       }

       // Check whether the new location fix is newer or older
       long timeDelta = location.getTime() - currentBestLocation.getTime();
       boolean isNewer = timeDelta > 0;

       // Check whether the new location fix is more or less accurate
       int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
       boolean isLessAccurate = accuracyDelta > 0;
       boolean isMoreAccurate = accuracyDelta < 0;
       boolean isSignificantlyLessAccurate = accuracyDelta > 200;

       // Check if the old and new location are from the same provider
       boolean isFromSameProvider = location.getProvider().equals(currentBestLocation.getProvider());

       // Determine location quality using a combination of timeliness and accuracy
       if (isMoreAccurate) {
           return true;
       } else if (isNewer && !isLessAccurate) {
           return true;
       } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
           return true;
       }
       return false;
   }


    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
    
    // Dejamos de buscar ubicación al salir
    @Override
    protected void onStop() {
    	Toast.makeText(getApplicationContext(), "Fin de búsqueda de ubicación", Toast.LENGTH_SHORT).show();
    	locationManager.removeUpdates(this);    	
    	super.onStop();
    }

	public void onDismiss(DialogInterface arg0) {
		Toast.makeText(getApplicationContext(), "Fin de búsqueda de ubicación", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);		
	}
    
}