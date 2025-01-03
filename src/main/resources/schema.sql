DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS film_genre;
DROP TABLE IF EXISTS film;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS genre;

CREATE TABLE IF NOT EXISTS genre (
  id bigint auto_increment PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS rating (
  id bigint auto_increment PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
  id bigint auto_increment PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(250),
  release_date DATE,
  duration INTEGER,
  rating_id INTEGER,
  genre_id INTEGER,
  FOREIGN KEY (rating_id) REFERENCES rating (id),
  FOREIGN KEY (genre_id) REFERENCES genre (id)
);

CREATE TABLE IF NOT EXISTS "user" (
  id bigint auto_increment PRIMARY KEY,
  login VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  name VARCHAR(100) NOT NULL,
  birthday DATE
);

CREATE TABLE IF NOT EXISTS likes (
  film_id BIGINT,
  user_id BIGINT,
  PRIMARY KEY (film_id, user_id),
  FOREIGN KEY (film_id) REFERENCES film (id),
  FOREIGN KEY (user_id) REFERENCES "user" (id)
);

CREATE TABLE IF NOT EXISTS friendships (
  user_id BIGINT,
  friend_id BIGINT,
  PRIMARY KEY (user_id, friend_id),
  FOREIGN KEY (user_id) REFERENCES "user" (id),
  FOREIGN KEY (friend_id) REFERENCES "user" (id)
);

CREATE TABLE IF NOT EXISTS film_genre (
  film_id BIGINT,
  genre_id BIGINT,
  PRIMARY KEY (film_id, genre_id),
  FOREIGN KEY (film_id) REFERENCES film (id),
  FOREIGN KEY (genre_id) REFERENCES genre (id)
);

INSERT INTO rating (name) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');

INSERT INTO genre (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');