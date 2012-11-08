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
