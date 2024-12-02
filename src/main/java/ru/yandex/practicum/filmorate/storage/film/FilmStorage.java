package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    public Film changeFilm(Film film);

    public Film addFilm(Film film);

    public void removeFilm(int id);

    public List<Film> allFilms();

}
