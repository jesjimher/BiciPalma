package com.jesjimher.bicipalma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jesjimher.bicipalma.ResultadoBusqueda;

public class MesProperesActivity extends Activity implements LocationListener,DialogInterface.OnDismissListener {
	LocationManager locationManager;
	Location lBest;
	// Tiempo inicial de búsqueda de ubicación
	long tIni;
	ProgressDialog dBuscaUbic,dRecuperaEst;
	private String mUbic;
	
	private static final int DIEZ_SEGS=10*1000;
	
	private ArrayList<Estacion> estaciones;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesproperes);

        // TODO: No volver a descargar en cambios de orientación
        // Descargar las estaciones desde la web (en un thread aparte)
        // Cuando acabe, se activará la búsqueda de ubicación
        estaciones=new ArrayList<Estacion>();
        new RecuperarEstacionesTask(this).execute();
    }
        
    // Cuando llega una nueva ubicación mejor que la actual, reordenamos el listado
    public void onLocationChanged(Location location) {
		// Sólo hacer algo si la nueva ubicación es mejor que la actual
    	if (isBetterLocation(location, lBest)) {
	    	// Ocultar el diálogo de búsqueda de ubicación si se estaba visualizando
	    	if (dBuscaUbic.isShowing())
	    		dBuscaUbic.dismiss();
	    	else	    		
	    		Toast.makeText(getApplicationContext(), "Actualizando resultados", Toast.LENGTH_SHORT).show();
	    		
			// Actualizar precisión
	    	TextView pre=(TextView) this.findViewById(R.id.precisionNum);
	    	if (location.hasAccuracy())
	    		pre.setText(String.format("%.0f m",location.getAccuracy()));
	    	else
	    		pre.setText("Desconocida");
	    	
	    	lBest=location;
	        
	        // Calcular distancias desde la ubicación actual hasta cada estación, generando
			// un objeto Resultado
	        ArrayList<ResultadoBusqueda> result=new ArrayList<ResultadoBusqueda>();
	        Iterator i=estaciones.iterator();
	        while (i.hasNext()) {
	        	Estacion e=(Estacion) i.next();
	        	Location aux=e.getLoc();
	        	Double dist=new Double(location.distanceTo(aux));
	        	result.add(new ResultadoBusqueda(e,dist));
	        }
	        
	        // Ordenar por distancia
	        Collections.sort(result);
	        
	        // Mostrarlo en el ListView
	        ArrayList<String> est=new ArrayList<String>();
	        i=result.iterator();
	        while (i.hasNext()) {
	        	ResultadoBusqueda e=(ResultadoBusqueda) i.next();
	        	est.add(String.format("%s (%.2f km)", e.getEstacion().getNombre(),e.getDist()/1000));
	        }

	        ListView l=(ListView) this.findViewById(R.id.listado);
	        l.setAdapter(new ResultadoAdapter(this,result));
	        
	        // TODO: Crear handler que al hacer clic abra Google Maps
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

	// Clase privada para recuperar la lista de estaciones en segundo plano
	// TODO: Mover a BiciPalmaActivity, tiene más sentido allí. Pasar luego los datos con un Bundle
	private class RecuperarEstacionesTask extends AsyncTask<Void, Void, ArrayList<Estacion>> {

		Context c;
		
	    public RecuperarEstacionesTask(Context c) {
	    	this.c=c;	    	
	    }
	    
	    @Override
		protected void onPreExecute() {
	    	 dRecuperaEst = ProgressDialog.show(c, "", "Recuperando lista de estaciones",true,true);
	    }
		
	    // Cuando acabe de descargar, activar la búsqueda de ubicación 
	    protected void onPostExecute(ArrayList<Estacion> result) {
	    	// Cerrar diálogo y guardar resultados
	    	dRecuperaEst.dismiss();
	    	estaciones=result;

	    	dBuscaUbic=ProgressDialog.show(c, "","Determinando ubicación",true,true);
//	        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_SHORT).show();
	        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

	        // Comprobar si se ha activado o no el GPS, y decidir el método para ubicarse
	        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	        	mUbic=LocationManager.GPS_PROVIDER;
	        else {
	        	Toast.makeText(getApplicationContext(), "Usando posicionamiento por red. Active el GPS para mayor precisión", Toast.LENGTH_LONG).show();
	        	mUbic=LocationManager.NETWORK_PROVIDER;
	        }
	        // Activar búsqueda de ubicación        
	        locationManager.requestLocationUpdates(mUbic, 0, 0, (LocationListener) c);        

	    	// Guardar el inicio de búsqueda de ubicación para no pasarse de tiempo
	        // TODO: Crear un Timer que pare la búsqueda de ubicación cuando pase un tiempo máximo
	    	//tIni=new Date().getTime();
	        tIni=System.currentTimeMillis();
	    }

		@Override
		protected ArrayList<Estacion> doInBackground(Void... arg0) {
	    	JSONArray json=BicipalmaJsonClient.connect("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
	    	
	    	// Extraer estaciones del JSON
	    	// TODO: Guardarlas en data
	        // TODO: Si falla, mostrar un mensaje y usar la copia local, sin nº de bicis libres
	        // TODO: Si la copia local es antigua, actualizarla
	    	// TODO: Los acentos dan problemas, convertir
	    	ArrayList<Estacion> est=new ArrayList<Estacion>();
	    	for(int i=0;i<json.length();i++) {
	    		try {
					String nombre=json.getJSONObject(i).getString("alia");
					Location pos=new Location("network");
					pos.setLatitude(json.getJSONObject(i).getDouble("realLat"));
					pos.setLongitude(json.getJSONObject(i).getDouble("realLon"));
	    			Estacion e=new Estacion(nombre,pos);
	    			est.add(e);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	}
	    	return est;
		}
	 }	
	
}

