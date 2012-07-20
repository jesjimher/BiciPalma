package com.jesjimher.bicipalma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jesjimher.bicipalma.ResultadoBusqueda;

public class MesProperesActivity extends Activity implements LocationListener,DialogInterface.OnDismissListener,SharedPreferences.OnSharedPreferenceChangeListener {
	LocationManager locationManager;
	Location lBest=null;
	// Tiempo inicial de búsqueda de ubicación
	long tIni;
	ProgressDialog dRecuperaEst;
	private String mUbic;
	private SharedPreferences prefs;
	
	private static final int DIEZ_SEGS=10*1000;
	
	private ArrayList<Estacion> estaciones;
	
	private RecuperarEstacionesTask descargaEstaciones;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesproperes);
        
        // TODO: No volver a descargar en cambios de orientación
        // Leer las estaciones de disco si están disponibles
        try {
			File f=new File("estaciones.json");
			if (f.exists()) {
				BufferedReader fis;
				fis = new BufferedReader(new FileReader(f));
				String s=fis.readLine();
				leerFicheroEstaciones(new JSONArray(s));
				// Poner nº de bicis/anclajes a desconocido
				for(int i=0;i<estaciones.size();i++) {
					estaciones.get(i).setAnclajesLibres(-1);
					estaciones.get(i).setBicisLibres(-1);
				}
				actualizarListado();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        	
        // Descargar las estaciones desde la web (en un thread aparte)
        // Cuando acabe, se activará la búsqueda de ubicación
        estaciones=new ArrayList<Estacion>();
        descargaEstaciones=new RecuperarEstacionesTask(this);
        descargaEstaciones.execute();
        
        // Activar búsqueda de ubicación
 //    	dBuscaUbic=ProgressDialog.show(c, "",getString(R.string.buscandoubica),true,true);
//        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Comprobar si se ha activado o no el GPS, y decidir el método para ubicarse
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	mUbic=LocationManager.GPS_PROVIDER;
        else {
        	Toast.makeText(getApplicationContext(), R.string.avisonogps, Toast.LENGTH_LONG).show();
        	mUbic=LocationManager.NETWORK_PROVIDER;
        }
        locationManager.requestLocationUpdates(mUbic, 0, 0, (LocationListener) this);        

    	// Guardar el inicio de búsqueda de ubicación para no pasarse de tiempo
        // TODO: Crear un Timer que pare la búsqueda de ubicación cuando pase un tiempo máximo
    	//tIni=new Date().getTime();
        tIni=System.currentTimeMillis();

        // Crear listener para abrir una estación en Google Maps al seleccionarla
        // TODO: Mirar si abrir GMaps externo o interno
        // TODO: Menú con clic largo para abrir en gmaps, navigation
        ListView lv=(ListView) findViewById(R.id.listado);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v,int position,long id) {
        		ResultadoBusqueda rb=(ResultadoBusqueda) parent.getAdapter().getItem(position);
        		// Más rápido en posicionar, pero no muestra pin
//        		String uri="geo:"+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude();
        		String uri="geo:0,0?q="+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude()+" ("+rb.getEstacion().getNombre()+")";
        		startActivity(new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(uri)));
//        		Toast.makeText(getApplicationContext(), rb.getEstacion().getNombre(),Toast.LENGTH_SHORT).show();
        	}
		});
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
        
    /**
     *	Actualiza el listado de estaciones 
     */
    public void actualizarListado() {
    	// Si no se han descargado las estaciones y hay ubicación disponible, no hacer nada
    	if ((estaciones.size()==0) || (lBest==null))
    		return;
    	
    	// Ocultar el diálogo de búsqueda de ubicación si se estaba visualizando
    	if (dRecuperaEst.isShowing())
    		dRecuperaEst.dismiss();
    	else	    		
    		Toast.makeText(getApplicationContext(), "Actualizando resultados", Toast.LENGTH_SHORT).show();

    	// Mirar si está activa la opción de ocultar estaciones vacías
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ocultarVacios=sharedPrefs.getBoolean("ocultarVaciosPref", false);

        // Calcular distancias desde la ubicación actual hasta cada estación, generando
		// un objeto Resultado
        ArrayList<ResultadoBusqueda> result=new ArrayList<ResultadoBusqueda>();
        Iterator<Estacion> i=estaciones.iterator();
        while (i.hasNext()) {
        	Estacion e=(Estacion) i.next();
        	Location aux=e.getLoc();
        	Double dist=new Double(lBest.distanceTo(aux));
        	if (!(ocultarVacios && (e.getBicisLibres()<=0)))
        		result.add(new ResultadoBusqueda(e,dist));
        }	        
        	       	        
        // Ordenar por distancia
        Collections.sort(result);	        

        // Mostrar
        ListView l=(ListView) this.findViewById(R.id.listado);
        l.setAdapter(new ResultadoAdapter(this,result));	    	
    }
    
    // Cuando llega una nueva ubicación mejor que la actual, reordenamos el listado
    public void onLocationChanged(Location location) {
		// Sólo hacer algo si la nueva ubicación es mejor que la actual
    	if (isBetterLocation(location, lBest)) {
	    		
			// Actualizar precisión
	    	TextView pre=(TextView) this.findViewById(R.id.precisionNum);
	    	if (location.hasAccuracy())
	    		pre.setText(String.format("%.0f m",location.getAccuracy()));
	    	else
	    		pre.setText("Desconocida");
	    	
	    	lBest=location;
	        
    		actualizarListado();
        
		} else {
	    	//Toast.makeText(getApplicationContext(), "Ignorando ubicación chunga", Toast.LENGTH_SHORT).show();
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
    public void onPause() {
    	if (locationManager!=null)
    		locationManager.removeUpdates(this);
    	// Si habia descargas en curso, pararlas
    	if (descargaEstaciones!=null) {
    		descargaEstaciones.cancel(true);
    		descargaEstaciones=null;
    	}
    	super.onPause();
    }

	public void onDismiss(DialogInterface arg0) {
//		Toast.makeText(getApplicationContext(), "Fin de búsqueda de ubicación", Toast.LENGTH_SHORT).show();
    	if (locationManager!=null)
    		locationManager.removeUpdates(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.preferencias:
	    	Intent settingsActivity = new Intent(getBaseContext(),PreferenciasActivity.class);
	    	startActivity(settingsActivity);
	        prefs.registerOnSharedPreferenceChangeListener(this);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}	
	// Clase privada para recuperar la lista de estaciones en segundo plano
	// TODO: Mostrar un ProgressDialog con una barra de progreso
	private class RecuperarEstacionesTask extends AsyncTask<Void, Void, ArrayList<Estacion>> {

		Context c;
		
	    public RecuperarEstacionesTask(Context c) {
	    	this.c=c;
	    }
	    
	    @Override
		protected void onPreExecute() {
	    	 dRecuperaEst = ProgressDialog.show(c, "", getString(R.string.recuperandolista),true,true);
	    }
		
	    // Cuando acabe de descargar, activar la búsqueda de ubicación 
	    protected void onPostExecute(ArrayList<Estacion> result) {
	    	// Cerrar diálogo y guardar resultados
	    	estaciones=result;
	    	
	    	dRecuperaEst.setTitle(R.string.buscandoubica);
	    	
	    	actualizarListado();

	    }

		@Override
		protected ArrayList<Estacion> doInBackground(Void... arg0) {
	    	JSONArray json=BicipalmaJsonClient.connect("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
	    	
	    	return leerFicheroEstaciones(json);
		}
	 }
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		  if (key.equals("ocultarVaciosPref"))
			  actualizarListado(); 		
	}

	/**
	 * @param json
	 * @return
	 * @throws FileNotFoundException 
	 */
	private ArrayList<Estacion> leerFicheroEstaciones(JSONArray json) {
		// Extraer estaciones del JSON
		// TODO: Guardarlas en data
		// TODO: Si falla, mostrar un mensaje y usar la copia local, sin nº de bicis libres
		// TODO: Si la copia local es antigua, actualizarla
		ArrayList<Estacion> est=new ArrayList<Estacion>();
		for(int i=0;i<json.length();i++) {
			try {
				String nombre=json.getJSONObject(i).getString("alia");
				Location pos=new Location("network");
				pos.setLatitude(json.getJSONObject(i).getDouble("realLat"));
				pos.setLongitude(json.getJSONObject(i).getDouble("realLon"));
				Estacion e=new Estacion(nombre,pos);
				String html=json.getJSONObject(i).getString("paramsHtml");
				int pos2=html.indexOf("Bicis Libres:</span>")+"Bicis Libres:</span>".length();
				if (pos2>0)
					e.setBicisLibres(Long.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setBicisLibres(0);
				pos2=html.indexOf("Anclajes Libres:</span>")+"Anclajes Libres:</span>".length();
				if (pos2>0)
					e.setAnclajesLibres(Long.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setAnclajesLibres(0);
				est.add(e);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Escribir el JSON a disco para acelerar futuros accesos
		try {
			FileOutputStream fos=openFileOutput("estaciones.json", Context.MODE_PRIVATE);
			fos.write(json.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return est;
	}	
	
}

