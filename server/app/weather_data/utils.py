def get_weather(weathercode: int) -> str:
    """
    Restituisce una descrizione leggibile del codice meteo Open-Meteo.
    """
    weather_dict = {
        0: "Sereno",
        1: "Poco nuvoloso",
        2: "Nuvoloso",
        3: "Coperto",
        45: "Nebbia",
        48: "Nebbia ghiacciata",
        51: "Pioggia leggera",
        53: "Pioggia moderata",
        55: "Pioggia intensa",
        56: "Pioggia ghiacciata leggera",
        57: "Pioggia ghiacciata intensa",
        61: "Pioggia debole",
        63: "Pioggia moderata",
        65: "Pioggia forte",
        66: "Pioggia ghiacciata debole",
        67: "Pioggia ghiacciata forte",
        71: "Neve leggera",
        73: "Neve moderata",
        75: "Neve intensa",
        77: "Grandine",
        80: "Rovesci di pioggia leggeri",
        81: "Rovesci di pioggia moderati",
        82: "Rovesci di pioggia forti",
        85: "Rovesci di neve leggeri",
        86: "Rovesci di neve forti",
        95: "Temporale leggero o moderato",
        96: "Temporale con grandine debole",
        99: "Temporale con grandine forte"
    }
    return weather_dict.get(weathercode, "Codice meteo sconosciuto")

def get_cities():
    """
    Restituisce una lista di città supportate con le loro coordinate.
    """
    return {
    "Venezia": (45.44, 12.33),
    "Verona": (45.43, 10.98),
    "Padova": (45.42, 11.87),
    "Vicenza": (45.55, 11.54),
    "Treviso": (45.67, 12.25),
    "Rovigo": (45.07, 11.79),
    "Chioggia": (45.23, 12.24),
    "Bassano del Grappa": (45.77, 11.72),
    "San Donà di Piave": (45.62, 12.69),
    "Belluno": (46.14, 12.22),
    "Schio": (45.73, 11.37),
    "Conegliano": (45.67, 12.34),
    "Castelfranco Veneto": (45.68, 11.93),
    "Montebelluna": (45.78, 12.04),
    "Oderzo": (45.88, 12.58),
    "Vittorio Veneto": (45.98, 12.32),
    "Villafranca di Verona": (45.41, 10.87),
    }
