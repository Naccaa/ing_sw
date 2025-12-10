import datetime
from flask import Blueprint, current_app, request, jsonify
#from flask_jwt_extended import get_jwt
#from flask_jwt_extended.view_decorators import jwt_required
from src.db_types import DBGuidelines, emergency_type
 
from sqlalchemy import exc
from db import db

from src.auth_decorators import required_admin

guidelines_route = Blueprint('guidelines_route', __name__)

'''
request body{
    emergency_type: str
    message: str
}
'''
@guidelines_route.route('/guidelines', methods=['POST'])
@required_admin
def add_guideline():
    data = request.get_json()

    # valida se tutti i campi sono stati inseriti nella richiesta
    if 'emergency_type' not in data or data['emergency_type'] not in [item.value for item in emergency_type]:
        return {"error": True, "message" : f"Request must contain one of the following emergency type: {[item.value for item in emergency_type]}"}, 400
    if 'message' not in data:
        return {"error": True, "message" : "Request must contain the message (description of the guideline)"}, 400

    new_guideline = DBGuidelines(emergency_type=data["emergency_type"],
                                message=data["message"])
    try:
      db.session.add(new_guideline)
      db.session.commit()
    except exc.IntegrityError:
      return {"error" : True, "message": "A guideline for this emergency type already exists"}, 400
    except Exception as e:
      current_app.logger.debug(e)
      return {"error" : True, "message": "Server error"}, 500
    return {"error" : False, "message" : "Guideline created successfully"}, 201


'''
Ritorna la lista delle guidelines per mostrarle tutte insieme
all'utente.
'''
@guidelines_route.route('/guidelines', methods=['GET'])
def show_guidelines():
  try:
    rows = DBGuidelines.query.all()
  except Exception as e:
    current_app.logger.debug(e)
    return {"error": True, "message": "Server error"}, 500

  return jsonify([
      {
          "emergency_type": r.emergency_type,
          "message": r.message
      }
      for r in rows
  ])



'''
Ritorna la guideline specifica per farla leggere all'utente
quanto viene scelta dall'utente nell'interfaccia che le mostra tutte.
'''
@guidelines_route.route('/guideline/<id>', methods=['GET'])
def show_guideline(id):
  try:
    rows = DBGuidelines.query.filter(DBGuidelines.emergency_type == id).all()
  except Exception as e:
    current_app.logger.debug(e)
    return {"error": True, "message": "Server error"}, 500

  return jsonify([
      {
          "emergency_type": r.emergency_type,
          "message": r.message
      }
      for r in rows
  ])