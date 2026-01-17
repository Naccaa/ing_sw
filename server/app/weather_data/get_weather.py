from datetime import datetime, timedelta
import requests
from weather_data.utils import get_weather, get_cities
from apscheduler.schedulers.background import BackgroundScheduler

API_URL = "https://api.open-meteo.com/v1/forecast"
POLL_INTERVAL = 60 

CITIES = get_cities()
_scheduler_started = False

# ----------------------------
# Fetch meteo
# ----------------------------
def fetch_weather(lat, lon):
    params = {
        "latitude": lat,
        "longitude": lon,
        "hourly": "temperature_2m,precipitation,windspeed_10m,pressure_msl,weathercode",
        "timezone": "Europe/Rome"
    }
    response = requests.get(API_URL, params=params, timeout=10)
    response.raise_for_status()
    return response.json()

# ----------------------------
# Funzioni allerte
# ----------------------------
def check_heavy_rain(data, threshold_mm=1):
    rain_now = data["hourly"]["precipitation"][0]
    rain_30 = data["hourly"]["precipitation"][1]
    return rain_now >= threshold_mm or rain_30 >= threshold_mm

def check_flood(data, threshold_mm=25, code_threshold=95):
    rain_now = data["hourly"]["precipitation"][0]
    rain_30 = data["hourly"]["precipitation"][1]
    code_now = data["hourly"]["weathercode"][0]
    code_30 = data["hourly"]["weathercode"][1]
    return rain_now >= threshold_mm or rain_30 >= threshold_mm or code_now >= code_threshold or code_30 >= code_threshold

def check_hail(data):
    hail_codes = [77, 96, 99]
    code_now = data["hourly"]["weathercode"][0]
    code_30 = data["hourly"]["weathercode"][1]
    return code_now in hail_codes or code_30 in hail_codes

def check_wind(data):
    wind_now = data["hourly"]["windspeed_10m"][0]
    wind_30 = data["hourly"]["windspeed_10m"][1]
    code_now = data["hourly"]["weathercode"][0]
    code_30 = data["hourly"]["weathercode"][1]
    return (code_now >= 95 and wind_now >= 90) or (code_30 >= 95 and wind_30 >= 90)

def compute_alert_types(data):
    alerts = []

    temp_now, rain_now = data["hourly"]["temperature_2m"][0], data["hourly"]["precipitation"][0]
    wind_now, pressure_now = data["hourly"]["windspeed_10m"][0], data["hourly"]["pressure_msl"][0]
    code_now = data["hourly"]["weathercode"][0]

    temp_30, rain_30 = data["hourly"]["temperature_2m"][1], data["hourly"]["precipitation"][1]
    wind_30, pressure_30 = data["hourly"]["windspeed_10m"][1], data["hourly"]["pressure_msl"][1]
    code_30 = data["hourly"]["weathercode"][1]

    '''
    # soglie classiche
    if rain_now > 20: alerts.append("Forte pioggia (attuale)")
    if wind_now > 70: alerts.append("Vento forte (attuale)")
    if temp_now > 35: alerts.append("Caldo estremo (attuale)")
    if pressure_now < 990: alerts.append("Instabilità atmosferica (attuale)")

    if rain_30 > 20: alerts.append("Forte pioggia (+30min)")
    if wind_30 > 70: alerts.append("Vento forte (+30min)")
    if temp_30 > 35: alerts.append("Caldo estremo (+30min)")
    if pressure_30 < 990: alerts.append("Instabilità atmosferica (+30min)")

    # codici meteo
    if code_now >= 61: alerts.append(f"Pioggia prevista (attuale) - {get_weather(code_now)}")
    if code_now >= 95: alerts.append(f"Temporale (attuale) - {get_weather(code_now)}")
    if code_30 >= 61: alerts.append(f"Pioggia prevista (+30min) - {get_weather(code_30)}")
    if code_30 >= 95: alerts.append(f"Temporale (+30min) - {get_weather(code_30)}")
    '''


    # allerte estreme
    if check_heavy_rain(data): alerts.append("alluvione")
    if check_flood(data): alerts.append("allagamento")
    if check_hail(data): alerts.append("grandinata")
    if check_wind(data): alerts.append("tromba d'aria")

    return alerts


def start_weather_monitor(app):
    global _scheduler_started
    if _scheduler_started:
        print("[Weather Monitor] Scheduler già avviato, skip", flush=True)
        return

    _scheduler_started = True
    print("[Weather Monitor] Avvio monitor", flush=True)

    with app.app_context():
        save_emergency_db(
            emergency_type="alluvione",
            message="Allerta dummy all'avvio",
            lat=1,
            lon=1
        )
    print("[Weather Monitor] Dummy aggiunto", flush=True)

    def job():
        with app.app_context():
            print(f"[Weather Monitor] Job start: {datetime.utcnow()}", flush=True)
            for city, (lat, lon) in CITIES.items():
                try:
                    data = fetch_weather(lat, lon)
                    alerts = compute_alert_types(data)
                    print(f"[Weather Monitor] Meteo {city}: Temp={data['hourly']['temperature_2m'][0]}°C, "
                          f"Rain={data['hourly']['precipitation'][0]}mm, "
                          f"Wind={data['hourly']['windspeed_10m'][0]}km/h, "
                          f"Code={data['hourly']['weathercode'][0]}", flush=True)
                    
                    if alerts:
                        print(f"[Weather Monitor] Alerts for {city}: {alerts}", flush=True)
                        for a in alerts:
                            save_emergency_db(
                                emergency_type=a,
                                message=f"{a} rilevata a {city}",
                                lat=lat,
                                lon=lon
                            )
                    else:
                        print(f"[Weather Monitor] No alerts for {city}", flush=True)
                except Exception as e:
                    print(f"[Weather Monitor] Errore fetch per {city}: {e}", flush=True)

    # scheduler senza threading ricorsivo
    scheduler = BackgroundScheduler()
    scheduler.add_job(
        job,
        'interval',
        seconds=POLL_INTERVAL,
        next_run_time=datetime.utcnow() + timedelta(seconds=1),
        max_instances=1,
        coalesce=True,
        misfire_grace_time=30
    )
    scheduler.start()
    print("[Weather Monitor] Scheduler avviato", flush=True)
    
    



def save_emergency_db(emergency_type, message, lat, lon, radius=5.0):
    from src.db_types import DBEmergencies
    from db import db
    from datetime import datetime
    from sqlalchemy import select, func

    # controlla se esiste già
    
    existing = db.session.execute(
        select(DBEmergencies).where(
            DBEmergencies.emergency_type == emergency_type,
            DBEmergencies.location.op("<->")(func.point(lon, lat))==0,
            DBEmergencies.end_time.is_(None)
        )
    ).first()
    

    if existing:
        print("Gia presente")
        return
    
    emergency = DBEmergencies(
        emergency_type=emergency_type,
        message=message,
        location=func.point(lon, lat),
        radius=radius,
        start_time=datetime.utcnow(),
        end_time=None
    )

    db.session.add(emergency)
    db.session.commit()
    print("SALVATA")

        
        
    