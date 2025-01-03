package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.like.LikeStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;

    public void addLike(int filmId, int idOfUser) {
        likeStorage.addLike(filmId, idOfUser);
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film changeFilm(Film film) {
        return filmStorage.changeFilm(film);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public void deleteLike(int filmId, int idOfUser) {
        likeStorage.removeLike(filmId, idOfUser);
    }

    public List<Film> findBestFilms(int count) {
        return likeStorage.findBestFilms(count);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }
}
