package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int id = 1;
    private final HashMap<Integer, Film> films = new HashMap();
    private static final LocalDate START_FILMS = LocalDate.of(1895, 12, 28);

    @Override
    public Film changeFilm(Film film) {
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            if (film.getName() == null) {
                film.setName(oldFilm.getName());
            }
            if (film.getDescription() == null) {
                film.setDescription(oldFilm.getDescription());
            }
            if (film.getDuration() == 0) {
                film.setDuration(oldFilm.getDuration());
            }
            if (film.getReleaseDate() == null) {
                film.setReleaseDate(oldFilm.getReleaseDate());
            }
            films.remove(film.getId());
            films.put(film.getId(), film);
            return film;
        } else throw new NotFoundException("Фильм не найден.");
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(id);
        id++;
        validateNewFilm(film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> allFilms() {
        return films.values().stream().toList();
    }

    @Override
    public Film getFilmById(int id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        return films.get(id);
    }

    private void validateNewFilm(Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidateException("Название не может быть пустым.");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new ValidateException("Максимальная длина строки - 200 символов.");
        }
        if (film.getDuration() < 1) {
            throw new ValidateException("Продолжительность не может быть отрицательной.");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(START_FILMS)) {
            throw new ValidateException("Релиз не может быть раньше 28 декабря 1985 года.");
        }
    }
}
