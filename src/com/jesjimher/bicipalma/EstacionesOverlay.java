package com.jesjimher.bicipalma;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
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
    	OverlayItem overlayitem = new OverlayItem(p, e.getNombre(), "Libres: "+e.getBicisLibres());
    	Drawable d=mContext.getResources().getDrawable(R.drawable.buscabici);
    	overlayitem.setMarker(boundCenterBottom(d));
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
}
