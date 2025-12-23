from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy import Float
from sqlalchemy.dialects.postgresql import types as pg_types
from db import db
import secrets
import hmac
import datetime
from enum import Enum


class Base(DeclarativeBase):
    pass


class PasswordInterface():
    password_salt_hex: Mapped[str]
    password_digest_hex: Mapped[str]

    def verify_password(self, password):
        h = hmac.new(bytes.fromhex(self.password_salt_hex), digestmod="sha512")
        h.update(password.encode('utf-8'))
        return hmac.compare_digest(self.password_digest_hex, h.hexdigest())

    def set_password(self, password):
        salt = secrets.token_bytes(16)
        h = hmac.new(salt, digestmod='sha512')
        h.update(password.encode('utf-8'))
        self.password_salt_hex = salt.hex()
        self.password_digest_hex = h.hexdigest()

class user_status(Enum):
    FINE = 'fine'
    PENDING = 'pending'
    IN_DANGE = 'inDanger'


class DBUser(db.Model, PasswordInterface):
    __table__ = db.metadata.tables['users']

    user_id: Mapped[int]
    caregiver_id: Mapped[int]
    email: Mapped[str]
    fullname: Mapped[str]
    phone_number: Mapped[str]
    status: Mapped[user_status]
    status_time: Mapped[datetime.datetime]
    last_location: Mapped[pg_types.POINT]
    last_location_time: Mapped[datetime.datetime]
    is_admin: Mapped[bool]
    # può essere NULL
    firebase_token: Mapped[str]
        
    def __init__(
        self,
        email,
        fullname,
        phone_number,
        password,
        caregiver_id = None,
        status = user_status.FINE,
        status_time = None,
        last_location = None,
        last_location_time = None,
        is_admin = False,
        firebase_token = None
    ):
        self.caregiver_id = caregiver_id
        self.email = email
        self.fullname = fullname
        self.phone_number = phone_number
        self.status = status.value
        self.status_time = status_time
        self.last_location = last_location
        self.last_location_time = last_location_time
        self.is_admin = is_admin
        self.firebase_token = firebase_token
        self.set_password(password)

    def __repr__(self):
        return f"DBUser(user_id={self.user_id}, caregiver_id={self.caregiver_id}, " \
            f"email={self.email}, fullname={self.fullname}, phone_number={self.phone_number}, " \
            f"status={self.status}, status_time={self.status_time}, " \
            f"last_location={self.last_location}, last_location_time={self.last_location_time}, " \
            f"is_admin={self.is_admin}, firebase_token={self.firebase_token}, "\
            f"password_salt_hex={self.password_salt_hex}, password_digest_hex={self.password_digest_hex})" 

    def to_dict(self):
        return {
            "user_id": self.user_id,
            "caregiver_id": self.caregiver_id,
            "email": self.email,
            "fullname": self.fullname,
            "phone_number": self.phone_number,
            "status": self.status,
            "status_time": self.status_time,
            "last_location": self.last_location,
            "last_location_time": self.last_location_time,
            "is_admin": self.is_admin,
            "firebase_token": self.firebase_token,
            "password_salt_hex": self.password_salt_hex,
            "password_digest_hex": self.password_digest_hex
        }


class emergency_type(Enum):
    ALLAGAMENTO = "allagamento"
    ALLUVIONE = "alluvione"
    GRANDINATA = "grandinata"
    TROMBA_DARIA = "tromba d'aria"
    ALTRO = "altro"

class DBEmergencies(db.Model):
    __table__ = db.metadata.tables['emergencies']
    
    id: Mapped[int]
    emergency_type: Mapped[emergency_type]
    message: Mapped[str]

    location: Mapped[pg_types.POINT]
    radius: Mapped[float]
    
    start_time: Mapped[datetime.datetime]
    end_time: Mapped[datetime.datetime]
    
    def __init__(self, emergency_type, message, location, radius, start_time, end_time):
        self.emergency_type = emergency_type
        self.message = message
        self.location = location
        self.radius = radius
        self.start_time = start_time
        self.end_time = end_time

    def __repr__(self):
        return f"DBEmergency(id={self.id}, emergency_type={self.emergency_type}, message={self.message}, " \
               f"location={self.location}, radius={self.radius}, start_time={self.start_time}, end_time={self.end_time})"

    def to_dict(self):
        return {
            "id": self.id,
            "emergency_type": self.emergency_type,
            "message": self.message,
            "location": self.location,
            "radius": self.radius,
            "start_time": self.start_time,
            "end_time": self.end_time
        }

class DBGuidelines(db.Model):
    __table__ = db.metadata.tables['guidelines']
    
    emergency_type: Mapped[emergency_type]
    message: Mapped[str]

    def __init__(self, emergency_type, message):
        self.emergency_type = emergency_type
        self.message = message

    def __repr__(self):
        return f"DBGuideline(emergency_type={self.emergency_type}, message={self.message})"

    def to_dict(self):
        return {
            "emergency_type": self.emergency_type,
            "message": self.message,
        }

class DBPasswordResetTokens(db.Model):
    __table__ = db.metadata.tables['password_reset_tokens']
    
    token: Mapped[str]
    user_id: Mapped[int]
    expires_at: Mapped[datetime.datetime]
    created_at: Mapped[datetime.datetime]
    used: Mapped[bool]

    def __init__(self, token, user_id, expires_at, created_at, used):
        self.token = token
        self.user_id = user_id
        self.expires_at = expires_at
        self.created_at = created_at
        self.used = used 

    def __repr__(self):
        return f"DBPasswordResetTokens(token={self.token}, user_id={self.user_id}, expires_at={self.expires_at}, created_at={self.created_at}, used={self.used})"

    def to_dict(self):
        return {
            "token": self.token,
            "user_id": self.user_id,
            "expires_at": self.expires_at,
            "created_at": self.created_at,
            "used": self.used
        }