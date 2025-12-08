from flask import request, current_app, jsonify, Blueprint
from src.auth_decorators import required_admin

import math
from sqlalchemy import exc, text
from src.db_types import DBEmergencies, DBGuidelines
from db import db

emergencies_route = Blueprint('emergencies_route', __name__)


# TODO: testare
@emergencies_route.route('/emergencies', methods=['GET'])
def get_emergencies():
    """
    Il parametro near è opzionale e serve per filtrare le emergenze che sono vicine al punto. Il frontend passerà la posizione dell'utente come parametro near.
    Gli admin invece potrebbero voler vedere tutte le emergenze e in tal caso non passerebbero il parametro near.
    """

    # TODO: decidere un formato standard per i punti geografici (es. "x,y" o "(x,y)") e poi togliere da queste 2 funzioni gli if che non servono
    
    def parse_point(s):
        if s is None:
            return None
        s = s.strip()
        if s.startswith("(") and s.endswith(")"):
            s = s[1:-1]
        parts = s.split(",")
        if len(parts) != 2:
            raise ValueError("point must be 'x,y' or '(x,y)'")
        return float(parts[0].strip()), float(parts[1].strip())

    def serialize_location(loc):
        if loc is None:
            return None
        if isinstance(loc, (list, tuple)) and len(loc) == 2:
            return f"{float(loc[0])},{float(loc[1])}"
        if isinstance(loc, str):
            return loc.strip("()")
        try:
            return f"{float(loc.x)},{float(loc.y)}"
        except Exception:
            return str(loc)


    try:
        q = DBEmergencies.query.outerjoin(
            DBGuidelines,
            DBEmergencies.emergency_type == DBGuidelines.emergency_type
        ).with_entities(
            DBEmergencies.id,
            DBEmergencies.emergency_type,
            DBEmergencies.message,
            DBEmergencies.location,
            DBEmergencies.radius,
            DBEmergencies.start_time,
            DBEmergencies.end_time,
            DBGuidelines.message.label("guideline_message")
        )

        if near_param := request.args.get("near"):
            try:
                nx, ny = parse_point(near_param)
            except ValueError as ve:
                return {"error": True, "message": f"Invalid near parameter: {ve}"}, 400
            
            q = q.filter(
                text("((location[0] - :nx)*(location[0] - :nx) + (location[1] - :ny)*(location[1] - :ny)) <= (radius * radius)")
            ).params(nx=nx, ny=ny)

        rows = q.all()
    except Exception as e:
        current_app.logger.debug(e)
        return {"error": True, "message": "Server error"}, 500


    return jsonify([{
            "id": r.id,
            "emergency_type": str(r.emergency_type) if r.emergency_type is not None else None,
            "message": r.message,
            "location": serialize_location(r.location),
            "radius": r.radius,
            "start_time": r.start_time.isoformat() if r.start_time is not None else None,
            "end_time": r.end_time.isoformat() if r.end_time is not None else None,
            "guideline_message": r.guideline_message
        } for r in rows]), 200


@emergencies_route.route('/emergencies', methods=['POST'])
@required_admin
def post_emergency():
    raise NotImplementedError()


@emergencies_route.route('/emergencies/<int:id>', methods=['PATCH'])
@required_admin
def patch_emergency(id):
    """
    quando finisce un'emergenza un admin aggiornerà il campo end_time
    """
    raise NotImplementedError()