#!/usr/bin/env python3
import datetime
from flask import Flask, jsonify, request
from flask_cors import CORS
import os
from dotenv import load_dotenv
from flask import Flask
from flask_jwt_extended import JWTManager
from db import db

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

print(db.metadata.tables.items)

# ROUTES
from routes.users_routes import users_route
version = None
app.register_blueprint(users_route, url_prefix=version)

print(app.url_map)

@app.route('/')
def home():
    return jsonify({
        "version": "1.0",
        "endpoints": [
        ]
    })


if __name__ == "__main__":
    app.run()
