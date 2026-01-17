# ing_sw

![icon](android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

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

Momentaneamente il progetto sta venendo sviluppato su tre git branches differenti per lavorare su feature diverse. Il main branch non è quindi al momento completo, ma tutte le feature elencate sono divise tra i branch a seconda delle loro dipendenze.
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

## Come usare firebase
Ho seguito le istruzioni su https://firebase.google.com/docs/admin/setup?authuser=0&hl=it
1. Accedere a https://console.firebase.google.com/u/0/project/ing-sw-636e2/settings/serviceaccounts/adminsdk con una mail autorizzata, se il link non funziona provare a sostituire lo 0 con la posizione con cui l'account è salvato sul browser. Dovrebbe comparire una pagina con titolo "Impostazioni progetto", aperta nella sezione "Account di servizio"
2. Cliccare "Genera nuova chiave privata".
3. Rinominare il file in "firebase-adminsdk.json" e spostarlo in "server/app/firebase-adminsdk.json", assicurarsi che sia ignorato da git.
