from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()
db._engine_options = {"connect_args" : {'options':"--search_path=ing_sw"}}
