package com.jesjimher.bicipalma;

import android.location.Location;

/** Resultado de una búsqueda de estaciones
 *  Ordenable por distancia
 * @author u82066
 *
 */
public class ResultadoBusqueda implements Comparable {
	
	String nombre;
	Location loc;
	Double dist;
	Long anclajesLibres,anclajesOcupados,anclajesAveriados;
	Long bicisLibres,bicisAveriadas;
	
	public ResultadoBusqueda(String nombre,Location loc,Double dist) {
		this.nombre=nombre;
		this.loc=loc;
		this.dist=dist;		
	}

	/* Ordena según la distancia */
	public int compareTo(Object o) {
		ResultadoBusqueda r=(ResultadoBusqueda)o;
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

	public Long getAnclajesLibres() {
		return anclajesLibres;
	}

	public void setAnclajesLibres(Long anclajesLibres) {
		this.anclajesLibres = anclajesLibres;
	}

	public Long getAnclajesOcupados() {
		return anclajesOcupados;
	}

	public void setAnclajesOcupados(Long anclajesOcupados) {
		this.anclajesOcupados = anclajesOcupados;
	}

	public Long getAnclajesAveriados() {
		return anclajesAveriados;
	}

	public void setAnclajesAveriados(Long anclajesAveriados) {
		this.anclajesAveriados = anclajesAveriados;
	}

	public Long getBicisLibres() {
		return bicisLibres;
	}

	public void setBicisLibres(Long bicisLibres) {
		this.bicisLibres = bicisLibres;
	}

	public Long getBicisAveriadas() {
		return bicisAveriadas;
	}

	public void setBicisAveriadas(Long bicisAveriadas) {
		this.bicisAveriadas = bicisAveriadas;
	}

}
