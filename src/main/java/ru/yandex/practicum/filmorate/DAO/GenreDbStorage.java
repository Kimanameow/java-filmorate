package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.genre.GenreStorage;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}