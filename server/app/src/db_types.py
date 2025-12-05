from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from db import db
import secrets
import hmac
import datetime
from enum import Enum


class Base(DeclarativeBase):
    pass


class PasswordInterface():
    salt: Mapped[str]
    hash: Mapped[str]

    def verify_password(self, password):
        h = hmac.new(bytes.fromhex(self.salt), digestmod="sha512")
        h.update(password.encode('utf-8'))
        return hmac.compare_digest(self.hash, h.hexdigest())

    def set_password(self, password):
        salt = secrets.token_bytes(16)
        h = hmac.new(salt, digestmod='sha512')
        h.update(password.encode('utf-8'))
        self.salt = salt.hex()
        self.hash = h.hexdigest()


class user_status(Enum):
    FINE = 'fine',
    PENDING = 'pending',
    IN_DANGE = 'inDanger',


class DBUser(db.Model, PasswordInterface):
    __table__ = db.metadata.tables['Users']

    userId: Mapped[int]
    caregiverId: Mapped[int]
    email: Mapped[str]
    fullname: Mapped[str]
    phone_number: Mapped[str]
    last_status: Mapped[user_status]
    last_status_time: Mapped[datetime.datetime]
    last_location: Mapped[(float, float)]
    last_location_time: Mapped[datetime.datetime]
    is_admin: Mapped[bool]

    def __init__(self, userId, caregiverId, email, fullname, phone_number, last_status, last_status_time, last_location, last_location_time, is_admin, password):
        self.userId = userId
        self.caregiverId = caregiverId
        self.email = email
        self.fullname = fullname
        self.phone_number = phone_number
        self.last_status = last_status
        self.last_status_time = last_status_time
        self.last_location = last_location
        self.last_location_time = last_location_time
        self.is_admin = is_admin
        self.set_password(password)

    def __repr__(self):
        return f"DBUser(userId={self.userId}, caregiverId={self.caregiverId}, " \
               f"email={self.email}, fullname={self.fullname}, phone_number={self.phone_number}, last_status={self.last_status}, last_status_time={self.last_status_time}, last_location={self.last_location}, last_location_time={self.last_location_time}" \
               f"is_admin={self.is_admin}, salt={self.salt}, hash={self.hash})"

    def to_dict(self):
        return {
            "userId": self.userId,
            "caregiverId": self.caregiverId,
            "email": self.email,
            "fullname": self.fullname,
            "phone_number": self.phone_number,
            "last_status": self.last_status,
            "last_status_time": self.last_status_time,
            "last_location": self.last_location,
            "last_location_time": self.last_location_time,
            "is_admin": self.is_admin,
            "salt": self.salt,
            "hash": self.hash
        }

class emergency_type(Enum):
    ALLAGAMENTO = "allagamento"
    ALLUVIONE = "alluvione"
    GRANDINATA = "grandinata"
    TROMBA_DARIA = "tromba d'aria"
    ALRO = "alro"

class DBEmergencies(db.Model):
    __table__ = db.metadata.tables['Emergencies']
    
    id: Mapped[int]
    emergency_type: Mapped[emergency_type]
    message: Mapped[str]
    
    location: Mapped[(float, float)]
    radius: Mapped[float]
    
    start_time: Mapped[datetime.datetime]
    end_time: Mapped[datetime.datetime]
    
    def __init__(self, id, emergency_type, message, location, radius, start_time, end_time):
        self.id = id
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

class Guidelines(db.Model):
    __table__ = db.metadata.tables['Guidelines']
    
    emergency_type: Mapped[emergency_type]
    message: Mapped[str]

    def __repr__(self):
        return f"DBGuideline(emergency_type={self.emergency_type}, message={self.message})"

    def to_dict(self):
        return {
            "emergency_type": self.emergency_type,
            "message": self.message,
        }
