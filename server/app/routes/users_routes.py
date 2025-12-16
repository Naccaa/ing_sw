import datetime
from flask import Blueprint, current_app, request, jsonify
from flask_jwt_extended import get_jwt
from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBUser, DBCaregivers, user_status
 
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
}
'''
# testata
users_route = Blueprint('users_route', __name__)
@users_route.route('/users', methods=['POST'])
def add_user():
    data = request.get_json()

    # valida se tutti i campi sono stati inseriti nella richiesta
    if 'email' not in data:
        return {"error": True, "message" : "Request must contain the email of the user"}, 400
    if 'fullname' not in data:
        return {"error": True, "message" : "Request must contain the fullname of the user"}, 400
    if 'phone_number' not in data:
        return {"error": True, "message" : "Request must contain the phone number of the user"}, 400
    if 'password' not in data:
        return {"error": True, "message" : "Request must contain the password of the user"}, 400

    new_user = DBUser(email=data["email"].lower().lstrip().rstrip(),
                      fullname=data["fullname"].lstrip().rstrip(), 
                      phone_number=data["phone_number"].lstrip().rstrip(), 
                      password=data["password"])

    try:
      db.session.add(new_user)
      db.session.commit()
    except exc.IntegrityError:
      return {"error" : True, "message": "Email already used"}, 400
    except Exception as e:
      current_app.logger.debug(e)
      return {"error" : True, "message": "Server error"}, 500
    return {"error" : False, "message" : "User created successfully"}, 201

@users_route.route('/users/<int:userId>', methods=['GET'])
#@jwt_required()
def get_user(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    '''
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information of another user"}, 403
    '''
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404
    response_data = {
        "user_id": user.user_id,
        #"caregiver_id": user.caregiver_id,
        "email": user.email,
        "fullname": user.fullname,
        "phone_number": user.phone_number,
        "status": user.status,
        "status_time": user.status_time,
        "last_location": user.last_location,
        "last_location_time": user.last_location_time,
        "is_admin": user.is_admin
    }
    return response_data, 200

@users_route.route('/users/<int:userId>/caregivers', methods=['GET'])
#@jwt_required()
def get_caregivers(userId):
    # controlla se l'utente autenticato è lo stesso di userId
    '''
    auth_data = get_jwt()
    if int(auth_data.get('sub')) != userId:
        return {'error': True, "message": "Cannot get information about another user"}, 403
    '''
    user = DBUser.query.get(userId)
    if not user:
        return {"error": True, "message": "User not found"}, 404
    #if user.caregiver_id == None:
    #    response_data = {}
    else:
        caregivers = DBCaregivers.query.filter(DBCaregivers.user_id==userId).all()
        response_data = jsonify([
            {
                "caregiver_id": c.caregiver_id,
                "email": c.email,
                "phone_number": c.phone_number,
                "alias": c.alias,
                "user_id": c.user_id,
                "authenticated": c.authenticated
            }
            for c in caregivers
        ])
    return response_data, 200

@users_route.route('/users/<int:userId>/caregivers', methods=['POST'])
#@jwt_required()
def add_caregiver(userId):
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
       
        new_caregiver = DBCaregivers(email=data["email"].lower().lstrip().rstrip(),
                                     phone_number=data["phone_number"].lstrip().rstrip(),
                                     alias=data["alias"].lstrip().rstrip(),
                                     user_id=userId,
                                     authenticated=False)
        
        try:
            db.session.add(new_caregiver)
            db.session.commit()
        except Exception as e:
            current_app.logger.debug(e)
            return {"error" : True, "message": "Server error"}, 500
        return {"error" : False, "message" : "Caregiver created successfully"}, 201
        

'''
request body{
    caregiver_id: int,
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
#testata senza auth
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
