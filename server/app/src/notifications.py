from firebase_admin import messaging
from firebase_admin.exceptions import FirebaseError
from typing import Optional, Dict

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
    

    try:
        # The messaging.send() function communicates with the FCM service
        response = messaging.send(messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body),
            data=data_payload,
            token=token
        ))
        
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