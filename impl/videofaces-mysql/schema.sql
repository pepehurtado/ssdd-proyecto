CREATE SCHEMA IF NOT EXISTS videofaces;
USE videofaces;

CREATE TABLE IF NOT EXISTS users(
	id varchar(50),
       	email varchar(50),
	password_hash text,
       	name text,
	TOKEN text,
	visits int,
	PRIMARY KEY(id)
);

-- Para búsquedas con email
CREATE INDEX user_email_idx ON users (email);

-- CUIDADO!! AÑADO UN USUARIO PARA PROBAR, PASSWORD: "admin"
INSERT INTO users VALUES ("ddd", "dsevilla@um.es", "21232f297a57a5a743894a0e4a801fc3", "diego", "TOKEN", 0);
