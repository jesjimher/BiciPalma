/* Copyright 2012 Jesús Jiménez Herranz
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
