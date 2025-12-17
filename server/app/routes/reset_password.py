from flask import Blueprint, request, render_template, redirect, url_for
from db import db
import sqlalchemy as sq
from src.db_types import DBUser, DBPasswordResetTokens
from datetime import datetime, timedelta
import secrets
import string
import smtplib
from email.message import EmailMessage
from os import getenv
import hashlib


def generate_reset_token(length=32):
    """
    Genera un token sicuro per il reset password.
    length: lunghezza del token
    """
    alphabet = string.ascii_letters + string.digits
    token = ''.join(secrets.choice(alphabet) for _ in range(length))
    return token

reset_bp = Blueprint('reset', __name__)


@reset_bp.route("/reset_password", methods=["GET", "POST"])
def reset_password():
    token = request.args.get("token")
    message = ""

    if request.method == "POST":
        new_password = request.form.get("newPassword")
        confirm_password = request.form.get("confirmPassword")
        if new_password != confirm_password:
            message = "Le password non coincidono"

        # Controllo se il token è nel DB
        try:
            found_token = db.session.execute(sq.select(DBPasswordResetTokens).filter(DBPasswordResetTokens.token == token)).scalar_one_or_none()
            if found_token == None:
                return {"error" : True, "message" : "Token not valid."}, 400
            id = int(found_token.user_id)
        except:
            return {"error" : True, "message" : "Server error."}, 500
        
        # Controllo se il token è ancora valido (potrebbe essere usato oppure scaduto)
        if found_token.used == True:
            return {"error" : True, "message" : "Token not valid."}, 400
        if found_token.expires_at > datetime.now():
            return {"error" : True, "message" : "Token not valid."}, 400

        # Aggiorno la password
        user = DBUser.query.get(id)
        if not user:
            return {"error": True, "message": "User not found"}, 404

    
        user.set_password(new_password)

        # Setto il token come usato
        found_token.used = True

        try:
            db.session.commit()
        except Exception as e:
            print(e)
            db.session.rollback()
            return {"error": True, "message": "Server error"}, 500
        
        message = "Password aggiornata con successo!"
        return render_template("reset_password.html", token="", message=message)

    # Nel caso della GET servo la pagina web minimale per il reset della password
    return render_template("reset_password.html", token=token, message=message)


'''
Questo endpoint viene utilizzato quando l'utente dell'app vuole resettare
la sua password
'''
@reset_bp.route("/forgot_password", methods=["POST"])
def forgot_password():
    data = request.get_json()
    email = data.get('email', '').lstrip().rstrip().lower()

    try:
        found_user = db.session.execute(sq.select(DBUser).filter(DBUser.email == email)).scalar_one_or_none()
        if found_user == None:
            return {"error" : False, "message" : "Mail inviata"}, 200
        id = int(found_user.user_id)
    except:
        return {"error" : False, "message" : "Mail inviata"}, 200
    
    # Genero token casuale
    token = generate_reset_token()

    # Aggiungo il token al database
    current_timestamp = datetime.now()
    new_token = DBPasswordResetTokens(token, id, current_timestamp, current_timestamp + timedelta(minutes=10), False)

    try:
      db.session.add(new_token)
      db.session.commit()
    except Exception as e:
      return {"error" : True, "message": "Server error"}, 500
    
    # Invio mail all'utente
    source = getenv('SENDER_MAIL')
    password = getenv('MAIL_PASSWORD')

    url = f"http://localhost:5000/reset_password?token={token}"  # Replace with actual URL   
    msg = EmailMessage()
    msg['Subject'] = "Link per il reset della password"
    msg['From'] = source
    msg['To'] = email

    msg.set_content(f"""
    Salve,
    Clicchi sul seguente link per resettare la sua password: 
    {url}
    Il link resterà valido per 10 minuti.
    Qualora non abbia richiesto un cambio di password ignori pure questa mail.
    """)
    try:
        with smtplib.SMTP('smtp.gmail.com', 587) as server:
            server.starttls()  
            server.login(source, password)
            server.send_message(msg)
    except Exception as e:
        print(f"Error sending email: {e}")

    # Terminazione con successo
    return {"error" : False, "message" : "Mail inviata"}, 200