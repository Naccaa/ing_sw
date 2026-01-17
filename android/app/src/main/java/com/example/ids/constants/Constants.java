package com.example.ids.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    // Indirizzo usato dalle chiamate al backend
    // Se non già presente aggiungere l'indirizzo ad android/app/src/main/res/xml/network_security_config.xml
    // public static String BASE_URL = "http://192.168.1.50:5000";
    //public static String BASE_URL = "http://192.168.178.104:5000";
    public static String BASE_URL = "http://10.0.2.2:5000";

    public static final Map<String, double[]> CITIES;

    static {
        Map<String, double[]> map = new HashMap<>();

        map.put("Venezia", new double[]{45.44, 12.33});
        map.put("Verona", new double[]{45.43, 10.98});
        map.put("Padova", new double[]{45.42, 11.87});
        map.put("Vicenza", new double[]{45.55, 11.54});
        map.put("Treviso", new double[]{45.67, 12.25});
        map.put("Rovigo", new double[]{45.07, 11.79});
        map.put("Chioggia", new double[]{45.23, 12.24});
        map.put("Bassano del Grappa", new double[]{45.77, 11.72});
        map.put("San Donà di Piave", new double[]{45.62, 12.69});
        map.put("Belluno", new double[]{46.14, 12.22});
        map.put("Schio", new double[]{45.73, 11.37});
        map.put("Conegliano", new double[]{45.67, 12.34});
        map.put("Castelfranco Veneto", new double[]{45.68, 11.93});
        map.put("Montebelluna", new double[]{45.78, 12.04});
        map.put("Oderzo", new double[]{45.88, 12.58});
        map.put("Vittorio Veneto", new double[]{45.98, 12.32});
        map.put("Villafranca di Verona", new double[]{45.41, 10.87});

        // rende la mappa immutabile (best practice)
        CITIES = Collections.unmodifiableMap(map);
    }
}
