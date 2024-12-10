package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(int filmId, int idOfUser) {
        userStorage.getUserById(idOfUser);
        filmStorage.getFilmById(filmId).getLikes().add(idOfUser);
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
        userStorage.getUserById(idOfUser);
        filmStorage.getFilmById(filmId).deleteLike(idOfUser);
    }

    public List<Film> findBestFilms(int count) {
        return filmStorage.allFilms().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
