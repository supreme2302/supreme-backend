CREATE EXTENSION IF NOT EXISTS citext;
DROP TABLE IF EXISTS SPRING_SESSION CASCADE ;
DROP TABLE IF EXISTS SPRING_SESSION_ATTRIBUTES CASCADE ;
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
  PRIMARY_ID CHAR(36) NOT NULL,
  SESSION_ID CHAR(36) NOT NULL,
  CREATION_TIME BIGINT NOT NULL,
  LAST_ACCESS_TIME BIGINT NOT NULL,
  MAX_INACTIVE_INTERVAL INT NOT NULL,
  EXPIRY_TIME BIGINT NOT NULL,
  PRINCIPAL_NAME VARCHAR(100),
  CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

DROP INDEX IF EXISTS SPRING_SESSION_IX1;
DROP INDEX IF EXISTS SPRING_SESSION_IX2;
DROP INDEX IF EXISTS SPRING_SESSION_IX3;

CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
  SESSION_PRIMARY_ID CHAR(36) NOT NULL,
  ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
  ATTRIBUTE_BYTES BYTEA NOT NULL,
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

DROP INDEX IF EXISTS SPRING_SESSION_ATTRIBUTES_IX1;

CREATE INDEX IF NOT EXISTS SPRING_SESSION_ATTRIBUTES_IX1 ON SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID);


drop table IF exists "auth" cascade ;
drop table if exists "comment" cascade ;
drop table if exists "comment_counter" cascade ;
drop table if exists "profile" cascade ;
drop table if exists "profile_skill" cascade ;
drop table if exists "skill" cascade ;
drop table if exists "message" cascade ;
drop table if exists "genre" cascade ;
drop table if exists "profile_genre" cascade ;

CREATE TABLE IF NOT EXISTS "auth" (
  id SERIAL NOT NULL PRIMARY KEY ,
  email CITEXT NOT NULL UNIQUE ,
  username CITEXT NOT NULL UNIQUE,
  password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "comment" (
  id SERIAL NOT NULL PRIMARY KEY,
  to_user_id INTEGER NOT NULL REFERENCES auth(id),
  from_username CITEXT NOT NULL REFERENCES auth(username),
  from_email CITEXT NOT NULL REFERENCES auth(email),
  comment_val CITEXT NOT NULL,
  rating INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS "comment_counter" (
  id SERIAL NOT NULL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES auth(id),
  counter INTEGER DEFAULT 0,
  sum_rating REAL DEFAULT 0
);


CREATE TABLE IF NOT EXISTS "profile" (
  id SERIAL NOT NULL PRIMARY KEY ,
  user_id INTEGER REFERENCES auth(id),
  phone CITEXT DEFAULT '',
  onpage BOOLEAN DEFAULT FALSE,
  about CITEXT default '',
  rating REAL DEFAULT 0,
  avatar TEXT DEFAULT 'peenge.png'
);

CREATE TABLE IF NOT EXISTS "skill" (
  id SERIAL NOT NULL PRIMARY KEY ,
  skill_name CITEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "profile_skill" (
  id SERIAL NOT NULL PRIMARY KEY ,
  profile_id INTEGER NOT NULL ,
  skill_id INTEGER NOT NULL,
  FOREIGN KEY (profile_id) REFERENCES profile(id),
  FOREIGN KEY (skill_id) REFERENCES skill(id)
);

CREATE TABLE IF NOT EXISTS "genre" (
  id SERIAL NOT NULL PRIMARY KEY ,
  genre_name CITEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "profile_genre" (
  id SERIAL NOT NULL  PRIMARY KEY ,
  profile_id INTEGER NOT NULL ,
  genre_id INTEGER NOT NULL ,
  FOREIGN KEY (profile_id) REFERENCES profile(id),
  FOREIGN KEY (genre_id) REFERENCES genre(id)
);


CREATE TABLE IF NOT EXISTS "message" (
  id SERIAL NOT NULL PRIMARY KEY ,
  content CITEXT NOT NULL,
  recipient INTEGER REFERENCES auth(id),
  sender INTEGER REFERENCES auth(id),
  message_date TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS "picture" (
  id SERIAL NOT NULL PRIMARY KEY ,
  link CITEXT DEFAULT 'default.jpg',
  client_id INTEGER REFERENCES auth(id)
);

INSERT INTO skill (skill_name) VALUES ('drums');
INSERT INTO skill (skill_name) VALUES ('guitar');
INSERT INTO skill (skill_name) VALUES ('bass');
INSERT INTO skill (skill_name) VALUES ('keyboards');

INSERT INTO genre (genre_name) VALUES ('pop');
INSERT INTO genre (genre_name) VALUES ('rock');
INSERT INTO genre (genre_name) VALUES ('jazz');
INSERT INTO genre (genre_name) VALUES ('metal');

INSERT INTO auth (email, username, password) VALUES ('exist@e.ru', 'exist', '$2a$10$Rneet0FPQZ7jZYit4soJz.n85xEh.0IFn7B24AIBK4OpGVi/oQaHq');
INSERT INTO profile (user_id) VALUES (1);
INSERT INTO comment_counter (user_id) VALUES (1);
INSERT INTO auth (email, username, password) VALUES ('exist3@e.ru', 'userWithProfileAndSkillsAndGenres', '$2a$10$Rneet0FPQZ7jZYit4soJz.n85xEh.0IFn7B24AIBK4OpGVi/oQaHq');
INSERT INTO profile (user_id) VALUES (2);
INSERT INTO comment_counter (user_id) VALUES (2);
UPDATE profile SET phone = '9153456789', about = 'bio', onpage = true, rating = 0;
INSERT INTO profile_skill (profile_id, skill_id) VALUES (2, 1);
INSERT INTO profile_skill (profile_id, skill_id) VALUES (2, 3);
INSERT INTO profile_genre (profile_id, genre_id) VALUES (2, 2);
INSERT INTO profile_genre (profile_id, genre_id) VALUES (2, 3);



