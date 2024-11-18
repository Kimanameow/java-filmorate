package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/films")
public class FilmController {
    private int id = 1;
    private HashMap<Integer, Film> films = new HashMap();
    private static final LocalDate START_FILMS = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        film.setId(id);
        id++;
        validateNewFilm(film);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film changeFilm(@RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            films.remove(film.getId());
            validateNewFilm(film);
            films.put(film.getId(), film);
        }
        return film;
    }

    @GetMapping
    public Collection<Film> allFilms() {
        return films.values();
    }

    private void validateNewFilm(Film film) {
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidateException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidateException("Максимальная длина строки - 200 символов.");
        }
        if (film.getDuration().toMinutes() < 1) {
            throw new ValidateException("Продолжительность не может быть отрицательной.");
        }
        if (film.getReleaseDate().isBefore(START_FILMS)) {
            throw new ValidateException("Релиз не может быть раньше 28 декабря 1985 года.");
        }
    }
}
