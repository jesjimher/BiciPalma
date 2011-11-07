package com.jesjimher.bicipalma;

import android.location.Location;

public class Estacion {
	String nombre;
	Location loc;
	
	Long bicisLibres,anclajesLibres;
	
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

	public Long getBicisLibres() {
		return bicisLibres;
	}
	public void setBicisLibres(Long bicisLibres) {
		this.bicisLibres = bicisLibres;
	}
	public Long getAnclajesLibres() {
		return anclajesLibres;
	}
	public void setAnclajesLibres(Long anclajesLibres) {
		this.anclajesLibres = anclajesLibres;
	}
}
