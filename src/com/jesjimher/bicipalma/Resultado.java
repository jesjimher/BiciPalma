package com.jesjimher.bicipalma;

import android.location.Location;

/** Resultado de una búsqueda de estaciones
 *  Ordenable por distancia
 * @author u82066
 *
 */
public class Resultado implements Comparable {
	
	String nombre;
	Location loc;
	Double dist;
	
	public Resultado(String nombre,Location loc,Double dist) {
		this.nombre=nombre;
		this.loc=loc;
		this.dist=dist;		
	}

	/* Ordena según la distancia */
	public int compareTo(Object o) {
		Resultado r=(Resultado)o;
		if (r.getDist()>this.getDist()) {
			return -1;
		}
		else if (r.getDist()==this.getDist()) {
			return 0;
		}
		else return 1;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public Double getDist() {
		return dist;
	}

	public void setDist(Double dist) {
		this.dist = dist;
	}

	@Override
	public String toString() {
		return nombre;
	}

}
