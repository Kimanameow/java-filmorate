package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTests {
    FilmStorage fc = new InMemoryFilmStorage();

    @Test
    void durationZeroTest() {
        Film film1 = new Film("Name", "Descr", LocalDate.now(), 0);

        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void emptyName() {
        Film film1 = new Film(" ", "Descr", LocalDate.now(), 20);
        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void nullName() {
        Film film1 = new Film(null, "Descr", LocalDate.now(), 20);
        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void nullDescription() {
        Film film1 = new Film("Name", null, LocalDate.now(), 20);
        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void longDescription() {
        Film film1 = new Film("N", "В небольшом городке происходит загадочное исчезновение." +
                " Местный детектив, столкнувшись с тайнами прошлого, объединяет усилия с юной журналисткой." +
                " Вместе они раскрывают мрачные секреты, меняющие их жизни навсегда.", LocalDate.now(), 20);
        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void checkReleaseDate() {
        Film film1 = new Film("Name", "Des", LocalDate.of(1895, 12, 28), 20);
        fc.addFilm(film1);
        assertTrue(fc.allFilms().contains(film1));
    }

    @Test
    void filmOlderThanFirst() {
        Film film1 = new Film("Name", "Descr", LocalDate.of(1895, 12, 27), 20);
        assertThrows(ValidateException.class, () -> fc.addFilm(film1));
    }

    @Test
    void goodChangingFilm() {
        Film film1 = new Film("Name1", "Descr", LocalDate.of(1895, 12, 28), 20);
        Film film2 = new Film("Name2", "Descr", LocalDate.of(1895, 12, 28), 20);
        fc.addFilm(film1);
        film2.setId(film1.getId());
        fc.changeFilm(film2);

        assertEquals(1, fc.allFilms().size());
        assertTrue(fc.allFilms().contains(film2));
        assertFalse(fc.allFilms().contains(film1));
    }

    @Test
    void cantFindFilmPerId() {
        Film film1 = new Film("Name1", "Descr", LocalDate.of(1895, 12, 28), 20);
        film1.setId(20);
        assertThrows(NotFoundException.class, () -> fc.changeFilm(film1));
    }
}
