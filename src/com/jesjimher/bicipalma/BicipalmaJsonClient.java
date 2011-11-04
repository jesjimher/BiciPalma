package com.jesjimher.bicipalma;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

public class BicipalmaJsonClient {

	// Se conecta a una URL para obtener un objeto JSON
	public static JSONObject connect(String url) {
		
		JSONObject json=new JSONObject();
		
		// YA RECUPERA JSON, PERO FALLA LA SINTAXIS
		// HAY ALGÚN PROBLEMA CON LA CODIFICACION DE CARACTERES O ALGO, PORQUE HACIENDO COPYPASTE ES VALIDO EN JSONLINT
		try {
			// Primero conectar a la URL base para capturar el id de sesión
			HttpGet hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/Control?act=mp");
			HttpClient hcli = new DefaultHttpClient();
			HttpResponse resp=hcli.execute(hg);
			String cookie=resp.getFirstHeader("Set-Cookie").getValue().split(";")[0];
			hg=new HttpGet("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
			hg.setHeader("Referer","http://83.36.51.60:8080/eTraffic3/Control?act=mp");
			hg.addHeader("Cookie", cookie);
			resp=hcli.execute(hg);
			HttpEntity he=resp.getEntity();
			if (he != null) {			 
				// A Simple JSON Response Read
				InputStream instream = he.getContent();
				String result= convertStreamToString(instream);
				System.out.println(result);
				json=new JSONObject(result);			
				instream.close();
			}		 
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;		
	}

	// Convierte un InputStream a una cadena
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
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
