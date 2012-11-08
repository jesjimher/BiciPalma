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

/** Resultado de una búsqueda de estaciones
 *  Ordenable por distancia
 * @author u82066
 *
 */
public class ResultadoBusqueda implements Comparable {
	
	Estacion e;
	Double dist;
	
	public ResultadoBusqueda(Estacion e,Double dist) {
		this.e=e;
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



	public Double getDist() {
		return dist;
	}

	public void setDist(Double dist) {
		this.dist = dist;
	}

	@Override
	public String toString() {
		return e.getNombre();
	}

	public Estacion getEstacion() {
		return e;
	}

	public void setEstacion(Estacion e) {
		this.e = e;
	}



}
