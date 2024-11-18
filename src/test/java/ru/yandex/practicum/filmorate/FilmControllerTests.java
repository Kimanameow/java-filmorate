package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmControllerTests {
    FilmController fc = new FilmController();

    @Test
    void durationZeroTest() {
        Film film1 = new Film("Name", "Descr", LocalDate.now(), Duration.ofMinutes(0));

        assertThrows(ValidateException.class, () -> {
            fc.addFilm(film1);
        });
    }

    @Test
    void emptyName() {
        Film film1 = new Film(" ", "Descr", LocalDate.now(), Duration.ofMinutes(20));
        assertThrows(ValidateException.class, () -> {
            fc.addFilm(film1);
        });
    }

    @Test
    void releaseDate() {
        Film film1 = new Film("Name", "Des", LocalDate.of(1895, 12, 28), Duration.ofMinutes(20));
        fc.addFilm(film1);
        assertTrue(fc.allFilms().contains(film1));
    }
}
