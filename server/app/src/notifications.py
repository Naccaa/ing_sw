from firebase_admin import messaging
from firebase_admin.exceptions import FirebaseError
from typing import Optional, Dict
import threading

from src.db_types import DBUser
from src.auth_caregivers import *

from flask import jsonify

def send(
    token: str,
    title: str,
    body: str,
    data_payload: Optional[Dict[str, str]] = None
):
    """
    # le notifiche non vengono visualizzate se l'app è aperta

    Sends an FCM notification to a single device using its registration token.

    :param token: The unique FCM token of the target device.
    :param title: The title of the notification (displayed in the system tray).
    :param body: The body/content of the notification.
    :param data_payload: Optional dictionary of custom key-value data to send to the app.
    """
    
    if data_payload is None:
        data_payload = {}

    # Aggiungi titolo e corpo ai dati "invisibili"
    data_payload["title"] = title
    data_payload["body"] = body

    try:

        android_config = messaging.AndroidConfig(
            priority='high', # <--- FONDAMENTALE: Sveglia il dispositivo anche in Doze Mode
            ttl=0 # Time To Live 0: consegna subito o mai (evita consegne vecchie)
        )

        response = messaging.send(messaging.Message(
            data=data_payload, # Solo dati!
            token=token,
            android=android_config
        ))

        """
        # The messaging.send() function communicates with the FCM service
        response = messaging.send(messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body),
            data=data_payload,
            token=token
        ))"""
        
        # Success response is the message ID
        print(f"Successfully sent message: {response}")
    except FirebaseError as e:
        # Handle specific Firebase errors (e.g., invalid token)
        print(f"Firebase error sending message: {e}")
        # Common error codes: 'INVALID_ARGUMENT', 'UNREGISTERED'
        if e.code == 'UNREGISTERED':
            print("Token is invalid/expired. Consider removing it from your database.")
    except Exception as e:
        # Handle general errors (e.g., network issues)
        print(f"An unexpected error occurred: {e}")

def get_firebase_token(user_id: int):
    user = DBUser.get_or_none(DBUser.id == user_id)
    if user is None:
        return None
    return user.firebase_token
    
def send_emergency_notification(token: str, emergency: list):
    # emergency è una lista di emergenze ottenute tramite altre funzioni
    if len(emergency)>0:
        for e in emergency:
            def send_notification():
                title = "emergenza"
                body = e.get("message", "Emergenza in corso")
                data_payload = {
                    "emergency_type": e.get("emergency_type", ""),
                    "message": e.get("message", "placeholder")
                    #"latitude": str(e.get("latitude", "")),
                    #"longitude": str(e.get("longitude", ""))
                }
                send(token, title, body, data_payload)
            threading.Thread(target=send_notification).start()
    # eventualmente mandare una notifica al client per dire che non ci sono emergenze
    
    return


# --- Example Usage ---

# send(
#     # This is the token you must get from your Android client app
#     # and store in your server's database.
#     registration_token="your_android_device_fcm_token_here" ,
#     title="Flash Flood Warning",
#     body="Immediate evacuation advised for the coastal zone.",
#     data_payload={
#     "category": "EMERGENCY_ALERT",
#     "priority": "HIGH",
#     "event_id": "FL-12052025"
# }
# )