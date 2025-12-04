from flask_jwt_extended import get_jwt, verify_jwt_in_request
from functools import wraps
from flask import request

def required_admin(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        verify_jwt_in_request()
        auth_data = get_jwt()
        if not auth_data.get("isAdmin", False):
            return {"error": True, "message": "Admin access required."}, 403
        return fn(*args, **kwargs)
    return wrapper


def required_logged_user(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        verify_jwt_in_request()
        auth_data = get_jwt()
        if auth_data.get("role") != "user":
            return {"error": True, "message": "User access required."}, 403
        return fn(*args, **kwargs)
    return wrapper
