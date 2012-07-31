package com.jesjimher.bicipalma;

import android.os.Bundle;
import android.preference.PreferenceActivity;
//TODO: Forzar idioma para el que lo quiera en cat pero no tenga android en cat
public class PreferenciasActivity extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
