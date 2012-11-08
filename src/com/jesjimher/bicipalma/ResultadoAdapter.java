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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultadoAdapter extends BaseAdapter {
	private ArrayList<ResultadoBusqueda> listado;
	private LayoutInflater mInflater;
	Context c;

	public ResultadoAdapter(Context c,ArrayList<ResultadoBusqueda> a) {
		listado=a;
		mInflater = LayoutInflater.from(c);
		this.c=c;
	}
	
	public int getCount() {
		return listado.size();
	}

	public Object getItem(int pos) {
		return listado.get(pos);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new ViewHolder();
			holder.imgIcono= (ImageView) convertView.findViewById(R.id.iconoEstacion);
			holder.txtNombre = (TextView) convertView.findViewById(R.id.nombre);
			holder.txtDistancia= (TextView) convertView.findViewById(R.id.distancia);
			holder.txtBicisLibres= (TextView) convertView.findViewById(R.id.bicislibres);
			holder.txtAnclajesLibres= (TextView) convertView.findViewById(R.id.anclajeslibres);
			
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
  
		//TODO: Retocar iconos para que el fondo sea mucho más claro, quedan muy feotes
		holder.txtNombre.setText(listado.get(position).getEstacion().getNombre());
		holder.txtDistancia.setText(String.format("%.2f km",listado.get(position).getDist()/1000));
		int bLibres=listado.get(position).getEstacion().getBicisLibres();
		if (bLibres>=0) {
			holder.txtBicisLibres.setText(String.format("%s: %d",this.c.getString(R.string.lbicislibres),bLibres));
			holder.imgIcono.getDrawable().setLevel(bLibres+1);
		}
		else {
			holder.txtBicisLibres.setText(String.format("%s: ?",this.c.getString(R.string.lbicislibres)));
			holder.imgIcono.getDrawable().setLevel(0);			
		}
		if (listado.get(position).getEstacion().getBicisLibres()==0) 
			holder.txtBicisLibres.setTextColor(android.graphics.Color.RED);
		else
			holder.txtBicisLibres.setTextColor(holder.txtNombre.getCurrentTextColor());
			
		int aLibres=listado.get(position).getEstacion().getAnclajesLibres();
		if (aLibres>=0)
			holder.txtAnclajesLibres.setText(String.format("%s: %d",this.c.getString(R.string.lanclajeslibres),aLibres));
		else
			holder.txtAnclajesLibres.setText(String.format("%s: ?",this.c.getString(R.string.lanclajeslibres)));
		if (listado.get(position).getEstacion().getAnclajesLibres()==0) 
			holder.txtAnclajesLibres.setTextColor(android.graphics.Color.RED);
		else
			holder.txtAnclajesLibres.setTextColor(holder.txtNombre.getCurrentTextColor());

		return convertView;	
	}
	static class ViewHolder {
		ImageView imgIcono;
		TextView txtNombre;
		TextView txtDistancia;
		TextView txtBicisLibres;
		TextView txtAnclajesLibres;
	}
}
