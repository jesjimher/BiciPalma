package com.jesjimher.bicipalma;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class EstacionesOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	
	public EstacionesOverlay(Drawable arg0) {
		super(boundCenterBottom(arg0));
	}

	public EstacionesOverlay(Drawable defaultMarker, Context context) {
	  super(boundCenterBottom(defaultMarker));
	  mContext = context;
	}	

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void addEstacion(Estacion e,GeoPoint p) {		
		String mensaje=String.format("%s: %d, %s: %d", mContext.getResources().getString(R.string.lbicislibres),e.getBicisLibres(),mContext.getResources().getString(R.string.lanclajeslibres),e.getAnclajesLibres());
    	OverlayItem overlayitem = new OverlayItem(p, e.getNombre(), mensaje);
    	Drawable d=mContext.getResources().getDrawable(R.drawable.estado_variable);
    	d.setLevel(e.getBicisLibres()+1);
    	int w=d.getIntrinsicWidth()/2;
    	int h=d.getIntrinsicHeight()/2;
    	d.setBounds(-w/2,-h/2,w/2,h/2);
    	overlayitem.setMarker(d);
	    mOverlays.add(overlayitem);
	    populate();
	}	
	
	public void finEstaciones() {
		populate();		
	}

	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
	
	// Eliminamos la sombra de los marcadores
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}
}
