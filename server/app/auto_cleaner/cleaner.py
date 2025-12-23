from src.db_types import DBEmergencies, DBCaregivers
from sqlalchemy import  exc
from db import db
from datetime import datetime, timezone, timedelta

now_utc = datetime.now(timezone.utc)

# delete old emergencies
emergency_cleaned = []
for emergency in db.session.execute(db.select(DBEmergencies).where(DBEmergencies.end_time < now_utc)).scalars().all():
    try:
        db.session.delete(emergency)
        db.session.commit()
        emergency_cleaned.append(emergency.to_dict())
    except Exception as e:
        db.session.rollback()
        print(f"error deleting {emergency.to_dict()}: {e}")
print(f"Old emergencies successfully deleted: {emergency_cleaned}")

# delete caregiver requests with expired tokens
caregivers_cleaned = []
for caregiver in db.session.execute(db.select(DBCaregivers).where(DBCaregivers.authenticated == False, DBCaregivers.date_added < now_utc + timedelta(days=1))).scalars().all():
    try:
        db.session.delete(caregiver)
        db.session.commit()
        caregivers_cleaned.append(caregiver.to_dict())
    except Exception as e:
        db.session.rollback()
        print(f"error deleting {caregiver.to_dict()}: {e}")

print(f"Caregivers successfully deleted due expired tokens: {caregivers_cleaned}")
