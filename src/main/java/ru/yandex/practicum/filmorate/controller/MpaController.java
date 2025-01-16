package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpa")
public class MpaController {
    private final RatingService ratingService;

    @GetMapping
    public List<Mpa> getRatings() {
        return ratingService.getRatings();
    }

    @GetMapping("/{id}")
    public Mpa getRatingsById(@PathVariable int id) {
        return ratingService.getRatingsById(id);
    }
}
