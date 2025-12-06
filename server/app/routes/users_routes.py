import datetime
from flask import Blueprint, current_app, request, jsonify
from flask_jwt_extended import get_jwt
from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBUser, user_status
 
from sqlalchemy import exc
from db import db

from src.auth_decorators import required_logged_user

import secrets
import hashlib

users_route = Blueprint('users_route', __name__)

# TODO: testare
# TODO: dare la possibilità di aggiungere o aggiornare caregiverId
@users_route.route('/users/<int:userId>', methods=['PATCH'])
@required_logged_user
def patch_user(userId):
    # TODO: check that userId == id of the logged in user
    
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
            #user.lastLocation = f"({x},{y})"
            user.last_location = (x,y)
            user.last_location_time = now_utc
        except ValueError as ve:
            return {"error": True, "message": f"Invalid location: {ve}"}, 400
        except Exception as e:
            current_app.logger.debug(e)
            return {"error": True, "message": "Failed to set location"}, 500

    if status := request.args.get("status"):
        user.status = status
        user.status_time = now_utc

    if full_name := request.args.get("fullName"):
        user.fullname = full_name.strip()

    if phone_number := request.args.get("phoneNumber"):
        user.phone_number = phone_number.strip()

    try:
        # forse non serve chiamare .add()
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


# TODO: testare
@users_route.route('/users/<int:userId>', methods=['DELETE'])
@required_logged_user
def delete_user(userId):
    # TODO: check that userId == id of the logged in user

    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404

    try:
        db.session.delete(user)
        db.session.commit()
    except Exception as e:
        current_app.logger.debug(e)
        db.session.rollback()
        return {"error": True, "message": "Server error"}, 500

    return {"error": False, "message": "User deleted successfully"}, 200
