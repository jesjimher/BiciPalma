/* Copyright 2012 Jes�s Jim�nez Herranz
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

public class BicipalmaJsonClient {

	// Se conecta a una URL para obtener un objeto JSON
	public static JSONArray connect(String url) {
		
		JSONArray json=new JSONArray();

		try {
			// Primero conectar a la URL base para capturar el id de sesi�n
			HttpGet hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/Control?act=mp");
			HttpParams httpParameters = new BasicHttpParams();
			// Poner los timeouts apropiadamente
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 7000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			DefaultHttpClient hcli = new DefaultHttpClient(httpParameters);			
			HttpResponse resp=hcli.execute(hg);
			if (resp.getStatusLine().getStatusCode()==200) {
				// Con el JSESSIONID, conectar a la URL que recupera el JSON
				String cookie=resp.getFirstHeader("Set-Cookie").getValue().split(";")[0];
				resp.getEntity().consumeContent();
				hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
				hg.setHeader("Referer","http://83.36.51.60:8080/eTraffic3/Control?act=mp");
				hg.addHeader("Cookie", cookie);
				resp=hcli.execute(hg);
				if (resp.getStatusLine().getStatusCode()==200) {
					HttpEntity he=resp.getEntity();
					if (he != null) {			 
						// A Simple JSON Response Read
						InputStream instream = he.getContent();
						// Averiguar el encoding
						String enc=he.getContentType().getValue();
						enc=enc.substring(enc.indexOf("charset=")+8);
						if (enc.length()<=0) enc="ISO-8859-1";
						String result= convertStreamToString(instream,enc);
						
						json=new JSONArray(result);			
						instream.close();						
					}		 
					resp.getEntity().consumeContent();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;		
	}

	// Convierte un InputStream a una cadena
	public static String convertStreamToString(InputStream is,String encoding) {
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,encoding));
		} catch (UnsupportedEncodingException e1) {
			// Si el encoding proporcionado no es v�lido, usar el encoding por defecto
			reader = new BufferedReader(new InputStreamReader(is));
		}
		StringBuilder sb = new StringBuilder();
		 
		String line = null;
		try {
			while ((line = reader.readLine()) != null) 
				sb.append(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}	
}
