CREATE SCHEMA ing_sw;

CREATE TYPE ing_sw.status AS ENUM (
  'fine',
  'pending',
  'inDanger'
);

/*
*/
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

/*
INSERT: il nome deve essere unico
UPDATE: il nome deve essere unico
DELETE: non deve avere voli (e quindi aerei) previsti per oggi o il futuro

TRIGGER ON DELETE (ha voli || ha aerei || attiva) -> non fare delete
*/
CREATE TABLE taw_bd.Airlines (
  name varchar NOT NULL UNIQUE CHECK(name <> ''),
  airline_id serial PRIMARY KEY,
  email varchar NOT NULL UNIQUE CHECK(email <> ''),
  salt varchar NOT NULL,
  hash varchar NOT NULL,
  extra_baggage_cost real NOT NULL CHECK(extra_baggage_cost > 0),
  extra_space_cost real NOT NULL CHECK(extra_space_cost > 0),
  isActive boolean DEFAULT TRUE
);

/*
INSERT: non può avere 0 posti totali (CHECK)
UPDATE: non si può cambiare un modello se ha aerei di questo tipo presenti nella base di dati
        (permette le modifiche di errori di battitura in creazione, ma una volta che sono stati inseriti aerei così si dà per scontato
        che il modello sia corretto)
TRIGGER ON UPDATE (esistono aerei di quel modello) -> non fare l'update

DELETE: non deve avere ancora aerei di questo modello presenti nella base di dati
TRIGGER ON DELETE (esistono aerrei di quel di quel modello) -> non fare la delete

*/
CREATE TABLE taw_bd.AirplaneModels (
  model_id varchar PRIMARY KEY,
  economy_rows int NOT NULL DEFAULT 0 CHECK(economy_rows >= 0),
  economy_columns int NOT NULL DEFAULT 0 CHECK(economy_columns IN (0,2,3)),
  business_rows int NOT NULL DEFAULT 0 CHECK(business_rows >= 0),
  business_columns int NOT NULL DEFAULT 0 CHECK(business_columns IN (0,2,3)),
  firstclass_rows int NOT NULL DEFAULT 0 CHECK(firstclass_rows >= 0),
  firstclass_columns int NOT NULL DEFAULT 0 CHECK(firstclass_columns IN (0,2,3)),

  -- if economy_columns > 0 then economy_rows > 0
  CHECK(economy_rows <=0 OR economy_columns > 0),
  -- if economy_rows > 0 then economy_columns > 0
  CHECK(economy_columns <=0 OR economy_rows > 0),

  -- if business_columns > 0 then business_rows > 0
  CHECK(business_rows <=0 OR business_columns > 0),
  -- if business_rows > 0 then business_columns > 0
  CHECK(business_columns <=0 OR business_rows > 0),

  -- if firstclass_columns > 0 then firstclass_rows > 0
  CHECK(firstclass_rows <=0 OR firstclass_columns > 0),
  -- if firstclass_rows > 0 then firstclass_columns > 0
  CHECK(firstclass_columns <=0 OR firstclass_rows > 0),

  CHECK(economy_columns*economy_rows + business_columns*business_rows + firstclass_columns*firstclass_rows > 0)
);

/*
INSERT:
UPDATE:
TRIGGER ON UPDATE (non esistono voli di quell'aereo) - non fare update

DELETE: non deve avere voli previsti su questo aereo oggi o in futuro (prima bisogna annullare i voli o cambiare aereo)
TRIGGER ON DELETE (esistono voli di quell'aereo) - non fare delete

*/
CREATE TABLE taw_bd.Airplanes (
  serial_number varchar PRIMARY KEY,
  model_id varchar NOT NULL,
  airline_id int NOT NULL, -- non si può avere un aereo di nessuno, deve essere di qualche compagnia

  -- ADD CONSTRAINT has
  FOREIGN KEY (model_id) REFERENCES taw_bd.airplaneModels (model_id)
  ON UPDATE CASCADE -- non dovrebbe mai capitare, ma se mai si dovesse cambiare il nome del modello allora si aggiornano da soli
  ON DELETE NO ACTION, -- così si può eliminare un modello solo se non ci sono aerei di quel modello ancora presenti nella base di dati
  -- ADD CONSTRAINT owned_by
  FOREIGN KEY (airline_id) REFERENCES taw_bd.Airlines (airline_id)
  ON UPDATE CASCADE
  ON DELETE NO ACTION -- così si può eliminare una compagnia aerea solo se non ci sono più suoi aerei nella base di dati
);

/*
INSERT: la data deve essere oggi o in futuro CHECK

UPDATE: non si possono cambiare se non cambiare lo status dell'acquisto
TRIGGER (controllo valori vecchi e valori nuovi)

DELETE: (andranno riportati i biglietti allo stato di 'available' e tolti i passeggeri dai biglietti)
TRIGGER (vuoto || passata durata di cleanup) -> effettua delete

*/
CREATE TABLE taw_bd.Purchases (
  purchase_id serial PRIMARY KEY,
  purchase_date timestamp NOT NULL CHECK(purchase_date >= CURRENT_DATE),
  payment_method varchar NOT NULL,
  buyer_id int NOT NULL,

  -- ADD CONSTRAINT made_by
  FOREIGN KEY (buyer_id) REFERENCES taw_bd.Users (user_id)
  ON UPDATE CASCADE 
  ON DELETE CASCADE -- permette di eliminare un profilo se e solo se non ha voli ancora da prendere (da usare un trigger)
);

/*
INSERT: il giorno di partenza deve essere oggi o in futuro e non può atterrare prima di essere partito, gli aeroporti di partenza e arrivo devono essere diversi
CHECK
UPDATE: il giorno di partenza deve essere oggi o in futuro e non può atterrare prima di essere partito, gli aeroporti di partenza e arrivo devono essere diversi
CHECK

DELETE: (bisogna annullare tutti i biglietti)
TRIGGER ON DELETE (passato tempo limite) -> delete

*/
CREATE TABLE taw_bd.Flights (
  flight_id varchar(6) PRIMARY KEY,
  departure_airport varchar NOT NULL,
  arrival_airport varchar NOT NULL,
  departure_date timestamp NOT NULL CHECK(departure_date > CURRENT_TIMESTAMP),
  arrival_date timestamp NOT NULL CHECK(arrival_date > CURRENT_TIMESTAMP),
  airline_id int NOT NULL,
  airplane_serial_number varchar NOT NULL,
  cancelled boolean DEFAULT FALSE,
  economy_price real NOT NULL CHECK(economy_price > 0),
  business_price real NOT NULL CHECK(business_price > 0),
  firstclass_price real NOT NULL CHECK(firstclass_price > 0),

  -- ADD CONSTRAINT flown_by
  FOREIGN KEY (airplane_serial_number) REFERENCES taw_bd.airplanes (serial_number)
  ON UPDATE CASCADE
  ON DELETE NO ACTION,
  -- ADD CONSTRAINT operated_by
  FOREIGN KEY (airline_id) REFERENCES taw_bd.Airlines (airline_id)
  ON UPDATE CASCADE
  ON DELETE NO ACTION,

  CHECK(departure_airport <> arrival_airport),
  CHECK(departure_date < arrival_date)
);

/*
INSERT: costo > 0, extra_baggages >= 0 check

UPDATE: si può aggiornare solo passenger_name, purchase_id e status trigger (status) -> disponibile -> purchased
                                                                            (status purchase_id == null && passenger_name = null) purchased -> available
tramite trigger

DELETE: (eliminabile solo se viene cancellato il volo)
tramite trigger
*/
CREATE TABLE taw_bd.Tickets (
  ticket_id serial PRIMARY KEY,
  seat varchar(3) NOT NULL,
  cost real NOT NULL CHECK(cost > 0),
  passenger_class taw_bd.passenger_class NOT NULL,
  extra_baggages int DEFAULT 0 NOT NULL CHECK(extra_baggages >= 0),
  extra_space bool DEFAULT FALSE NOT NULL,
  passenger_name varchar DEFAULT NULL,
  status taw_bd.ticket_status DEFAULT 'available' NOT NULL,
  purchase_id int DEFAULT NULL,
  flight_id varchar(6) NOT NULL,
  checked_in boolean DEFAULT FALSE NOT NULL,

  -- ADD CONSTRAINT includes
  FOREIGN KEY (purchase_id) REFERENCES taw_bd.Purchases (purchase_id)
  ON UPDATE CASCADE
  ON DELETE SET NULL,

  -- ADD CONSTRAINT referenced_by
  FOREIGN KEY (flight_id) REFERENCES taw_bd.Flights (flight_id)
  ON UPDATE CASCADE -- NON DOVREBBE CAPITARE
  ON DELETE CASCADE
);

