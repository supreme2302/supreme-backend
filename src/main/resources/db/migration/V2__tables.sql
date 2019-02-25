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
  recipient citext REFERENCES auth(email),
  sender citext REFERENCES auth(email),
  message_date TIMESTAMP WITH TIME ZONE
);

INSERT INTO skill (skill_name) VALUES ('drums');
INSERT INTO skill (skill_name) VALUES ('guitar');
INSERT INTO skill (skill_name) VALUES ('bass');
INSERT INTO skill (skill_name) VALUES ('keyboards');

INSERT INTO genre (genre_name) VALUES ('pop');
INSERT INTO genre (genre_name) VALUES ('rock');
INSERT INTO genre (genre_name) VALUES ('jazz');
INSERT INTO genre (genre_name) VALUES ('metal');



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
select * from genre;
select * from profile_genre;

update profile_skill set skill_id = 4 where id = 6;

insert into skill (skill_name) values ('drums');
insert into genre (genre_name) values ('metall');
SELECT id from skill where skill_name = 'drumss';
insert into profile_skill(profile_id, skill_id) VALUES (2,1);
insert into profile_genre(profile_id, genre_id) values (9,2);

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

insert into skill(skill_name) values ('guitar');
insert into skill(skill_name) values ('piano');
insert into skill(skill_name) values ('drums');

select * from content where (recipient = 'e@e.ru' and sender = 'a@a.ru') or (sender = 'e@e.ru' and recipient = 'a@a.ru');


select username, email, phone, about, skill_name from auth
join profile p on auth.id = p.user_id
join profile_skill skill on p.id = skill.profile_id
join skill s on skill.skill_id = s.id
where s.skill_name = 'Sandra Adamss' OR s.skill_name = 'drums'
order by username desc offset 2 limit 1;


select username, email, about from auth
                join profile p on auth.id = p.user_id
                join profile_skill skill on p.id = skill.profile_id
                join skill s on skill.skill_id = s.id
                where s.skill_name = 'Sandra Adams'
                order by username
                offset 1 rows limit 1;



select distinct auth.id, username, email, about from auth
join profile p on auth.id = p.user_id
join profile_skill skill on p.id = skill.profile_id
join skill s on skill.skill_id = s.id
join profile_genre pg on p.id = pg.profile_id
join genre g on pg.genre_id = g.id
where s.skill_name::citext = 'sandra adams'::citext
and g.genre_name::citext = 'pop'::citext
and p.onpage = true
order by username offset 0 rows limit 6;


select distinct auth.id, username, email, about from auth
join profile p on auth.id = p.user_id
join profile_skill skill on p.id = skill.profile_id
join skill s on skill.skill_id = s.id
where  s.skill_name = 'sandra adams' order by username offset 0 rows limit 6;


select distinct auth.id, username, email, about from auth
join profile p on auth.id = p.user_id
join profile_skill skill on p.id = skill.profile_id
join skill s on skill.skill_id = s.id
join profile_genre pg on p.id = pg.profile_id
join genre g on pg.genre_id = g.id
where  g.genre_name::citext = ?::citext
 and  g.genre_name::citext = ?::citext
 and p.onpage = true
 order by username offset ? rows limit ?;


insert into comment_counter(user_id) values (1);
select * from comment_counter;
update comment_counter set counter = counter + 1, sum_rating = sum_rating + 5;
select (sum_rating / counter) from comment_counter;

SELECT * from comment where to_user_id = '1';