package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final LocalDate START_FILMS = LocalDate.of(1895, 12, 28);
    private final GenreDbStorage genreDbStorage;
    private final DataSource dataSource;

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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.setInt(6, film.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении фильма: " + e.getMessage(), e);
        }
        updateGenresForFilm(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        validateNewFilm(film);
        if (!ratingExists(film.getMpa().getId())) {
            throw new ValidateException("Рейтинг с ID " + film.getMpa().getId() + " не существует");
        }
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
        Film film1 = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        film1.setGenres(genreDbStorage.getGenresForFilm(id));
        return film1;
    }

    @Override
    public List<Film> findBestFilms(int count) {
        List<Film> bestFilms = new ArrayList<>();
        String sql = "SELECT f.*, COUNT(l.user_id) AS like_count " +
                "FROM film f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, count);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Film film = new Film();
                film.setId(resultSet.getInt("id"));
                film.setName(resultSet.getString("name"));
                film.setDescription(resultSet.getString("description"));
                film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
                film.setDuration(resultSet.getInt("duration"));
                bestFilms.add(film);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bestFilms;
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
        if (genres != null && !genres.isEmpty()) {
            String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : genres) {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
            }
        }
    }

    private void addGenresToFilm(Set<Genre> genres, int filmId) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            if (!genreExists(genre.getId())) {
                throw new ValidateException("Жанр с ID " + genre.getId() + " не существует");
            }
        }
        jdbcTemplate.batchUpdate(sql, genres, genres.size(),
                (ps, genre) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genre.getId());
                });
    }

    private boolean ratingExists(int ratingId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{ratingId}, Integer.class);
        return count != null && count > 0;
    }

    private boolean genreExists(int genreId) {
        String sql = "SELECT COUNT(*) FROM genre WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{genreId}, Integer.class);
        return count != null && count > 0;
    }
}