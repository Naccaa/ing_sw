CREATE SCHEMA ing_sw;

CREATE TYPE ing_sw.user_roles AS ENUM (
  'cittadino',
  'admin',
);

CREATE TYPE ing_sw.user_health AS ENUM (
  "al sicuro",
  "in pericolo"
);

CREATE TYPE ing_sw.user_status AS (
  health ing_sw.user_health,
  location point,
  time timestampz
);

CREATE TABLE ing_sw.Users (
  email varchar PRIMARY KEY,
  caregiver_email varchar DEFAULT NULL,

  role ing_sw.user_roles NOT NULL,
  fullname varchar NOT NULL,
  phone_number varchar NOT NULL,
  last_status ing_sw.user_status DEFAULT NULL,

  password_salt_hex varchar NOT NULL,
  password_digest_hex varchar NOT NULL

  FOREIGN KEY (caregiver_email) REFERENCES ing_sw.Users (email)
  ON UPDATE CASCADE
	ON DELETE SET NULL
);

CREATE TYPE ing_sw.emergency_type AS ENUM (
  "allagamento",
  "alluvione",
  "grandinata",
  "tromba d'aria",
  "alro"
);

CREATE TABLE ing_sw.Emergencies (
  id serial PRIMARY KEY,
  emergency_type ing_sw.emergency_type NOT NULL,
  message varchar NOT NULL,
  
  location point NOT NULL,
  radius double NOT NULL,

  start_time timestampz NOT NULL,
  end_time timestampz DEFAULT NULL
);

CREATE TABLE ing_sw.Guidelines (
  emergency_type ing_sw.emergency_type PRIMARY KEY,
  message varchar NOT NULL
);




/* altra proposta semplificata per gli user
CREATE TYPE ing_sw.status AS ENUM (
  'fine',
  'pending',
  'inDanger'
);

CREATE TABLE taw_bd.Users (
  userId serial PRIMARY KEY,
  email varchar NOT NULL UNIQUE,
  fullName varchar NOT NULL,
	phoneNumber varchar NOT NULL,
	status ing_sw.status NOT NULL DEFAULT 'fine',
	caregiver serial DEFAULT NULL,
	-- lastLocation ??
  isAdmin boolean NOT NULL DEFAULT FALSE,
  salt varchar NOT NULL,
  hash varchar NOT NULL,

  FOREIGN KEY (caregiver) REFERENCES ing_sw.Users (userId)
  ON UPDATE CASCADE
	ON DELETE SET NULL
);
*/
