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

# da sistemare
class user_healt(Enum):
    CITTADINO = 'cittadino',
    ADMIN = 'admin'

class user_status(Enum):
    SICURO = 'al sicuro',
    PERICOLO = 'in prericolo'

class user_status():
    healt: user_healt
    location: (float, float)
    time: datetime.datetime

    def __init__(self, healt, location, time):
        self.healt = healt
        self.location = location
        self.time = time

    def __repr__(self):
        return f"user_status(healt={self.healt}, location={self.location}, time={self.time})"

    def to_dict(self):
        return {
            'healt': self.healt, 
            'location': self.location,
            'time': self.time
        }

class DBUser(db.Model, PasswordInterface):
    __table__ = db.metadata.tables['Users']

    email: Mapped[str]
    caregiver_email: Mapped[str]
    
    role: Mapped[str]
    fullname: Mapped[str]
    phone_number: Mapped[str]
    last_status: Mapped[user_status]

    def __init__(self, email, caregiver_email, role, fullname, phone_number, last_status, password):
        self.email = email
        self.caregiver_email = caregiver_email
        self.role = role
        self.fullname = fullname
        self.phone_number = phone_number
        self.last_status = last_status
        self.set_password(password)

    def __repr__(self):
        return f"DBUser(email={self.email}, caregiver_email={self.caregiver_email}, role={self.role}, " \
               f"fullname={self.fullname}, phone_number={self.phone_number}, last_status={self.last_status})" \
               f"salt={self.salt}, hash={self.hash})"

    def to_dict(self):
        return {
            "email": self.email,
            "caregiver_email": self.caregiver_email,
            "role": self.role,
            "fullname": self.fullname,
            "phone_number": self.phone_number,
            "last_status": self.last_status.to_dict() if self.last_status else None
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
