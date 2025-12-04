import datetime
from flask import Blueprint, current_app, request
from flask_jwt_extended import get_jwt
from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBUser, user_healt, user_status, user_status
 
from sqlalchemy import exc
from db import db

from src.auth_decorators import required_logged_user

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
