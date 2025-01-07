package ru.yandex.practicum.filmorate.storage.film.rating;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;


public interface RatingStorage {
    Mpa getRatingById(int id);

    List<Mpa> getAllRatings();
}
