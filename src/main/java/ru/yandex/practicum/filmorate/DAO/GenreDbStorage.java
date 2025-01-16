package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.genre.GenreStorage;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT id, name FROM genre WHERE id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowGenre(rs), id).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Жанр не найден"));
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT id, name FROM genre";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowGenre(rs));
    }

    public Set<Genre> getGenresForFilm(int filmId) {
        Set<Genre> genres = new HashSet<>();
        String sql = "SELECT g.id, g.name FROM film_genre fg JOIN genre g ON fg.genre_id = g.id WHERE fg.film_id = ?";
        jdbcTemplate.query(sql, new Object[]{filmId}, (rs, rowNum) -> {
            genres.add(new Genre(rs.getInt("id"), rs.getString("name")));
            return null;
        });
        return genres;
    }

    private Genre mapRowGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Genre(id, name);
    }

    public boolean genreExists(int genreId) {
        String sql = "SELECT COUNT(*) FROM genre WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{genreId}, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<Film> addFilmGenres(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        String filmIds = films.stream()
                .map(film -> String.valueOf(film.getId()))
                .collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.id AS genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + filmIds + ")";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int genreId = rs.getInt("genre_id");
            String genreName = rs.getString("name");
            return new Genre(genreId, genreName);
        });
        Map<Integer, Set<Genre>> filmGenresMap = new HashMap<>();
        for (Genre genre : genres) {
            filmGenresMap.computeIfAbsent(genre.getId(), k -> new HashSet<>()).add(genre);
        }
        for (Film film : films) {
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), Collections.emptySet()));
        }
        return films;
    }
}