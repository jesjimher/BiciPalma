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
