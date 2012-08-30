package com.jesjimher.bicipalma;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Estacion implements Parcelable {
	private static final long serialVersionUID = 3302402589493000251L;
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

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nombre);
		dest.writeInt(bicisLibres);
		dest.writeInt(bicisAveriadas);
		dest.writeInt(anclajesLibres);
		dest.writeInt(anclajesUsados);
		dest.writeInt(anclajesAveriados);
		dest.writeDouble(loc.getLatitude());
		dest.writeDouble(loc.getLongitude());	
		dest.writeInt(numEstacion);
	}
	public static final Parcelable.Creator<Estacion> CREATOR
    = new Parcelable.Creator<Estacion>() {
		public Estacion createFromParcel(Parcel in) {
		    return new Estacion(in);
		}
		
		public Estacion[] newArray(int size) {
		    return new Estacion[size];
		}
	};
	
	private Estacion(Parcel in) {
		nombre=in.readString();
		bicisLibres= in.readInt();
		bicisAveriadas= in.readInt();
		anclajesLibres= in.readInt();
		anclajesUsados= in.readInt();
		anclajesAveriados= in.readInt();
		loc=new Location("network");
		loc.setLatitude(in.readDouble());
		loc.setLongitude(in.readDouble());	
		numEstacion=in.readInt();
	}	
}
