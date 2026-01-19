import logging
from datetime import datetime, timedelta
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
from apscheduler.schedulers.background import BackgroundScheduler

# Configurazione Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

# Configurazione API e Soglie
API_URL = "https://api.open-meteo.com/v1/forecast"
API_FLOOD = "https://flood-api.open-meteo.com/v1/flood"
POLL_INTERVAL = 60 

THRESHOLD_RAIN_FLOOD = 20.0
THRESHOLD_RAIN_WATERLOG = 10.0
THRESHOLD_WIND_TORNADO = 85.0
PRESSURE_DROP_CRITICAL = -2.5


def get_session():
    session = requests.Session()
    retries = Retry(
        total=3,
        backoff_factor=1,
        status_forcelist=[500, 502, 503, 504],
        raise_on_status=False
    )
    session.mount('https://', HTTPAdapter(max_retries=retries))
    return session

http_session = get_session()


def fetch_weather_and_flood(lat, lon):
    try:
        
        w_params = {
            "latitude": lat, "longitude": lon,
            "hourly": "temperature_2m,precipitation,windspeed_10m,pressure_msl,weathercode",
            "timezone": "Europe/Rome", "forecast_days": 1
        }
        w_res = http_session.get(API_URL, params=w_params, timeout=10).json()

        
        f_params = {
            "latitude": lat, "longitude": lon,
            "daily": "river_discharge", "forecast_days": 1
        }
        f_res = http_session.get(API_FLOOD, params=f_params, timeout=10).json()
        
        return w_res, f_res
    except Exception as e:
        logger.error(f"Errore durante la richiesta API ({lat}, {lon}): {e}")
        return None, None



def is_flood(w_data, f_data):
    rain_now = w_data["hourly"]["precipitation"][0]
    rain_next = w_data["hourly"]["precipitation"][1]
    code_now = w_data["hourly"]["weathercode"][0]
    discharge = f_data.get("daily", {}).get("river_discharge", [0])[0] if f_data else 0
    
    # Alluvione se: Pioggia estrema (>20) O (Pioggia >10 E Portata fiume >200) O Codice temporale estremo
    return rain_now >= THRESHOLD_RAIN_FLOOD or (rain_now > 10 and discharge > 200) or code_now == 99

def is_waterlog(w_data, f_data):
    rain_now = w_data["hourly"]["precipitation"][0]
    code_now = w_data["hourly"]["weathercode"][0]
    if is_flood(w_data, f_data): return False
    return rain_now >= THRESHOLD_RAIN_WATERLOG or code_now in [65, 81, 82]

def is_hail(w_data):
    hail_codes = [89, 90, 96, 99]
    return w_data["hourly"]["weathercode"][0] in hail_codes or w_data["hourly"]["weathercode"][1] in hail_codes

def is_tornado(w_data):
    wind_now = w_data["hourly"]["windspeed_10m"][0]
    pressure_now = w_data["hourly"]["pressure_msl"][0]
    pressure_next = w_data["hourly"]["pressure_msl"][1]
    return wind_now >= THRESHOLD_WIND_TORNADO and ((pressure_next - pressure_now) <= PRESSURE_DROP_CRITICAL or w_data["hourly"]["weathercode"][0] >= 95)

def compute_alert_types(w_data, f_data):
    alerts = []
    if is_flood(w_data, f_data):    alerts.append("alluvione")
    if is_waterlog(w_data, f_data): alerts.append("allagamento")
    if is_hail(w_data):             alerts.append("grandinata")
    if is_tornado(w_data):          alerts.append("tromba d'aria")
    return alerts

def create_detailed_message(city, alert_type, w_data, f_data):
    rain = w_data['hourly']['precipitation'][0]
    wind = w_data['hourly']['windspeed_10m'][0]
    discharge = f_data.get("daily", {}).get("river_discharge", [0])[0] if f_data else 0
    
    msg = f"Rilevata emergenza {alert_type.upper()} a {city}. "
    
    if alert_type == "alluvione":
        msg += f"Pioggia: {rain}mm/h. Portata fluviale: {discharge}m³/s. Pericolo esondazione."
    elif alert_type == "allagamento":
        msg += f"Accumuli idrici urbani previsti ({rain}mm/h)."
    elif alert_type == "grandinata":
        msg += "Rilevata attività convettiva con rischio grandine imminente."
    elif alert_type == "tromba d'aria":
        msg += f"Venti estremi a {wind}km/h con forte instabilità barometrica."
        
    return msg

def calculate_emergency_radius(alert_type, w_data):
    rain = w_data["hourly"]["precipitation"][0]
    if alert_type == "alluvione": return round(10.0 + (max(0, rain - 20) / 5), 1)
    if alert_type == "allagamento": return 4.0
    if alert_type == "grandinata": return 5.0
    if alert_type == "tromba d'aria": return round(3.0 + (w_data["hourly"]["windspeed_10m"][0] / 40), 1)
    return 5.0



def save_emergency_db(emergency_type, message, lat, lon, radius):
    from src.db_types import DBEmergencies
    from db import db
    from sqlalchemy import select, func, cast, Float


    tolerance = radius * 0.009

    # Controllo di prossimità spaziale per evitare duplicati
    existing = db.session.execute(
        select(DBEmergencies).where(
            DBEmergencies.emergency_type == emergency_type,
            DBEmergencies.end_time.is_(None),
            DBEmergencies.location.op("<->")(func.point(lon, lat)) < tolerance

        )
    ).first()

    if existing: return

    emergency = DBEmergencies(
        emergency_type=emergency_type, message=message,
        location=func.point(lon, lat), radius=radius,
        start_time=datetime.utcnow(),
        end_time=None
    )
    db.session.add(emergency)
    db.session.commit()
    logger.info(f"SALVATA: {emergency_type.upper()} a {lat},{lon} (Raggio: {radius}km)")

_scheduler_started = False

def start_weather_monitor(app):
    global _scheduler_started
    if _scheduler_started: return
    _scheduler_started = True

    with app.app_context():
        try:
            logger.info("[Weather Monitor] Inserimento emergenza fittizia di test...")
            save_emergency_db(
                emergency_type="alluvione",
                message="TEST SISTEMA: Monitoraggio meteo avviato correttamente. Questa è un'allerta fittizia.",
                lat=0.0,
                lon=0.0,
                radius=1.0
            )
            logger.info("[Weather Monitor] Emergenza dummy inserita con successo.")
        except Exception as e:
            logger.error(f"[Weather Monitor] Errore nell'inserimento della dummy: {e}")

    from weather_data.utils import get_cities
    CITIES = get_cities()

    def job():
        with app.app_context():
            logger.info(f"--- Ciclo monitoraggio su {len(CITIES)} città ---")
            for city, (lat, lon) in CITIES.items():
                w_data, f_data = fetch_weather_and_flood(lat, lon)
                if not w_data: continue

                # Log console per ogni città
                #print(f"[{city.upper()}] R:{w_data['hourly']['precipitation'][0]}mm | W:{w_data['hourly']['windspeed_10m'][0]}km/h | Code:{w_data['hourly']['weathercode'][0]}", flush=True)

                alerts = compute_alert_types(w_data, f_data)
                for a in alerts:
                    msg = create_detailed_message(city, a, w_data, f_data)
                    rad = calculate_emergency_radius(a, w_data)
                    save_emergency_db(a, msg, lat, lon, rad)

    scheduler = BackgroundScheduler(
        executors={'default': {'type': 'threadpool', 'max_workers': 10}}
    )
    scheduler.add_job(
        job,
        'interval',
        seconds=POLL_INTERVAL,
        next_run_time=datetime.utcnow() + timedelta(seconds=1),
        max_instances=3,
        coalesce=True,
        misfire_grace_time=60
    )
    scheduler.start()
    logger.info("Weather Monitor Scheduler operativo con integrazione GloFAS.")