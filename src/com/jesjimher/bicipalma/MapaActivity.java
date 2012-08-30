package com.jesjimher.bicipalma;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapaActivity extends MapActivity {

	ArrayList<Estacion> estaciones;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

        // Activar zoom
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        MapController mc=mapView.getController();
        mc.setZoom(17);     

        Intent i=getIntent();
        GeoPoint point=null;
        if (i.hasExtra("estaciones")) {
        	estaciones=i.getExtras().getParcelableArrayList("estaciones");
        
	        List<Overlay> mapOverlays = mapView.getOverlays();
	        Drawable drawable = this.getResources().getDrawable(R.drawable.buscabici);
	        EstacionesOverlay estoverlay = new EstacionesOverlay(drawable, this);

	        for (Estacion e:estaciones) {
	        	point = new GeoPoint((int)(e.getLoc().getLatitude()*1E6),(int) (e.getLoc().getLongitude()*1E6));
//	        	OverlayItem overlayitem = new OverlayItem(point, e.getNombre(), "Libres: "+e.getBicisLibres());
	        	estoverlay.addEstacion(e,point);
	        }
	        mapOverlays.add(estoverlay);	        	
	        
	        int lat=(int) (i.getExtras().getDouble("latcentro")*1E6);
	        int lon=(int) (i.getExtras().getDouble("longcentro")*1E6);
	        GeoPoint gp=new GeoPoint(lat,lon);	        
	        mc.animateTo(gp);
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

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
