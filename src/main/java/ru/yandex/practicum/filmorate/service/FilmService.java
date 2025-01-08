package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.rating.RatingStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;

    public void addLike(int filmId, int idOfUser) {
        likeStorage.addLike(filmId, idOfUser);
    }

    public Collection<Film> allFilms() {
        List<Film> films = filmStorage.allFilms();
        return genreStorage.addFilmGenres(films);
    }

    public Film changeFilm(Film film) {
        validateGenreAndRating(film);
        return filmStorage.changeFilm(film);
    }

    public Film addFilm(Film film) {
        validateGenreAndRating(film);
        return filmStorage.addFilm(film);
    }

    public void deleteLike(int filmId, int idOfUser) {
        likeStorage.removeLike(filmId, idOfUser);
    }

    public List<Film> findBestFilms(int count) {
        List<Film> films = filmStorage.findBestFilms(count);
        return genreStorage.addFilmGenres(films);
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        film.setGenres(genreStorage.getGenresForFilm(film.getId()));
        return film;
    }

    private void validateGenreAndRating(Film film) {
        if (!ratingStorage.ratingExists(film.getMpa().getId())) {
            throw new ValidateException("Рейтинг с ID " + film.getMpa().getId() + " не существует");
        }
        for (Genre genre : film.getGenres()) {
            if (!genreStorage.genreExists(genre.getId())) {
                throw new ValidateException("Жанр с ID " + genre.getId() + " не существует");
            }
        }
    }
}
