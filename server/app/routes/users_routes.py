from datetime import datetime, timezone
from os import wait
from flask import Blueprint, current_app, request, jsonify
from flask_jwt_extended import get_jwt
from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBUser, DBCaregivers, user_status
from src.auth_caregivers import *
 
from sqlalchemy import exc
from db import db

import secrets
import hashlib


'''
request body{
    email: string,
    fullname: string,
    phone_number: string,
    password: string

    is_admin: bool # only if an admin sends the request
}
'''
# testata
users_route = Blueprint('users_route', __name__)
@users_route.route('/users', methods=['POST'])
@jwt_required(optional=True)
def add_user():
    data = request.get_json()
    current_app.logger.info(data)
    # valida se tutti i campi sono stati inseriti nella richiesta
    if 'email' not in data:
        return {"error": True, "message" : "Request must contain the email of the user"}, 400
    if 'fullname' not in data:
        return {"error": True, "message" : "Request must contain the fullname of the user"}, 400
    if 'phone_number' not in data:
        return {"error": True, "message" : "Request must contain the phone number of the user"}, 400
    if 'password' not in data:
        return {"error": True, "message" : "Request must contain the password of the user"}, 400

    user = DBUser(email=data["email"].lower().lstrip().rstrip(),
                      fullname=data["fullname"].lstrip().rstrip(), 
                      phone_number=data["phone_number"].lstrip().rstrip(), 
                      password=data["password"])
    
    auth_data = get_jwt()
    if "is_admin" in auth_data and auth_data["is_admin"]:
        user.is_admin = data["is_admin"]
    
    try:
      db.session.add(user)
      db.session.commit()
      response_data = {
        "user_id": user.user_id,
        "email": user.email,
        "fullname": user.fullname,
        "phone_number": user.phone_number,
        "status": user.status,
        "status_time": user.status_time,
        "last_location": user.last_location,
        "last_location_time": user.last_location_time,
        "is_admin": user.is_admin
        }
      return {"error" : False, "message" : "User created successfully", "data": response_data }, 201
    except exc.IntegrityError:
      return {"error" : True, "message": "Email already used"}, 400
    except Exception as e:
      current_app.logger.debug(e)
      return {"error" : True, "message": "Server error"}, 500

@users_route.route('/users/<int:userId>', methods=['GET'])
@jwt_required()
def get_user(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information of another user"}, 403
    
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404
    response_data = {
        "user_id": user.user_id,
        "email": user.email,
        "fullname": user.fullname,
        "phone_number": user.phone_number,
        "status": user.status,
        "status_time": user.status_time,
        "last_location": user.last_location,
        "last_location_time": user.last_location_time,
        "is_admin": user.is_admin
    }
    return {'error':False, "Message":"Ok", "data":response_data}, 200

       

'''
request body{
    email: string,
    fullname: string,
    phone_number: string,
    status: string,
    location: "1,3",
    is_admin: bool,
    password: string 
}
'''
# testato
@users_route.route('/users/<int:userId>', methods=['PATCH'])
@jwt_required()
def patch_user(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot change information about another user"}, 403
    
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404

    request_body = request.get_json(silent=True) or {}
    
    if password := request_body.get("password"):
        user.set_password(password) # Per cambiare password si usa il metodo implementato in DBUser

    now_utc = datetime.now(timezone.utc)

    if location := request_body.get("location"):
        try:
            parts = location.split(",")
            if len(parts) != 2:
                raise ValueError("location must be 'x,y'")
            x = float(parts[0].strip())
            y = float(parts[1].strip())
            # store as PostgreSQL point literal e.g. "(x,y)"
            user.last_location = f"({x},{y})"
            user.last_location_time = now_utc
        except ValueError as ve:
            return {"error": True, "message": f"Invalid location: {ve}"}, 400
        except Exception as e:
            current_app.logger.debug(e)
            return {"error": True, "message": "Failed to set location"}, 500

    if status := request_body.get("status"):
        if status not in [item.value for item in user_status]:
            return {"error": True, "message": "Invalid state"}, 400
        user.status = status
        user.status_time = now_utc

    if full_name := request_body.get("fullname"):
        if full_name.strip() == '':
            return {"error": True, "message": "Empty name are not allowed"}, 400
        user.fullname = full_name.strip()

    if phone_number := request_body.get("phone_number"):
        if phone_number.strip() == '':
            return {"error": True, "message": "Empty phone numbers are not allowed"}, 400
        user.phone_number = phone_number.strip()
    
    # non controlla se la mail è già usata da un altro utente
    # in quel caso tanto fallisce il commit
    if email := request_body.get("email"):
        if email.strip() == '':
            return {"error": True, "message": "Empty emails are not allowed"}, 400
        user.email = email.strip()

    if is_admin := request_body.get("is_admin"):
        if auth_data.get("is_admin") == False:
            return {'error': True, "message": "Only admins can change the is_admin field"}, 403
        user.is_admin = is_admin
    
    #if caregiver_id := request_body.get("caregiver_id"):
    #    user.caregiver_id = caregiver_id

    if firebase_token := request_body.get("firebase_token"):
        user.firebase_token = firebase_token

    try:
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

@users_route.route('/users/<int:userId>', methods=['DELETE'])
@jwt_required()
def delete_user(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot delete another user"}, 403

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

@users_route.route('/users/<int:userId>/caregivers', methods=['GET'])
@jwt_required()
def get_caregivers(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information about another user"}, 403
    
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404
    #if user.caregiver_id == None:
    #    response_data = {}
    else:
        caregivers = DBCaregivers.query.filter(DBCaregivers.user_id==userId, DBCaregivers.authenticated==True).all()
        response_data = [
            {
                "caregiver_id": c.caregiver_id,
                "email": c.email,
                "phone_number": c.phone_number,
                "alias": c.alias,
                "user_id": c.user_id,
                "authenticated": c.authenticated
            }
            for c in caregivers
        ]
    return {"error": False, "message": "Caregivers list retrieved successfully", "data":response_data}, 200


'''
request body{
    email: string,
    alias: string,
    phone_number: string,
}
'''
@users_route.route('/users/<int:userId>/caregivers', methods=['POST'])
@jwt_required()
def add_caregiver(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information about another user"}, 403
   
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404
    else:
        data = request.get_json()
        if 'email' not in data:
            return {"error": True, "message" : "Request must contain the email of the caregiver"}, 400
        if 'phone_number' not in data:
            return {"error": True, "message" : "Request must contain the phone number of the caregiver"}, 400
        if 'alias' not in data:
            return {"error": True, "message" : "Request must contain the alias of the caregiver"}, 400
       
        auth_token = generate_auth_token()

        new_caregiver = DBCaregivers(email=data["email"].lower().lstrip().rstrip(),
                                     phone_number=data["phone_number"].lstrip().rstrip(),
                                     alias=data["alias"].lstrip().rstrip(),
                                     user_id=userId,
                                     authenticated=False, auth_code=auth_token, 
                                     date_added=datetime.now(timezone.utc))
        
        try:
            db.session.add(new_caregiver)
            db.session.commit()
            # after the commit the new_caregiver.caregiver_id gets updated, so it can be used to send the auth mail
            send_auth_email(new_caregiver.email, auth_token, new_caregiver.caregiver_id, new_caregiver.alias, new_caregiver.phone_number, user.fullname, user.email, user.phone_number)
        except Exception as e:
            current_app.logger.debug(e)
            return {"error" : True, "message": "Server error"}, 500
        return {"error" : False, "message" : "Caregiver created successfully"}, 201
 
'''
request body{
    email: string,
    alias: string,
    phone_number: string,
}
'''
@users_route.route('/users/<int:userId>/caregivers/<int:caregiverId>', methods=['PATCH'])
@jwt_required()
def patch_caregiver(userId, caregiverId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information about another user"}, 403
    
    caregiver = DBCaregivers.query.filter(DBCaregivers.caregiver_id == caregiverId, DBCaregivers.user_id == userId).first()
    
    if not caregiver:
        return {"error": True, "message": "Caregiver not found"}, 404
    
    data = request.get_json()
    if 'email' in data:
        caregiver.email = data["email"].lower().lstrip().rstrip()
    if 'phone_number' in data:
        caregiver.phone_number = data["phone_number"].lstrip().rstrip()
    if 'alias' in data:
        caregiver.alias = data["alias"].lstrip().rstrip()
    try:
        db.session.commit()
    except Exception as e:
        current_app.logger.debug(e)
        return {"error" : True, "message": "Server error"}, 500
    return {"error" : False, "message" : "Caregiver updated successfully"}, 201

@users_route.route('/users/<int:userId>/caregivers/<int:caregiverId>', methods=['DELETE'])
@jwt_required()
def delete_caregiver(userId, caregiverId):
    # controlla se l'utente autenticato è lo stesso di userId
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot delete another user"}, 403

    caregiver = DBCaregivers.query.filter(DBCaregivers.caregiver_id == caregiverId, DBCaregivers.user_id == userId).first()
    
    if not caregiver:
        return {"error": True, "message": "Caregiver not found"}, 404

    try:
        db.session.delete(caregiver)
        db.session.commit()
    except Exception as e:
        current_app.logger.debug(e)
        db.session.rollback()
        return {"error": True, "message": "Server error"}, 500

    return {"error": False, "message": "Caregiver deleted successfully"}, 200


@users_route.route('/authenticate/<int:caregiver_id>/<string:auth_token>', methods=['GET'])
def authenticate_caregiver(caregiver_id, auth_token):
    result, status_code = check_auth_token(caregiver_id, auth_token)
    return jsonify(result), status_code
