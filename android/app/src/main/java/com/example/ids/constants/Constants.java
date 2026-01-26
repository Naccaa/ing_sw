package com.example.ids.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    // Indirizzo usato dalle chiamate al backend
    // Se non già presente aggiungere l'indirizzo ad android/app/src/main/res/xml/network_security_config.xml
    // public static String BASE_URL = "http://192.168.1.50:5000";
    //public static String BASE_URL = "http://192.168.178.104:5000";
    //public static String BASE_URL = "http://192.168.1.130:5000";
    public static String BASE_URL = "http://10.0.2.2:5000";

    public static final Map<String, double[]> CITIES;

    static {
        Map<String, double[]> map = new HashMap<>();

        map.put("Venezia", new double[]{12.33, 45.44});
        map.put("Verona", new double[]{10.98, 45.43});
        map.put("Padova", new double[]{11.87, 45.42});
        map.put("Vicenza", new double[]{11.54, 45.55});
        map.put("Treviso", new double[]{12.25, 45.67});
        map.put("Rovigo", new double[]{11.79, 45.07});
        map.put("Chioggia", new double[]{12.24, 45.23});
        map.put("Bassano del Grappa", new double[]{11.72, 45.77});
        map.put("San Donà di Piave", new double[]{12.69, 45.62});
        map.put("Belluno", new double[]{12.22, 46.14});
        map.put("Schio", new double[]{11.37, 45.73});
        map.put("Conegliano", new double[]{12.34, 45.67});
        map.put("Castelfranco Veneto", new double[]{11.93, 45.68});
        map.put("Montebelluna", new double[]{12.04, 45.78});
        map.put("Oderzo", new double[]{12.58, 45.88});
        map.put("Vittorio Veneto", new double[]{12.32, 45.98});
        map.put("Villafranca di Verona", new double[]{10.87, 45.41});

        // rende la mappa immutabile (best practice)
        CITIES = Collections.unmodifiableMap(map);
    }
}
