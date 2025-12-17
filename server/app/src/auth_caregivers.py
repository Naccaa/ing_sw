import random
import string
from src.db_types import DBCaregivers
from datetime import datetime, timedelta
from db import db
from flask import current_app
import smtplib
from email.message import EmailMessage
from os import getenv

def check_auth_token(caregiver_id, auth_token):
    caregiver = DBCaregivers.query.get(caregiver_id)
    if not caregiver:
        return {"error": True, "message": "Caregiver not found"}, 404

    if caregiver.auth_code != auth_token and not caregiver.authenticated and caregiver.date_added + timedelta(hours=24) >= datetime.now():
        return {"error": True, "message": "Invalid authentication code"}, 403

    caregiver.authenticated = True
    try:
        db.session.commit()
    except Exception as e:
        current_app.logger.debug(e)
        return {"error": True, "message": "Server error"}, 500

    return {"error": False, "message": "Caregiver authenticated successfully"}, 200 

def generate_auth_token():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=32))

def send_auth_email(caregiver_email, auth_token, user_id, alias, phone_number, user_fullname, user_email, user_phone):
    source = getenv('SENDER_MAIL')
    password = getenv('MAIL_PASSWORD')

    url = f"http://localhost:5000/authenticate/{user_id}/{auth_token}"  # Replace with actual URL   
    msg = EmailMessage()
    msg['Subject'] = "Caregiver Authentication Code"
    msg['From'] = source
    msg['To'] = caregiver_email

    msg.set_content(f"""
    Hello {alias}, 
    You have been registered as a caregiver by this user:
    - user full name: {user_fullname}
    - user email: {user_email}
    - user phone number: {user_phone}
    Your phone number: {phone_number}
    You can authenticate as a caregiver by visiting the following link: {url}
    This link will expire in 24 hours.
    If you do not wish to be a caregiver or your informations are wrong, please ignore this email.
    """)
    try:
        with smtplib.SMTP('smtp.gmail.com', 587) as server:
            server.starttls()  
            server.login(source, password)
            server.send_message(msg)
    except Exception as e:
        current_app.logger.debug(f"Error sending email: {e}")