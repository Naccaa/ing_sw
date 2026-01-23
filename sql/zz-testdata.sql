INSERT INTO ing_sw.Users (email, fullname, phone_number, is_admin, password_salt_hex, password_digest_hex) VALUES
('admin', 'admin admin', '1', TRUE, 'd28a3390f370e1732851012e2832faca', '55f70d0e16369bae7aa74e212af5a664f47626e606dadcc8565c83e714e89ea1e4772b42584493b5dd47968b64d0fd0c823f4114fdbef81cc9657c46bc53997a');

INSERT INTO ing_sw.Guidelines (emergency_type, message) VALUES
('allagamento', 'Rimanere in casa, evitare di uscire. Se necessario evacuare allora dirigersi verso zone più elevate.'),
('alluvione', 'Allontanarsi da fiumi, torrenti e corsi d''acqua. Raggiungere immediatamente zone più alte e rimanere lontano dai ponti.'),
('grandinata', 'Cercare riparo in casa o in un luogo al coperto. Non uscire fino al termine del fenomeno meteorologico.'),
('tromba d''aria', 'Cercare riparo in un luogo chiuso e protetto. Allontanarsi da finestre e rimanere lontano da edifici alti se all''aperto.');


INSERT INTO ing_sw.Emergencies (emergency_type, message, location, radius, start_time) VALUES
('allagamento', 'test allagamento', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('alluvione', 'test alluvione', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('grandinata', 'test grandinata', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('tromba d''aria', 'test tromba d''aria', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('altro', 'test altro', point(37.4219983, -122.084), 999999.0, CURRENT_TIMESTAMP),
('allagamento', 'Allagamento in corso, evitare di uscire di casa.', point(37.4219983, -122.084), 500.0, CURRENT_TIMESTAMP);
