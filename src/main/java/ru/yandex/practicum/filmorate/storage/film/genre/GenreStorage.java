package ru.yandex.practicum.filmorate.storage.film.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreStorage {
    Genre getGenreById(int id);

    List<Genre> getAllGenres();

    Set<Genre> getGenresForFilm(int id);
}
