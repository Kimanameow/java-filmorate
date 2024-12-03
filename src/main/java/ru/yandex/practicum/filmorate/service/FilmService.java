package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

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
        if (filmAndLikes.isEmpty()) {
            throw new FilmException("Лайков пока нет");
        }
        int availableFilmsCount = filmAndLikes.size();
        if (count > availableFilmsCount) {
            count = availableFilmsCount;
        }

        Map<Integer, Long> filmAndLikesCount = filmAndLikes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));

        List<Integer> topTenFilms = filmAndLikesCount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topTenFilms.stream()
                .map(filmStorage.allFilms()::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
