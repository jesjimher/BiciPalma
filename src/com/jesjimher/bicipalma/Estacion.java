package com.jesjimher.bicipalma;

import android.location.Location;

public class Estacion {
	String nombre;
	Location loc;
	
	int numEstacion;
	int bicisLibres;
	int bicisAveriadas;
	int anclajesLibres;
	int anclajesUsados;
	int anclajesAveriados;
	
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

	public int getBicisAveriadas() {
		return bicisAveriadas;
	}

	public void setBicisAveriadas(int bicisAveriadas) {
		this.bicisAveriadas = bicisAveriadas;
	}

	public int getAnclajesUsados() {
		return anclajesUsados;
	}

	public void setAnclajesUsados(int anclajesUsados) {
		this.anclajesUsados = anclajesUsados;
	}

	public int getAnclajesAveriados() {
		return anclajesAveriados;
	}

	public void setAnclajesAveriados(int anclajesAveriados) {
		this.anclajesAveriados = anclajesAveriados;
	}

	public int getNumEstacion() {
		return numEstacion;
	}

	public void setNumEstacion(int numEstacion) {
		this.numEstacion = numEstacion;
	}
}
