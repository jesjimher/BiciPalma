/* Copyright 2012 Jes�s Jim�nez Herranz
 * 
 * This file is part of BuscaBici
 * 
 * BuscaBici is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * BuscaBici is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with BuscaBici. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jesjimher.bicipalma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jesjimher.bicipalma.ResultadoBusqueda;

public class MesProperesActivity extends Activity implements LocationListener,DialogInterface.OnDismissListener,SharedPreferences.OnSharedPreferenceChangeListener,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
	LocationManager locationManager;
	Location lBest=null;
	// Tiempo inicial de b�squeda de ubicaci�n
	long tIni;
	ProgressDialog dRecuperaEst;
	private String mUbic;
	private SharedPreferences prefs;
	
	private ArrayList<Estacion> estaciones;
	
	private RecuperarEstacionesTask descargaEstaciones;
	
	private boolean estatWifi=false;
	private String providerCoarse;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mesproperes);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Si s'ha d'activar el wifi en inici, fer-ho
    	WifiManager wm=(WifiManager) this.getSystemService(Context.WIFI_SERVICE);        	
    	// Guardar el estado actual para restaurarlo al salir
    	this.estatWifi=wm.isWifiEnabled();
        if (prefs.getBoolean("activarWifiPref", false)) 
        	wm.setWifiEnabled(true);        	
        
        // Cargamos la versi�n cacheada de las estaciones
        leerCacheEstaciones();

        actualizarListado();
        	
        // Iniciamos la descarga de las estaciones y su estado desde la web (en un thread aparte)
        descargaEstaciones=new RecuperarEstacionesTask(this);
        descargaEstaciones.execute();
        
        // Inicialmente se busca por red (m�s r�pido)
 //    	dBuscaUbic=ProgressDialog.show(c, "",getString(R.string.buscandoubica),true,true);
//        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
		criteria.setAccuracy( Criteria.ACCURACY_COARSE );
		providerCoarse = locationManager.getBestProvider( criteria, true );
		
		if ( providerCoarse == null ) {
	        Toast.makeText(getApplicationContext(), "No hay forma de posicionarse", Toast.LENGTH_SHORT).show();
			return;
		}		
        locationManager.requestLocationUpdates(providerCoarse, 10, 0, (LocationListener) this);

        // Usar �ltima ubicaci�n conocida de red para empezar y recibir futuras actualizaciones
        lBest=locationManager.getLastKnownLocation(providerCoarse);
     
    	// Guardar el inicio de b�squeda de ubicaci�n para no pasarse de tiempo
    	//tIni=new Date().getTime();
        tIni=System.currentTimeMillis();

        // Crear listeners para mostrar estaci�n en mapa, o abrir men� con clic largo
        ListView lv=(ListView) findViewById(R.id.listado);
        lv.setOnItemClickListener((OnItemClickListener) this);
        lv.setOnItemLongClickListener((OnItemLongClickListener) this);
    }

	/**
	 * Carga la versi�n est�tica de la lista de estaciones para acelerar el arranque
	 * Puede ser la �ltima descargada, o la que viene en el APK si no hay ninguna descargada 
	 */
	private void leerCacheEstaciones() {
		estaciones=new ArrayList<Estacion>();
        JSONArray js=null;
        String s="";
        
        // Se busca primero una copia previa        
        try {
        	BufferedReader fis;
			File f=new File(getFilesDir(),"estaciones.json");
			if (f.exists()) {
//	        	Toast.makeText(getApplicationContext(), "Leyendo estaciones cacheadas", Toast.LENGTH_SHORT).show();
				
				fis=new BufferedReader(new FileReader(f));			
				s=fis.readLine();
				fis.close();				
				js=new JSONArray(s);
				// Si no hay datos dar error aunque sea un JSON v�lido
				if (js.length()==0) {
//		        	Toast.makeText(getApplicationContext(), "JSON ok, longitud 0", Toast.LENGTH_SHORT).show();
					js=null;
				}
			}
		} catch (IOException e) {
			js=null;
		} 
		catch (JSONException e) {
			// Si no es un JSON v�lido anular
//        	Toast.makeText(getApplicationContext(), "JSON no v�lido", Toast.LENGTH_SHORT).show();
			js=null;
		}
			
		// Si el fichero cacheado no era v�lido, leer la versi�n est�tica que incluye el APK
		// Esta tiene que ir bien s� o s�
		if (js==null) {
//        	Toast.makeText(getApplicationContext(), "Leyendo estaciones raw", Toast.LENGTH_SHORT).show();
			try {
				BufferedReader fis=new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.estaciones)));
				s=fis.readLine();
				fis.close();
				js=new JSONArray(s);
			} catch (IOException ie) {}
			catch (JSONException jse) {}			
		}
		
		// Una vez le�do el JSON, procesarlo
		estaciones=leerFicheroEstaciones(js);
		// Poner n� de bicis/anclajes a desconocido
		for(Estacion e:estaciones) {
			e.setAnclajesLibres(-1);
			e.setBicisLibres(-1);
		}
	}

	/**
	 * Activa la b�squeda de ubicaci�n usando el mejor m�todo disponible
	 */
	private void activarUbicacion() {
		locationManager.removeUpdates(this);
		// Comprobar si se ha activado o no el GPS, y decidir el m�todo para ubicarse
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	mUbic=LocationManager.GPS_PROVIDER;
        else {
//        	Toast.makeText(getApplicationContext(), R.string.avisonogps, Toast.LENGTH_LONG).show();
        	mUbic=providerCoarse;
        }
        locationManager.requestLocationUpdates(mUbic, 10, 0, (LocationListener) this);
	}
        
    /**
     *	Actualiza el listado de estaciones 
     */
    public void actualizarListado() {
    	// Si no se han descargado las estaciones y hay ubicaci�n disponible, no hacer nada
    	if ((estaciones.size()==0) || (lBest==null))
    		return;
    	
    	// Ocultar el di�logo de b�squeda de ubicaci�n si se estaba visualizando
    	if (dRecuperaEst.isShowing())
    		dRecuperaEst.dismiss();

    	// Mirar si est� activa la opci�n de ocultar estaciones vac�as
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ocultarVacios=sharedPrefs.getBoolean("ocultarVaciosPref", false);

        // Calcular distancias desde la ubicaci�n actual hasta cada estaci�n, generando
		// un objeto Resultado
        ArrayList<ResultadoBusqueda> result=new ArrayList<ResultadoBusqueda>();
        Iterator<Estacion> i=estaciones.iterator();
        while (i.hasNext()) {
        	Estacion e=(Estacion) i.next();
        	Location aux=e.getLoc();
        	Double dist=Double.valueOf(lBest.distanceTo(aux));
        	if (!(ocultarVacios && (e.getBicisLibres()<=0)))
        		result.add(new ResultadoBusqueda(e,dist));
        }	        
        	       	        
        // Ordenar por distancia
        Collections.sort(result);	        

        // Mostrar
        ListView l=(ListView) this.findViewById(R.id.listado);
        l.setAdapter(new ResultadoAdapter(this,result));	    	
    }
    
    // Cuando llega una nueva ubicaci�n mejor que la actual, reordenamos el listado
    public void onLocationChanged(Location location) {
		// S�lo hacer algo si la nueva ubicaci�n es mejor que la actual
    	if (isBetterLocation(location, lBest)) {
	    		
//          Toast.makeText(getApplicationContext(), "Ubicaci�n encontrada", Toast.LENGTH_SHORT).show();
          	dRecuperaEst.setMessage(getString(R.string.recuperandolista));
			// Actualizar precisi�n
	    	TextView pre=(TextView) this.findViewById(R.id.precisionNum);
	    	if (location.hasAccuracy())
	    		pre.setText(String.format("%.0f m",location.getAccuracy()));
	    	else
	    		pre.setText("Desconocida");
	    	
	    	lBest=location;
	        
    		actualizarListado();
        
		} else {
	    	//Toast.makeText(getApplicationContext(), "Ignorando ubicaci�n chunga", Toast.LENGTH_SHORT).show();
		}
    	// Si estamos en red y est� el GPS activado, pasar a GPS
    	// TODO: En API 9 se puede recibir un evento cuando se active el GPS. Investigar si se puede hacer con los modernos sin perder compatibilidad con API 8
    	if ((mUbic.equals(providerCoarse)) && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
    		activarUbicacion();
    }
    
    /** Determines whether one Location reading is better than the current Location fix
     *  (EXTRA�DO DEL SDK, MODIFICADO PARA ADAPTARLO A UBICACI�N R�PIDA)
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


   // Si el GPS deja de funcionar, pasar a modo red  
   public void onStatusChanged(String provider, int status, Bundle extras) {
       Toast.makeText(getApplicationContext(), "Cambio estado GPS", Toast.LENGTH_SHORT).show();
    	if (provider.equals(LocationManager.GPS_PROVIDER)) {
    		if (status!=LocationProvider.AVAILABLE)
    			activarUbicacion();
    	}    	    	
    }

	public void onProviderEnabled(String provider) {}
    
	public void onProviderDisabled(String provider) {}

	// Dejamos de buscar ubicaci�n al salir y restauramos el wifi
    @Override
    public void onPause() {
    	if (locationManager!=null)
    		locationManager.removeUpdates(this);
    	// Si habia descargas en curso, pararlas
    	if (descargaEstaciones!=null) {
    		descargaEstaciones.cancel(true);
    		descargaEstaciones=null;
    	}
    	
		WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
		wm.setWifiEnabled(this.estatWifi);
    	
    	super.onPause();
    }
    
    // Reactivar wifi si es necesario
    public void onResume() {
    	// Reactivar suscripci�n a ubicaciones
    	activarUbicacion();

        // Reactivar wifi si es necesario
    	if (prefs.getBoolean("activarWifiPref", false)) {
    		WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
    		wm.setWifiEnabled(true);
    	}
    	
    	super.onResume();
    }
    
    protected void OnStop() {
    	super.onStop();    	
    }

	public void onDismiss(DialogInterface arg0) {
//		Toast.makeText(getApplicationContext(), "Fin de b�squeda de ubicaci�n", Toast.LENGTH_SHORT).show();
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
	    case R.id.actualizar:
	        Toast.makeText(getApplicationContext(), getString(R.string.recuperandolista), Toast.LENGTH_SHORT).show();
	        activarUbicacion();
	        descargaEstaciones=new RecuperarEstacionesTask(this);
	        descargaEstaciones.execute();
	    	return true;
	    case R.id.estado:    	
	    	mostrarEstadisticas();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * 
	 */
	private void mostrarEstadisticas() {
		int bTot=0;
		int bAver=0;
		int aAver=0;
		int bLib=0;
		int aLib=0;
		int noBicis=0;
		
		for(Estacion e:estaciones) {
			bTot+=e.getAnclajesAveriados()+e.getAnclajesLibres()+e.getAnclajesUsados();
			bAver+=e.getBicisAveriadas();
			aAver+=e.getAnclajesAveriados();
			bLib+=e.getBicisLibres();
			aLib+=e.getAnclajesLibres();
			if (e.getBicisLibres()==0)
				noBicis++;
		}
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		// Si el n� de bicis es negativo, es q a�n no se han descargado los datos
		if (bLib<0) {
			builder.setMessage(R.string.sinDescargaTodavia)
				   .setCancelable(true)
				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				});
			AlertDialog alert=builder.create();
			alert.show();
		}
		else {
			String mensBicis=String.format(getString(R.string.estadisticasBicis),					
					bLib,
					bAver
					);
			String mensAnclajes=String.format(getString(R.string.estadisticasAnclajes), 
					bTot,
					bTot-(aLib+aAver),
					100*(1-aLib/(0.0+bTot-aAver)),
					aLib,
					100*aLib/(0.0+bTot-aAver),
					aAver,
					100*aAver/Double.valueOf(bTot)
					);
			
			String mensVacias=String.format(getString(R.string.estadisticasVacias), noBicis,estaciones.size());
			
			builder.setMessage(mensBicis+mensAnclajes+mensVacias)
				   .setTitle(R.string.estadoservicio)
				   .setCancelable(true)
				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				});
			AlertDialog alert=builder.create();
			alert.show();
		}
	}	
	// Clase privada para recuperar la lista de estaciones en segundo plano
	private class RecuperarEstacionesTask extends AsyncTask<Void, Void, ArrayList<Estacion>> {

		Context c;
		
	    public RecuperarEstacionesTask(Context c) {
	    	this.c=c;
	    }
	    
	    @Override
		protected void onPreExecute() {
	    	int mensaje;
	    	if (lBest==null)
	    		mensaje=R.string.buscandoubicaylista;
	    	else
	    		mensaje=R.string.recuperandolista;
    		dRecuperaEst = ProgressDialog.show(c, "", getString(mensaje),true,true);
    		// Si ya tenemos las estaciones cacheadas, cambiar el texto
	    	if (estaciones.size()>0) {
	    		if (lBest==null)
	    			dRecuperaEst.setMessage(getText(R.string.buscandoubica));
	    		else
	    			dRecuperaEst.dismiss();
	    	}
	    	
	    	ProgressBar pb=(ProgressBar) findViewById(R.id.progreso);
	    	pb.setIndeterminate(true);
	    	pb.setVisibility(View.VISIBLE);
	    }
		
	    // Cuando acabe de descargar, activar la b�squeda de ubicaci�n 
	    protected void onPostExecute(ArrayList<Estacion> result) {
//	          Toast.makeText(getApplicationContext(), "Descargadas estaciones", Toast.LENGTH_SHORT).show();
	    	// Cerrar di�logo y guardar resultados
	    	if (result==null) {
	    		AlertDialog.Builder builder=new AlertDialog.Builder(c);
				builder.setMessage(R.string.errorconexion)
				   .setCancelable(true)
				   .setTitle(R.string.error)
				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				});
			AlertDialog alert=builder.create();
			alert.show();
	    	}
	    	else {
	    		estaciones=result;
	    		actualizarListado();
	    	}
	    	
	    	if (lBest==null)
	    		dRecuperaEst.setMessage(getString(R.string.buscandoubica));
	    	else
	    		dRecuperaEst.dismiss();
	    	
	    	ProgressBar pb=(ProgressBar) findViewById(R.id.progreso);
	    	pb.setVisibility(View.INVISIBLE);

	    }

		@Override
		protected ArrayList<Estacion> doInBackground(Void... arg0) {
	    	JSONArray json=BicipalmaJsonClient.connect("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
	    	
	    	if (json.length()>0)
	    		return leerFicheroEstaciones(json);
	    	else {	    		
	    		return null;
	    	}
		}
	 }
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		  if (key.equals("ocultarVaciosPref"))
			  actualizarListado(); 		
		  
		  if (key.equals("activarWifiPref")) {
			  if (sharedPreferences.getBoolean("activarWifiPref", false)) {
				  WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
				  wm.setWifiEnabled(true);
			  }
		  }
	}

	/**
	 * @param json
	 * @return
	 * @throws FileNotFoundException 
	 */
	private ArrayList<Estacion> leerFicheroEstaciones(JSONArray json) {
		// Extraer estaciones del JSON
		ArrayList<Estacion> est=new ArrayList<Estacion>();
		for(int i=0;i<json.length();i++) {
			try {
				String nombre=json.getJSONObject(i).getString("alia");
				// Extraer el n� de estaci�n y eliminarlo del nombre
				int num=Integer.valueOf(nombre.substring(1,2));				
				nombre=nombre.substring(5);
				Location pos=new Location("network");
				pos.setLatitude(json.getJSONObject(i).getDouble("realLat"));
				pos.setLongitude(json.getJSONObject(i).getDouble("realLon"));
				Estacion e=new Estacion(nombre,pos);
				e.setNumEstacion(num);
				String html=json.getJSONObject(i).getString("paramsHtml");
				int pos2=html.indexOf("Bicis Libres:</span>")+"Bicis Libres:</span>".length();
				if (pos2>0)
					e.setBicisLibres(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setBicisLibres(0);
				pos2=html.indexOf("Bicis Averiadas:</span>")+"Bicis Averiadas:</span>".length();
				if (pos2>0)
					e.setBicisAveriadas(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setBicisAveriadas(0);
				pos2=html.indexOf("Anclajes Libres:</span>")+"Anclajes Libres:</span>".length();
				if (pos2>0)
					e.setAnclajesLibres(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setAnclajesLibres(0);
				pos2=html.indexOf("Anclajes Usados:</span>")+"Anclajes Usados:</span>".length();
				if (pos2>0)
					e.setAnclajesUsados(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setAnclajesUsados(0);
				pos2=html.indexOf("Anclajes Averiados:</span>")+"Anclajes Averiados:</span>".length();
				if (pos2>0)
					e.setAnclajesAveriados(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
				else
					e.setAnclajesAveriados(0);
				est.add(e);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Escribir el JSON a disco para acelerar futuros accesos
		try {
			if (est.size()>0) {
				FileOutputStream fos=openFileOutput("estaciones.json", Context.MODE_PRIVATE);
				fos.write(json.toString().getBytes());
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return est;
	}

	public void onItemClick(AdapterView<?> parent, View v,int position,long id) {
		ResultadoBusqueda rb=(ResultadoBusqueda) parent.getAdapter().getItem(position);
		// M�s r�pido en posicionar, pero no muestra pin
//		String uri="geo:"+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude();
/*		String uri="geo:0,0?q="+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude()+" ("+rb.getEstacion().getNombre()+")";
		startActivity(new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(uri)));*/
		
    	Intent i=new Intent(this,MapaActivity.class);
    	i.putExtra("estaciones",estaciones);
    	i.putExtra("latcentro", rb.getEstacion().getLoc().getLatitude());
    	i.putExtra("longcentro", rb.getEstacion().getLoc().getLongitude());
    	startActivity(i);
	}

	// Con clic largo abrimos Google Maps con la ruta desde la posici�n actual
	public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position,final long id) {
		final CharSequence[] items=getResources().getTextArray(R.array.menu_contextual);
		AlertDialog.Builder b=new AlertDialog.Builder(this);
		final ResultadoBusqueda rb=(ResultadoBusqueda) parent.getAdapter().getItem(position);		
		
		b.setItems(items, new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int item) {
    			dialog.dismiss();
    			switch (item) {
    			case 0:
    				onItemClick(parent, v, position, id);
    				break;
    			case 1:
    				//TODO: Pasar a GMaps la versi�n localizada de "Current location" para que haga �l la b�squeda de origen
    				String latlongactual=lBest.getLatitude()+","+lBest.getLongitude();
    				String latlongdestino=rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude();
    				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
    						Uri.parse("http://maps.google.com/maps?saddr="+latlongactual+"&daddr="+latlongdestino));
    				startActivity(intent);    				
    				break;
    			}
    	    }
    	});
		AlertDialog alert=b.create();
		alert.show();
		
		return true;
	}

	
}

