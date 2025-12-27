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
@guidelines_route.route('/guidelines', methods=['GET'])
def show_guidelines():
    try:
        # Guide fittizie con messaggi più lunghi
        fake_guides = [
            {
                "emergency_type": "Allagamento",
                "message": "In caso di allagamento, evita di camminare o guidare in acque allagate. Mantieni oggetti importanti e documenti in posti elevati. Segui le indicazioni delle autorità locali e resta aggiornato sulle previsioni."
            },
            {
                "emergency_type": "Alluvione",
                "message": "Durante un'alluvione, cerca rifugio in zone elevate e sicure. Non attraversare mai corsi d'acqua in piena a piedi o in auto. Tieni pronto un kit di emergenza con cibo, acqua, medicine e torce."
            },
            {
                "emergency_type": "Grandinata",
                "message": "Se è prevista grandinata, rimani al coperto e proteggi veicoli e finestre. Evita di uscire all'aperto fino a quando il fenomeno non termina, poiché i chicchi di grandine possono provocare gravi danni."
            },
            {
                "emergency_type": "Tromba d'aria",
                "message": "Durante una tromba d'aria, trova subito un riparo sicuro lontano da finestre e oggetti che potrebbero volare. Se sei all'aperto, cerca rifugio in un edificio robusto o in un vano interrato."
            }
        ]

        return jsonify(fake_guides)

    except Exception as e:
        current_app.logger.debug(e)
        return {"error": True, "message": "Server error"}, 500


'''
Ritorna la guideline specifica per farla leggere all'utente
quanto viene scelta dall'utente nell'interfaccia che le mostra tutte.
'''
@guidelines_route.route('/guidelines/<id>', methods=['GET'])
def show_guideline(id):
  if id not in [item.value for item in emergency_type]:
    return {"error": True, "message" : f"Request must contain one of the following emergency type: {[item.value for item in emergency_type]}"}, 400
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
