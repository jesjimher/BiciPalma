package com.jesjimher.bicipalma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jesjimher.bicipalma.ResultadoBusqueda;

// TODO: Sustituir ListActivity por una Activity normal y un layout normal
public class MesProperesActivity extends ListActivity implements LocationListener,DialogInterface.OnDismissListener {
	LocationManager locationManager;
	Location lBest;
	// Tiempo inicial de búsqueda de ubicación
	long tIni;
	ProgressDialog dBuscaUbic;
	private String mUbic;
	
	private static final int DIEZ_SEGS=10*1000;
	
	private TreeMap<String,String> estaciones;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.mesproperes);

        // Recuperar lista estaciones desde la web
    	ProgressDialog progress = ProgressDialog.show(this, "Espere...", "Recuperando lista de estaciones");
    	JSONObject json=BicipalmaJsonClient.connect("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
    	progress.dismiss();
        
        // TODO: Añadir todas las estaciones
        // TODO: Hacerlo de forma más limpia, desde resources
        estaciones=new TreeMap();
        estaciones.put("Parc Estacions","39.57616,2.65553");
        estaciones.put("Pça Espanya","39.57536,2.6541");
        estaciones.put("Blanquerna-Sallent","39.57815,2.6510");
        estaciones.put("Blanquerna-Bartomeu Pou","39.58705,2.6491");                
        estaciones.put("Plaça de la Reina","39.567964,2.645897");
        estaciones.put("Plaça Santa Eulàlia","39.569225,2.650682");
        estaciones.put("Plaça Rei Joan Carles I","39.571409,2.646911");
        estaciones.put("Jaume III","39.572533,2.642727");
        estaciones.put("Porta Santa Catalina","39.571169,2.641257");
        estaciones.put("Plaça del Mercat","39.572806,2.650012");
        estaciones.put("Via Roma","39.575279,2.647501");
        
    	dBuscaUbic=ProgressDialog.show(this, "","Determinando ubicación",true,true);
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
        // TODO: Sustituir con un Timer que pare la búsqueda de ubicación cuando pase un tiempo máximo
    	//tIni=new Date().getTime();
        tIni=System.currentTimeMillis();
    }
        
    // Cuando llega una nueva ubicación mejor que la actual, reordenamos el listado
    public void onLocationChanged(Location location) {
    	// TODO: Mostrar precisión de la ubicación
		// Sólo hacer algo si la nueva ubicación es mejor que la actual
    	if (isBetterLocation(location, lBest)) {
	    	// Ocultar el diálogo de búsqueda de ubicación si se estaba visualizando
	    	if (dBuscaUbic.isShowing())
	    		dBuscaUbic.dismiss();
	    	else	    		
	    		Toast.makeText(getApplicationContext(), "Actualizando resultados", Toast.LENGTH_SHORT).show();
	    		
			lBest=location;
	        
	        // Calcular distancias desde la ubicación actual hasta cada estación, generando
			// un objeto Resultado
	        ArrayList<ResultadoBusqueda> result=new ArrayList<ResultadoBusqueda>();
	        Iterator i=estaciones.keySet().iterator();
	        while (i.hasNext()) {
	        	String e=(String) i.next();
	        	String coords=estaciones.get(e);
	        	Location aux=new Location(location);
	        	aux.setLatitude(new Double(coords.split(",")[0]));
	        	aux.setLongitude(new Double(coords.split(",")[1]));
	        	Double dist=new Double(location.distanceTo(aux));
	        	result.add(new ResultadoBusqueda(e,aux,dist));
	        }
	        
	        // Ordenar por distancia
	        Collections.sort(result);
	        
	        // Mostrarlo en el ListView
	        ArrayList<String> est=new ArrayList<String>();
	        i=result.iterator();
	        while (i.hasNext()) {
	        	ResultadoBusqueda e=(ResultadoBusqueda) i.next();
	        	est.add(String.format("%s (%.2f km)", e.getNombre(),e.getDist()/1000));
	        }
//	        this.setListAdapter(new ArrayAdapter<String>(this,R.layout.list_item,est));	        
	        this.setListAdapter(new ResultadoAdapter(this,result)); 	        
		} else {
	    	Toast.makeText(getApplicationContext(), "Ignorando ubicación chunga", Toast.LENGTH_SHORT).show();
		}    
    }
    
    /** Determines whether one Location reading is better than the current Location fix
     *  (EXTRAÍDO DEL SDK, MODIFICADO PARA ADAPTARLO A UBICACIÓN RÁPIDA)
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
   protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	   // A new location is always better than no location
       if (currentBestLocation == null)
           return true;

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
       } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
           return true;
       return false;
   }


    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}
    
    // Dejamos de buscar ubicación al salir
    @Override
    protected void onStop() {
//    	Toast.makeText(getApplicationContext(), "Fin de búsqueda de ubicación", Toast.LENGTH_SHORT).show();
    	locationManager.removeUpdates(this);    	
    	super.onStop();
    }

	public void onDismiss(DialogInterface arg0) {
//		Toast.makeText(getApplicationContext(), "Fin de búsqueda de ubicación", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);		
	}
    
}