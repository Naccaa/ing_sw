-- email e password sono uguali
INSERT INTO ing_sw.Users (user_id, email, fullname, phone_number, is_admin, password_salt_hex, password_digest_hex) VALUES
(1, 'admin', 'admin admin', '+39 012 345 6789', TRUE, 'd28a3390f370e1732851012e2832faca', '55f70d0e16369bae7aa74e212af5a664f47626e606dadcc8565c83e714e89ea1e4772b42584493b5dd47968b64d0fd0c823f4114fdbef81cc9657c46bc53997a'),
(2, 'utente0',	'utente0 utente0',	'+39 112 345 6789', FALSE,	'32def600a12da38ee4acde66f138551c',	'5bf5032f1c99506c6f36f0aed6ba4eabfb93c906e03dff38486dac9f56c3fd28ae83367797ca3283dff0517406cc5b55716dc205fad649c92da4352d788038cc'),
(3, 'utente1',	'utente1 utente1',	'+39 212 345 6789', FALSE,	'5b650cbc12f912f7ca060e0a246c8358',	'74e0f216907c2547f7ed781babf149c1a6025c82adb2f95cd14d8ef19c4a7e761a13936dbc004d69f62394653d19dd06bb74fbc5dcf29241417f9ef43db44617'),
(4, 'utente2',	'utente2 utente2',	'+39 312 345 6789', FALSE,	'c6c2c2422023abafe3d8040f8a98a06d',	'c336e6dbfe2689efb43313ae2ef5690ffa38058ce42d980297108bface9dc2d19889bc7451d3aed058be525e65a3b3a296107030efaa2056dcf7fec92356b954');
SELECT setval('ing_sw.users_user_id_seq', (SELECT MAX(user_id) FROM ing_sw.Users));

INSERT INTO ing_sw.Emergencies (emergency_type, message, location, radius, start_time) VALUES
('allagamento', 'test allagamento', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('alluvione', 'test alluvione', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('grandinata', 'test grandinata', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('tromba d''aria', 'test tromba d''aria', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('altro', 'test altro', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('allagamento', 'Allagamento in corso, evitare di uscire di casa.', point(37.4219983, -122.084), 500.0, CURRENT_TIMESTAMP);
SELECT setval('ing_sw.emergencies_id_seq', (SELECT MAX(id) FROM ing_sw.Emergencies));

INSERT INTO ing_sw.Guidelines (emergency_type, message) VALUES
('allagamento', 'All''aperto

Per lo svolgimento di attività nelle vicinanze di un corso d’acqua (anche un semplice pic-nic) o per scegliere l''area per un campeggio: scegli una zona a debita distanza dal letto del torrente e adeguatamente rialzata rispetto al livello del torrente stesso, oltre che sufficientemente distante da pendii ripidi o poco stabili: 
intensi scrosci di pioggia potrebbero attivare improvvisi movimenti del terreno.

In ambiente urbano
Le criticità più tipiche sono legate all’incapacità della rete fognaria di smaltire quantità d’acqua considerevoli che cadono al suolo in tempi ristretti con conseguenti repentini allagamenti di strade. Per questo: fai attenzione al passaggio in sottovia e sottopassi, c’è il rischio di trovarsi con il veicolo semi-sommerso o sommerso dall’acqua;
evita di recarti o soffermarti anche gli ambienti come scantinati, piani bassi, garage, sono a forte rischio allagamento durante intensi scrosci di pioggia.

E in particolare se sei alla guida: 
anche in assenza di allagamenti, l’asfalto reso improvvisamente viscido dalla pioggia rappresenta un insidioso pericolo per chi si trova alla guida di automezzi o motoveicoli, riducendo tanto la tenuta di strada quanto l’efficienza dell’impianto frenante;
limita la velocità o effettua una sosta, in attesa che la fase più intensa, che difficilmente dura più di mezz’ora, del temporale si attenui. È sufficiente pazientare brevemente in un’area di sosta. Durante la fase più intensa di un rovescio risulta infatti fortemente ridotta la visibilità.
'),
('alluvione', 'COSA FARE Durante l''alluvione

Se sei in un luogo chiuso:
Non scendere in cantine, seminterrati o garage per mettere al sicuro i beni: rischi la vita.
Non uscire assolutamente per mettere al sicuro l’automobile.
Se ti trovi in un locale seminterrato o al piano terra, sali ai piani superiori.
Evita l’ascensore: si può bloccare.
Aiuta gli anziani e le persone con disabilità che si trovano nell’edificio.
Chiudi il gas e disattiva l’impianto elettrico.
Non toccare impianti e apparecchi elettrici con mani o piedi bagnati.
Non bere acqua dal rubinetto: potrebbe essere contaminata.
Limita l’uso del cellulare: tenere libere le linee facilita i soccorsi.
Tieniti informato su come evolve la situazione e segui le indicazioni fornite dalle autorità

Se sei all’aperto:
Allontanati dalla zona allagata: per la velocità con cui scorre l’acqua, anche pochi centimetri potrebbero farti cadere.
Raggiungi rapidamente l’area vicina più elevata - o sali ai piani superiori di un edificio - evitando di dirigerti verso pendii o scarpate artificiali che potrebbero franare.
Fai attenzione a dove cammini: potrebbero esserci voragini, buche, tombini aperti ecc.
Evita di utilizzare l’automobile. Anche pochi centimetri d’acqua potrebbero farti perdere il controllo del veicolo o causarne lo spegnimento: rischi di rimanere intrappolato.
Evita sottopassi, argini, ponti: sostare o transitare in questi luoghi può essere molto pericoloso.
Limita l’uso del cellulare: tenere libere le linee facilita i soccorsi.
Tieniti informato su come evolve la situazione e segui le indicazioni fornite dalle autorità.
'),
('grandinata', 'In caso di grandine, valgono le avvertenze per la viabilità già viste per i rovesci di pioggia, riguardo alle conseguenze sullo stato scivoloso del manto stradale e sulle forti riduzioni di visibilità. La durata di una grandinata è tipicamente piuttosto breve.
'),
('tromba d''aria', 'All’aperto

- Allontanati rapidamente dalla costa verso zone più elevate e trova riparo in un edificio.
- Se sei in auto poni particolare attenzione perché le raffiche di vento potrebbero far sbandare il veicolo. Rallenta e raggiungi il luogo sicuro più vicino – preferibilmente un edificio in muratura – evitando di sostare sotto ponti, cavalcavia, strutture e oggetti che potrebbero cadere (come lampioni, impalcature, etc.) .
- Sono possibili anche distacchi di cavi elettrici. Se sei in auto e vieni colpito rimani all’interno del veicolo e attendi i soccorsi.
- Limita l’uso del cellulare. Tenere libere le linee facilita i soccorsi.

In casa

- Non uscire assolutamente, neanche per mettere in sicurezza beni o veicoli.
- Chiudi porte, finestre e imposte.
- Riparati nella stanza più interna della casa o in corridoio, il più lontano possibile da porte e finestre.
- Abbandona i piani seminterrati e i piani terra e portati ai piani alti.
- Se possibile evita di ripararti all’ultimo piano. Le forti raffiche di vento potrebbero danneggiare i tetti degli edifici più vulnerabili.
- Se possibile poni ulteriori protezioni davanti a finestre e vetrate.
- Fai entrare in casa gli animali domestici.
- Chiudi il gas e disattiva il quadro elettrico se gli impianti sono ai piani bassi.
- Se vivi in una casa mobile (roulotte, prefabbricato, campeggio) cerca riparo in un edificio sicuro.
- Tieni a portata di mano: documenti, farmaci indispensabili, batterie, torcia elettrica, radio a pile, cellulare, acqua in bottiglia.
- Limita l’uso del cellulare. Tenere libere le linee facilita i soccorsi.
- Anche se il fenomeno ti sembra in attenuazione non uscire di casa ma attendi le indicazioni delle autorità.');

INSERT INTO ing_sw.Caregivers (email, phone_number, alias, user_id, authenticated, auth_code, date_added) VALUES 
('dmuweuwthghcmqsgju@nespf.com', '+39 412 345 6789', 'utente1.caregiver1', 3, TRUE, '3I8MUNZFGGY7Y7LWPRUZONL9QZKZZ5IE', '2026-01-23 14:20:31.099829'),
('okuhzzvrcavjqdfntn@nespf.com', '+39 512 345 6789', 'utente2.caregiver1', 4, TRUE, 'GFCR28YLTPVQR3NY6K8KAKEJM4LV1KNT', '2026-01-23 12:25:31.999296'),
('xydijwkemtvjkcsnji@nespf.com', '+39 612 345 6789', 'utente2.caregiver2', 4, TRUE, 'GR72EIHPNVNKII4R5YU2IZW7KD4OB4IU', '2026-01-23 14:15:58.189078');
SELECT setval('ing_sw.caregivers_caregiver_id_seq', (SELECT MAX(caregiver_id) FROM ing_sw.Caregivers));
