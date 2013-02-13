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

import java.util.ArrayList;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends FragmentActivity {

	ArrayList<Estacion> estaciones;
	private static final LatLng PZAESPANYA = new LatLng(39.573793,2.64065);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

        // Activar zoom
        GoogleMap mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment)).getMap();
//        mapa.setBuiltInZoomControls(true);
        mapa.setMyLocationEnabled(true);

        // Si el mapa no está en Palma o similar, ponerlo en pza españa
        CameraPosition c=mapa.getCameraPosition();
        Location actual=new Location("");
        actual.setLatitude(c.target.latitude);
        actual.setLongitude(c.target.longitude);
        Location pe=new Location("");
        pe.setLatitude(PZAESPANYA.latitude);
        pe.setLongitude(PZAESPANYA.longitude);
        if (actual.distanceTo(pe)>=5000)
	        mapa.moveCamera(CameraUpdateFactory.newLatLng(PZAESPANYA));
        	
        Intent i=getIntent();
//        GeoPoint point=null;
        if (i.hasExtra("estaciones")) {
        	estaciones=i.getExtras().getParcelableArrayList("estaciones");
        
	        for (Estacion e:estaciones) {
	        	LevelListDrawable d=(LevelListDrawable) getResources().getDrawable(R.drawable.estado_variable);
	        	d.setLevel(e.getBicisLibres()+1);
	        	BitmapDrawable bd=(BitmapDrawable) d.getCurrent();
	        	Bitmap b=bd.getBitmap();
	        	Bitmap petit=Bitmap.createScaledBitmap(b, b.getWidth()/2,b.getHeight()/2, false);
	        	String mensaje=String.format("%s: %d, %s: %d", getResources().getString(R.string.lbicislibres),e.getBicisLibres(),getResources().getString(R.string.lanclajeslibres),e.getAnclajesLibres());	        	
	        	mapa.addMarker(new MarkerOptions()
	        					.position(new LatLng(e.getLoc().getLatitude(),e.getLoc().getLongitude()))
	        					.title(e.getNombre())
	        					.snippet(mensaje)
	        					.icon(BitmapDescriptorFactory.fromBitmap(petit))
	        	);
	        }
	        Double lat=i.getExtras().getDouble("latcentro");
	        Double lon=i.getExtras().getDouble("longcentro");
	        mapa.moveCamera(CameraUpdateFactory.zoomTo(16));
	        mapa.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.mapa, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

/*	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}*/

}
