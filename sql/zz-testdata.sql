-- email e password sono uguali
INSERT INTO ing_sw.Users (user_id, email, fullname, phone_number, is_admin, password_salt_hex, password_digest_hex) VALUES
(1, 'admin', 'admin admin', '+39 012 345 6789', TRUE, 'd28a3390f370e1732851012e2832faca', '55f70d0e16369bae7aa74e212af5a664f47626e606dadcc8565c83e714e89ea1e4772b42584493b5dd47968b64d0fd0c823f4114fdbef81cc9657c46bc53997a'),
(2, 'utente0',	'utente0 utente0',	'+39 112 345 6789', FALSE,	'32def600a12da38ee4acde66f138551c',	'5bf5032f1c99506c6f36f0aed6ba4eabfb93c906e03dff38486dac9f56c3fd28ae83367797ca3283dff0517406cc5b55716dc205fad649c92da4352d788038cc'),
(3, 'utente1',	'utente1 utente1',	'+39 212 345 6789', FALSE,	'5b650cbc12f912f7ca060e0a246c8358',	'74e0f216907c2547f7ed781babf149c1a6025c82adb2f95cd14d8ef19c4a7e761a13936dbc004d69f62394653d19dd06bb74fbc5dcf29241417f9ef43db44617'),
(4, 'utente2',	'utente2 utente2',	'+39 312 345 6789', FALSE,	'c6c2c2422023abafe3d8040f8a98a06d',	'c336e6dbfe2689efb43313ae2ef5690ffa38058ce42d980297108bface9dc2d19889bc7451d3aed058be525e65a3b3a296107030efaa2056dcf7fec92356b954');

INSERT INTO ing_sw.Emergencies (emergency_type, message, location, radius, start_time) VALUES
('allagamento', 'test allagamento', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('alluvione', 'test alluvione', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('grandinata', 'test grandinata', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('tromba d''aria', 'test tromba d''aria', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('altro', 'test altro', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('allagamento', 'Allagamento in corso, evitare di uscire di casa.', point(37.4219983, -122.084), 500.0, CURRENT_TIMESTAMP);

INSERT INTO ing_sw.Guidelines (emergency_type, message) VALUES
('allagamento', 'Rimanere in casa, evitare di uscire. Se è necessario evacuare allora dirigersi verso zone più elevate.'),
('alluvione', 'Allontanarsi da fiumi, torrenti e corsi d''acqua. Raggiungere immediatamente zone più alte e rimanere lontano dai ponti.'),
('grandinata', 'Cercare riparo in casa o in un luogo al coperto. Non uscire fino al termine del fenomeno meteorologico.'),
('tromba d''aria', 'Cercare riparo in un luogo chiuso e protetto. Allontanarsi da finestre e rimanere lontano da edifici alti se all''aperto.');

INSERT INTO ing_sw.Caregivers (email, phone_number, alias, user_id, authenticated, auth_code, date_added) VALUES 
('dmuweuwthghcmqsgju@nespf.com', '+39 412 345 6789', 'utente1.caregiver1', 3, TRUE, '3I8MUNZFGGY7Y7LWPRUZONL9QZKZZ5IE', '2026-01-23 14:20:31.099829'),
('okuhzzvrcavjqdfntn@nespf.com', '+39 512 345 6789', 'utente2.caregiver1', 4, TRUE, 'GFCR28YLTPVQR3NY6K8KAKEJM4LV1KNT', '2026-01-23 12:25:31.999296'),
('xydijwkemtvjkcsnji@nespf.com', '+39 612 345 6789', 'utente2.caregiver2', 4, TRUE, 'GR72EIHPNVNKII4R5YU2IZW7KD4OB4IU', '2026-01-23 14:15:58.189078');

