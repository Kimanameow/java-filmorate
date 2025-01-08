package ru.yandex.practicum.filmorate.storage.film.rating;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;


public interface RatingStorage {
    Mpa getRatingById(int id);

    List<Mpa> getAllRatings();

    boolean ratingExists(int ratingId);
}
