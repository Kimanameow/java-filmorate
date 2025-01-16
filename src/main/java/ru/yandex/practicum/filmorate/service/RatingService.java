package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.rating.RatingStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingStorage ratingStorage;

    public List<Mpa> getRatings() {
        return ratingStorage.getAllRatings();
    }

    public Mpa getRatingsById(int id) {
        return ratingStorage.getRatingById(id);
    }
}
