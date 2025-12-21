# ing_sw
## Stato avanzamento
Funzionalità completate 
- Implementata la struttura di navigazione con BottomNavigationView e NavController.
- Realizzato il sistema di login con verifica delle credenziali lato server.
- Implementata l’autenticazione tramite JWT, con memorizzazione del token lato client.
- Implementata la funzionalità di recupero password, con invio della richiesta al backend e gestione della risposta.
- Implementazione richiesta delle guide al backend, salvataggio guide locale per consultazione off line. 
- Visualizzazione delle guide per tutti gli utenti.
- Visualizzazione e modifica profilo utente.
- Gestione dei caregiver da parte degli utenti, compreso invio di mail di conferma al possibile nuovo caregiver.
- Creazione, visualizzazione e modifica profili amministratori.

## Come avviare il progetto
Le opzioni disponibili al momento sono due:
- all: dbms(e db) + server + adminer
- dev_all: dbms(e db) + dev_server (reload automatico quando ci sono nel sorgente) + adminer

docker compose build --profile {all | dev_all} build
docker compose build --profile {all | dev_all} up

se poi si preme 'w' si attiva la modalità watch che rilancia il server ad ogni modifica.
NOTA: è possibile che il server parta prima del DB. Fix: dopo un primo compose up si deve stoppare i container e rifare compose up (così il server partendo riesce a connetersi al db)

## Come accedere ad aminer
Accedere dal browser a localhost:8080 e selezionare:
- System: PostgreSQL
- Server: db
- Username: postgres
- Password: aVerySecurePasswordTustMe (se non funziona controllare il file .env)
- Database: postgres

una volta che si ha effettuato l'accesso è possibile selezionare lo schema ing_sw
