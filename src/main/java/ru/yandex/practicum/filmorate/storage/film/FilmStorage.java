package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film changeFilm(Film film);

    Film addFilm(Film film);

    List<Film> allFilms();

    Film getFilmById(int id);

    List<Film> findBestFilms(int count);
}
