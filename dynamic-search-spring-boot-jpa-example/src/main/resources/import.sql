-- Script d'initialisation simple (désactivé par défaut)
-- Pour générer 1 000 000 d'enregistrements, utilisez DataInitializer.java
-- Activer avec: app.data.init.enabled=true dans application.properties

-- Quelques exemples pour tester rapidement sans attendre le chargement de 1M
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('Windows', '11 Pro', 'NT 10.0', '2021-10-05T10:00:00', 1500000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('Ubuntu', '24.04 LTS', 'Linux 6.8', '2024-04-25T10:00:00', 890000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('Debian', '12 Bookworm', 'Linux 6.1', '2023-06-10T10:00:00', 720000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('Fedora', '40', 'Linux 6.8', '2024-04-23T10:00:00', 280000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('macOS', '15 Sequoia', 'Darwin 24.0', '2024-09-16T10:00:00', 580000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('Android', '15', 'Linux 6.6', '2024-10-15T10:00:00', 5200000);
INSERT INTO operating_system (name, version, kernel, release_date, usages) VALUES ('iOS', '18.1', 'Darwin 24.1', '2024-10-28T10:00:00', 3800000);
