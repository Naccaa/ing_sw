import datetime
from flask import Blueprint, current_app, request, jsonify
from flask_jwt_extended import get_jwt
from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBUser, user_healt, user_status, user_status
 
from sqlalchemy import exc
from db import db

from src.auth_decorators import required_logged_user

import secrets
import hashlib

users_route = Blueprint('users_route', __name__)

''' esempio di una route possibile (che non è correlato a questo progetto)
@users_route.route('/users', methods=['POST'])
@jwt_required(optional=True)
def register_user():
    data = request.get_json()

    # Making sure that only admins can create other admins
    admin_flag = False
    auth_data = get_jwt()
    if auth_data and auth_data.get("isAdmin") == True:
        admin_flag = data["isadmin"]

    new_user = DBUser(name=data["name"].lstrip().rstrip(), 
                      surname=data["surname"].lstrip().rstrip(), 
                      email=normalize(data["email"]), 
                      password=data["password"], 
                      isadmin=admin_flag)

    try:
      db.session.add(new_user)
      db.session.commit()
    except exc.IntegrityError:
      return {"error" : True, "message": "User already exists"}, 400
    except Exception as e:
      current_app.logger.debug(e)
      return {"error" : True, "message": "Server error"}, 500
    return {"error" : False, "message" : "User created successfully"}, 201
'''

# TODO: testare
# TODO: dare la possibilità di aggiungere o aggiornare caregiverId
@users_route.route('/users/<int:userId>', methods=['PATCH'])
@required_logged_user
def patch_user(userId):
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404

    request_body = request.get_json(silent=True) or {}
    if password := request_body.get("password"):
        try:
            salt_bytes = secrets.token_bytes(16)
            salt_hex = salt_bytes.hex()
            digest_hex = hashlib.pbkdf2_hmac(
                "sha256",
                password.encode("utf-8"),
                salt_bytes,
                100000
            ).hex()
            user.password_salt_hex = salt_hex
            user.password_digest_hex = digest_hex
        except Exception as e:
            current_app.logger.debug(e)
            return {"error": True, "message": "Failed to set password"}, 500

    now_utc = datetime.datetime.now(datetime.timezone.utc)

    if location := request.args.get("location"):
        try:
            parts = location.split(",")
            if len(parts) != 2:
                raise ValueError("location must be 'x,y'")
            x = float(parts[0].strip())
            y = float(parts[1].strip())
            # store as PostgreSQL point literal e.g. "(x,y)"
            user.lastLocation = f"({x},{y})"
            user.lastLocationTime = now_utc
        except ValueError as ve:
            return {"error": True, "message": f"Invalid location: {ve}"}, 400
        except Exception as e:
            current_app.logger.debug(e)
            return {"error": True, "message": "Failed to set location"}, 500

    if status := request.args.get("status"):
        user.status = status
        user.status_time = now_utc

    if full_name := request.args.get("fullName"):
        user.fullName = full_name.strip()

    if phone_number := request.args.get("phoneNumber"):
        user.phoneNumber = phone_number.strip()

    try:
        db.session.add(user)
        db.session.commit()
    except exc.IntegrityError as ie:
        current_app.logger.debug(ie)
        db.session.rollback()
        return {"error": True, "message": "Integrity error"}, 400
    except Exception as e:
        current_app.logger.debug(e)
        db.session.rollback()
        return {"error": True, "message": "Server error"}, 500

    return {"error": False, "message": "User updated successfully"}, 200
