package com.jesjimher.bicipalma;

import android.location.Location;

public class Estacion {
	String nombre;
	Location loc;
	
	int bicisLibres;
	int anclajesLibres;
	
	public Estacion(String nombre,Location loc) {
		this.nombre=nombre;
		this.loc=loc;
	}
	
	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getBicisLibres() {
		return bicisLibres;
	}
	public void setBicisLibres(int bicisLibres) {
		this.bicisLibres = bicisLibres;
	}
	public int getAnclajesLibres() {
		return anclajesLibres;
	}
	public void setAnclajesLibres(int anclajesLibres) {
		this.anclajesLibres = anclajesLibres;
	}
}
