#!/usr/bin/env python3
import datetime
from flask import Flask, jsonify, request
from flask_cors import CORS
import os
from dotenv import load_dotenv
from flask import Flask
from flask_jwt_extended import JWTManager
from db import db
from auto_cleaner.auto_cleaner_setup import auto_cleaner_setup
import firebase_admin
from firebase_admin import credentials

try:
    # Get .json file from https://console.firebase.google.com/u/0/project/ing-sw-636e2/settings/serviceaccounts/adminsdk
    cred = credentials.Certificate("firebase-adminsdk.json")
    firebase_admin.initialize_app(cred)
except Exception as e:
    print(f'[Error] Firebase: {e}', flush=True)

load_dotenv()
app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY')
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv('SQLALCHEMY_DATABASE_URI')
app.config['JWT_COOKIE_SECURE'] = False # HTTP, non HTTPS per semplicità
app.config['JWT_SECRET_KEY'] = os.getenv('JWT_SECRET')
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(hours=1) # può essere alzato senza problemi

db.init_app(app)
jwt = JWTManager(app)
CORS(app)

with app.app_context():
    db.reflect()

    # test notifiche 
    from src.db_types import DBUser
    from src.notifications import send

    try:
        for user in DBUser.query.all():
            send(
                token=user.firebase_token,
                title="Test Notifica",
                body="Questa è una notifica di test inviata all'avvio del server."
            )
    except Exception as e:
        print(f'[Error] Firebase: {e}', flush=True)

print(db.metadata.tables.items)

# ROUTES
from routes.users_routes import users_route
from routes.guidelines_routes import guidelines_route
from routes.emergencies_routes import emergencies_route
from routes.sessions_route import sessions_route
from routes.reset_password import reset_bp

version = None
app.register_blueprint(users_route, url_prefix=version)
app.register_blueprint(guidelines_route, url_prefix=version)
app.register_blueprint(emergencies_route, url_prefix=version)
app.register_blueprint(sessions_route, url_prefix=version)
app.register_blueprint(reset_bp, url_prefix=version)

print(app.url_map)

@app.route('/')
def home():
    endpoints = []
    for rule in app.url_map.iter_rules():
        for method in list(rule.methods):
            endpoints.append(method + " " + rule.rule)
    return jsonify({
        "error": False,
        "version": "1.0",
        "endpoints": endpoints
    }), 200

@app.after_request
def check_response_format(response):
    if response.content_type != 'application/json':
        app.logger.warning("Response is not JSON")
        return response
    
    json = response.get_json() 
    if 'error' not in json:
        app.logger.warning("Response missing 'error' key")
        return response
        
    return response


if __name__ == "__main__":
    auto_cleaner_setup()
    app.run()
