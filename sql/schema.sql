CREATE SCHEMA ing_sw;

CREATE TYPE ing_sw.user_status AS ENUM (
  'fine',
  'pending',
  'inDanger'
);

CREATE TABLE ing_sw.Users (
  user_id serial PRIMARY KEY,
	-- caregiver_id int DEFAULT NULL,
  email varchar NOT NULL UNIQUE,
  fullname varchar NOT NULL,
	phone_number varchar NOT NULL,
	status ing_sw.user_status NOT NULL DEFAULT 'fine',
	status_time timestamp DEFAULT NULL,
	last_location point DEFAULT NULL,
	last_location_time timestamp DEFAULT NULL,
  is_admin boolean NOT NULL DEFAULT FALSE,
  password_salt_hex varchar NOT NULL,
  password_digest_hex varchar NOT NULL

  -- FOREIGN KEY (caregiver_id) REFERENCES ing_sw.Users (user_id)
  -- ON UPDATE CASCADE
	-- ON DELETE SET NULL
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

-- Table containing all the caregivers
CREATE TABLE ing_sw.Caregivers (
  caregiver_id serial PRIMARY KEY,
  email varchar NOT NULL,
  phone_number varchar NOT NULL,
  alias varchar NOT NULL,
  user_id integer NOT NULL,
  authenticated boolean NOT NULL DEFAULT FALSE, 

  FOREIGN KEY (user_id) REFERENCES ing_sw.Users (user_id)
  ON UPDATE CASCADE
	ON DELETE SET NULL
);