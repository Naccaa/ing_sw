from src.db_types import DBUser
from flask import Blueprint, request, current_app
from flask_jwt_extended import create_access_token
from db import db
import sqlalchemy as sq

sessions_route = Blueprint('sessions_route', __name__)

'''
body request{
    email: string,
    password: string,
}

tocken{
    id: int,
    is_admin: bool
}
'''
@sessions_route.route('/sessions', methods=['POST'])
def create_user_session():
    data = request.get_json()
    email = data.get('email', '').lstrip().rstrip().lower()
    password = data.get('password', '').lstrip().rstrip()
    if email == '':
        return {"error": True, "message" : "Request must contain the email of the user"}, 400
    if password == '':
        return {"error": True, "message" : "Request must contain the password of the user"}, 400
    id: int

    try:
        found_user = db.session.execute(sq.select(DBUser).filter(DBUser.email == email)).scalar_one_or_none()
        if found_user == None:
            return {"error" : True, "message" : "User not found"}, 404
        id = int(found_user.user_id)
    except:
        return {"error": True, "message" : "Server error"}, 500

    if found_user.verify_password(password) == False:
        return {"error" : True, "message" : "Incorrect password"}, 401

    jwt_token = create_access_token(str(id), additional_claims={"is_admin": found_user.is_admin})
    return {"error" : False, "message" : "Session created successfully", "data" : {"session_token" : jwt_token}}, 200
