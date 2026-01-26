# ing_sw

![icon](android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

## ATTENZIONE!
Il progetto NON viene fornito con i file di configurazione dell'ambiente completi e non vengono date le credenziali usate per questioni di sicurezza e di privacy.  
Sarà necessario creare autonomamente quanto riguarda le credenziali necessarie per il DBMS, l'indirizzo mail che invierà le email agli utenti, i secret per i JWT o password e permessi per progetti Firebase.

## Applicazione Android
Scaricare sul proprio dispositivo (o emulatore) Android l'apk fornito.  
Per garantire il corretto funzionamento di tutte le funzionalità offerte sarà necessario fornire i permessi:
- per mandare SMS;
- per accedere alla posizione (assicurarsi di mantenerla sempre attiva);
- per ricevere le notifiche.

Una volta confermate tutti i permessi, nella sezione apposita sempre accessibile in alto a destra, sarà necessario impostare l'indirizzo ip della macchina che ospiterà il backend (vanno bene sia dispositivi personali che servizi terzi).    
Qualora si stia usando un dispositivo personale connesso ad una rete locale allora il backend sarà accessibile solamente all'interno della rete stessa.

## Come avviare il backend
### TLDR
Per avviare i container per la gestione completa del DBMS (e setup del database), del backend server e di adminer eseguire il seguente comando:  
(sudo) docker compose build --profile dev_all build  
(sudo) docker compose build --profile dev_all up
### Descrizione completa
Le opzioni disponibili al momento sono due:
- all: dbms(e db) + server + adminer
- dev_all: dbms(e db) + dev_server (reload automatico quando ci sono nel sorgente) + adminer

(sudo) docker compose build --profile {all | dev_all} build  
(sudo) docker compose build --profile {all | dev_all} up  

È possibile attivare la modalità watch premendo il tasto 'w' nel terminale in cui si ha eseguito il backend. Questa modalità che rilancia il server ad ogni modifica.

## Come accedere ad aminer
Accedere dal browser a localhost:8080 e selezionare:
- System: PostgreSQL
- Server: db
- Username: postgres
- Password: aVerySecurePasswordTustMe (se non funziona controllare il file .env)
- Database: postgres

una volta che si ha effettuato l'accesso è possibile selezionare lo schema ing_sw

## Come usare firebase
Sono state seguite le istruzioni su https://firebase.google.com/docs/admin/setup?authuser=0&hl=it
1. Accedere a https://console.firebase.google.com/u/0/project/ing-sw-636e2/settings/serviceaccounts/adminsdk con una mail autorizzata, se il link non funziona provare a sostituire lo 0 con la posizione con cui l'account è salvato sul browser. Dovrebbe comparire una pagina con titolo "Impostazioni progetto", aperta nella sezione "Account di servizio"
2. Cliccare "Genera nuova chiave privata".
3. Rinominare il file in "firebase-adminsdk.json" e spostarlo in "server/app/firebase-adminsdk.json", assicurarsi che sia ignorato da git.
