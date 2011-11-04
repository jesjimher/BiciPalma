package com.jesjimher.bicipalma;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResultadoAdapter extends BaseAdapter {
	private ArrayList<ResultadoBusqueda> listado;
	private LayoutInflater mInflater;

	public ResultadoAdapter(Context c,ArrayList<ResultadoBusqueda> a) {
		listado=a;
		 mInflater = LayoutInflater.from(c);
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
			holder.txtNombre = (TextView) convertView.findViewById(R.id.nombre);
			holder.txtDistancia= (TextView) convertView.findViewById(R.id.distancia);
			
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
  
		holder.txtNombre.setText(listado.get(position).getNombre());
		holder.txtDistancia.setText(String.format("%.2f km",listado.get(position).getDist()/1000));

		return convertView;	
	}
	static class ViewHolder {
		TextView txtNombre;
		TextView txtDistancia;
	}
}
