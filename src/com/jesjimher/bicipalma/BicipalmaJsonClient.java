package com.jesjimher.bicipalma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

public class BicipalmaJsonClient {

	// Se conecta a una URL para obtener un objeto JSON
	public static JSONArray connect(String url) {
		
		JSONArray json=new JSONArray();
		
		try {
			// Primero conectar a la URL base para capturar el id de sesión
			HttpGet hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/Control?act=mp");
			HttpClient hcli = new DefaultHttpClient();
			HttpResponse resp=hcli.execute(hg);
			// Con el JSESSIONID, conectar a la URL que recupera el JSON
			String cookie=resp.getFirstHeader("Set-Cookie").getValue().split(";")[0];
			hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
			hg.setHeader("Referer","http://83.36.51.60:8080/eTraffic3/Control?act=mp");
			hg.addHeader("Cookie", cookie);
			resp=hcli.execute(hg);
			HttpEntity he=resp.getEntity();
			if (he != null) {			 
				// A Simple JSON Response Read
				InputStream instream = he.getContent();
				// Averiguar el encoding
				String enc=he.getContentType().getValue();
				enc=enc.substring(enc.indexOf("charset=")+8);
				if (enc.length()<=0) enc="ISO-8859-1";
				String result= convertStreamToString(instream,enc);
/*				Log.i("PRUEBA",result);
				String aa=new String(result.getBytes(),"ISO-8859-1");
				Log.d("PRUEBA",aa);*/
				
				json=new JSONArray(result);			
				instream.close();
			}		 
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
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
			// Si el encoding proporcionado no es válido, usar el encoding por defecto
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
