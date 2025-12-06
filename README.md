# ing_sw

## Come avviare il progetto
Le opzioni disponibili al momento sono due:
- all: dbms(e db) + server + adminer
- dev_all: dbms(e db) + dev_server (reload automatico quando ci sono nel sorgente) + adminer

docker compose build --profile {all | dev_all} build
docker compose build --profile {all | dev_all} up

se poi si preme 'w' si attiva la modalità watch che rilancia il server ad ogni modifica.
NOTA: è possibile che il server parta prima del DB. Fix: dopo un primo compose up si deve stoppare i container e rifare compose up (così il server partendo riesce a connetersi al db)
# Come accedere ad aminer
accedere dal browser a localhost:8080
selezionare:
System: PostgreSQL
Server: db
Username: postgres
Password: iHateThisProject (al momento questa è la password salvata nel .env XD)
Database: postgres

una volta che si ha effettuato l'accesso è possibile selezionare lo schema ing_sw
