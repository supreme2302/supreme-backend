CREATE EXTENSION IF NOT EXISTS citext;

drop table IF exists "auth" cascade ;
drop table if exists "profile" cascade ;
drop table if exists "profile_skill" cascade ;
drop table if exists "skill" cascade ;
drop table if exists "message" cascade ;

CREATE TABLE IF NOT EXISTS "auth" (
  id SERIAL NOT NULL PRIMARY KEY ,
  email CITEXT NOT NULL UNIQUE ,
  username CITEXT NOT NULL UNIQUE,
  password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "profile" (
  id SERIAL NOT NULL PRIMARY KEY ,
  user_id INTEGER REFERENCES auth(id),
  phone CITEXT DEFAULT '',
  onpage BOOLEAN DEFAULT FALSE,
  about CITEXT default '',
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





CREATE TABLE IF NOT EXISTS "message" (
  id SERIAL NOT NULL PRIMARY KEY ,
  content CITEXT NOT NULL,
  recipient citext REFERENCES auth(email),
  sender citext REFERENCES auth(email),
  message_date TIMESTAMP WITH TIME ZONE
);


insert into auth (email, username, password) values ('a@a.ru', 'aaa', '123');
insert into auth (email, username, password) values ('b@b.ru', 'bbb', '123');
insert into auth (email, username, password) values ('c@c.ru', 'ccc', '123');
insert into profile (user_id, phone, about) values (5, '4234234232423', 'fdsfdgsd');

select auth.id, auth.email, auth.username, profile.phone, profile.about, profile.onpage from auth
join profile on auth.id = profile.user_id
where email = 'd@d.ru';

select * from auth;
select * from profile;
select * from profile_skill;
select * from skill;
select * from message;

insert into skill (skill_name) values ('drums');
SELECT id from skill where skill_name = 'drumss';
insert into profile_skill(profile_id, skill_id) VALUES (2,1);

select skill_name from profile
join profile_skill ps on profile.id = ps.profile_id
join skill s on ps.skill_id = s.id
join auth a on profile.user_id = a.id
where email = 'd@d.ru';

INSERT INTO content(content, recipient, sender) VALUES ('a', 'e@e.ru', 'a@a.ru');
INSERT INTO content(content, recipient, sender) VALUES ('b', 'e@e.ru', 'a@a.ru');
INSERT INTO content(content, recipient, sender) VALUES ('c', 'a@a.ru', 'e@e.ru');
INSERT INTO content(content, recipient, sender) VALUES ('d', 'a@a.ru', 'e@e.ru');
INSERT INTO content(content, recipient, sender) VALUES ('e', 'a@a.ru', 'f@f.ru');

select * from content where (recipient = 'e@e.ru' and sender = 'a@a.ru') or (sender = 'e@e.ru' and recipient = 'a@a.ru');