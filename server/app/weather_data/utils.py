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
    Restituisce una lista di città supportate con le loro coordinate invertite.
    Formato: "NomeCittà": (Longitudine, Latitudine)
    """
    return {
        "Venezia": (12.33, 45.44),
        "Verona": (10.98, 45.43),
        "Padova": (11.87, 45.42),
        "Vicenza": (11.54, 45.55),
        "Treviso": (12.25, 45.67),
        "Rovigo": (11.79, 45.07),
        "Chioggia": (12.24, 45.23),
        "Bassano del Grappa": (11.72, 45.77),
        "San Donà di Piave": (12.69, 45.62),
        "Belluno": (12.22, 46.14),
        "Schio": (11.37, 45.73),
        "Conegliano": (12.34, 45.67),
        "Castelfranco Veneto": (11.93, 45.68),
        "Montebelluna": (12.04, 45.78),
        "Oderzo": (12.58, 45.88),
        "Vittorio Veneto": (12.32, 45.98),
        "Villafranca di Verona": (10.87, 45.41),
    }
