CREATE SCHEMA ing_sw;

CREATE TYPE ing_sw.user_status AS ENUM (
  'fine',
  'pending',
  'inDanger'
);

CREATE TABLE ing_sw.Users (
  userId serial PRIMARY KEY,
	caregiverId int DEFAULT NULL,
  email varchar NOT NULL UNIQUE,
  fullName varchar NOT NULL,
	phoneNumber varchar NOT NULL,
	status ing_sw.user_status NOT NULL DEFAULT 'fine',
	status_time timestamp DEFAULT NULL,
	lastLocation point DEFAULT NULL,
	lastLocationTime timestamp DEFAULT NULL,
  isAdmin boolean NOT NULL DEFAULT FALSE,
  password_salt_hex varchar NOT NULL,
  password_digest_hex varchar NOT NULL,

  FOREIGN KEY (caregiverId) REFERENCES ing_sw.Users (userId)
  ON UPDATE CASCADE
	ON DELETE SET NULL
);

CREATE TYPE ing_sw.emergency_type AS ENUM (
  'allagamento',
  'alluvione',
  'grandinata',
  'tromba d''aria',
  'altro'
);

CREATE TABLE ing_sw.Emergencies (
  id serial PRIMARY KEY,
  emergency_type ing_sw.emergency_type NOT NULL DEFAULT 'altro',
  message varchar NOT NULL,
  
  location point NOT NULL,
  radius real NOT NULL,

  start_time timestamp NOT NULL,
  end_time timestamp DEFAULT NULL,

  CHECK(end_time IS NULL OR start_time < end_time)
);

CREATE TABLE ing_sw.Guidelines (
  emergency_type ing_sw.emergency_type PRIMARY KEY,
  message varchar NOT NULL
);
