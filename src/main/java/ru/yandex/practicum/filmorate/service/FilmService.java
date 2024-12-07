package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;

@Service
public class FilmService {

    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(int filmId, int idOfUser) {
        checkFilmAndUserInSystem(filmId, idOfUser);
        filmStorage.getFilms().get(filmId).getLikes().add(idOfUser);
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
        checkFilmAndUserInSystem(filmId, idOfUser);
        if (!filmStorage.getFilms().containsKey(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        if (filmStorage.getFilms().get(filmId).getLikes().contains(idOfUser)) {
            filmStorage.getFilms().get(filmId).getLikes().remove(idOfUser);
        } else {
            throw new FilmException("Вы не ставили лайк этому фильму");
        }
    }

    public List<Film> findTenBestFilms(int count) {
        List<Film> bestFilms = new ArrayList<>();
        List<Film> films = new ArrayList<>(filmStorage.getFilms().values());
        films.sort((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()));
        int limit = Math.min(count, films.size());
        for (int i = 0; i < limit; i++) {
            bestFilms.add(films.get(i));
        }
        return bestFilms;
    }

    private void checkFilmAndUserInSystem(int idOfFilm, int idOfUser) {
        if (filmStorage.getFilms().isEmpty() || !filmStorage.getFilms().containsKey(idOfFilm)) {
            throw new NotFoundException("Фильм не найден");
        }
        if (userStorage.getUsers().isEmpty() || !userStorage.getUsers().containsKey(idOfUser)) {
            throw new NotFoundException("Пользователя не существует");
        }
    }
}
