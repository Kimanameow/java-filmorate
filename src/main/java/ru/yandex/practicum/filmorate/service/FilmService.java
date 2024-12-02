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

    private final Map<Integer, List<Integer>> filmAndLikes;
    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmAndLikes = new HashMap<>();
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(int filmId, int idOfUser) {
        checkFilmAndUserInSystem(filmId, idOfUser);
        filmAndLikes.putIfAbsent(filmId, new ArrayList<>());
        if (filmAndLikes.get(filmId).contains(idOfUser)) {
            throw new FilmException("Вы уже ставили лайк этому фильму");
        } else {
            filmAndLikes.get(filmId).add(idOfUser);
        }
    }

    public void deleteLike(int filmId, int idOfUser) {
        checkFilmAndUserInSystem(filmId, idOfUser);
        if (!filmAndLikes.containsKey(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        if (filmAndLikes.get(filmId).contains(idOfUser)) {
            filmAndLikes.get(filmId).remove((Integer) idOfUser);
        } else {
            throw new FilmException("Вы не ставили лайк этому фильму");
        }
    }

    public List<Film> findTenBestFilms(int count) {
        Map<Integer, Integer> filmAndLikesCount = new HashMap<>();
        for (Integer i : filmAndLikes.keySet()) {
            filmAndLikesCount.put(i, filmAndLikes.get(i).size());
        }
        List<Map.Entry<Integer, Integer>> sortedFilms = new ArrayList<>(filmAndLikesCount.entrySet());
        sortedFilms.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        List<Integer> topTenFilms = new ArrayList<>();
        for (int i = 0; i < Math.min(count, sortedFilms.size()); i++) {
            topTenFilms.add(sortedFilms.get(i).getKey());
        }
        List<Film> films = new ArrayList<>();
        for (Integer i : topTenFilms) {
            films.add(filmStorage.allFilms().get(i));
        }
        return films;
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
