CREATE EXTENSION IF NOT EXISTS citext;

drop table IF exists "auth" cascade ;
drop table if exists "comment" cascade ;
drop table if exists "comment_counter" cascade ;
drop table if exists "profile" cascade ;
drop table if exists "profile_skill" cascade ;
drop table if exists "skill" cascade ;
drop table if exists "message" cascade ;
drop table if exists "genre" cascade ;
drop table if exists "profile_genre" cascade ;
drop table if exists "picture" cascade ;

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
