package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final LocalDate START_FILMS = LocalDate.of(1895, 12, 28);

    @Override
    public Film changeFilm(Film film) {
        Film thisFilm = getFilmById(film.getId());
        if (thisFilm == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (film.getName() == null) {
            film.setName(thisFilm.getName());
        }
        if (film.getDescription() == null) {
            film.setDescription(thisFilm.getDescription());
        }
        if (film.getDuration() == 0) {
            film.setDuration(thisFilm.getDuration());
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(thisFilm.getReleaseDate());
        }
        if (film.getMpa() == null) {
            film.setMpa(thisFilm.getMpa());
        }
        validateNewFilm(film);
        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenresForFilm(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        validateNewFilm(film);
        String sql = "INSERT INTO film (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        addGenresToFilm(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public List<Film> allFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "       f.rating_id, r.name AS rating_name " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.rating_id, r.name AS rating_name " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "WHERE f.id = ?;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    @Override
    public List<Film> findBestFilms(int count) {
        if (count < 1) {
            throw new ValidateException("Счетчик меньше 1.");
        }
        String filmLikesQuery = "SELECT film_id, COUNT(film_id) AS likes " +
                "FROM likes " +
                "GROUP BY film_id " +
                "ORDER BY likes DESC " +
                "LIMIT ?;";
        List<Integer> filmIds = jdbcTemplate.query(filmLikesQuery, new Object[]{count}, (rs, rowNum) ->
                rs.getInt("film_id"));
        if (filmIds.isEmpty()) {
            return new ArrayList<>();
        }
        String listFilmId = String.join(",", filmIds.stream().map(String::valueOf).toArray(String[]::new));
        String filmsQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "       f.rating_id, r.name AS rating_name " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "WHERE f.id IN (" + listFilmId + ") " +
                "LIMIT ?;";

        return jdbcTemplate.query(filmsQuery, new Object[]{count}, (rs, rowNum) -> mapRowToFilm(rs));
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        return Film.builder().id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                .build();
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

    private void updateGenresForFilm(Set<Genre> genres, int filmId) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        addGenresToFilm(genres, filmId);
    }

    private void addGenresToFilm(Set<Genre> genres, int filmId) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, genres, genres.size(),
                (ps, genre) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genre.getId());
                });
    }
}