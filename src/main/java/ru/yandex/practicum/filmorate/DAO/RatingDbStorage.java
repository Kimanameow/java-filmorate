package ru.yandex.practicum.filmorate.DAO;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.rating.RatingStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa getRatingById(int id) {
        String sql = "SELECT * FROM rating WHERE id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowMpa(rs), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + id + " не существует"));
    }

    @Override
    public List<Mpa> getAllRatings() {
        String sql = "SELECT * FROM rating";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowMpa(rs));
    }

    private Mpa mapRowMpa(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Mpa(id, name);
    }


    @Override
    public boolean ratingExists(int ratingId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{ratingId}, Integer.class);
        return count != null && count > 0;
    }
}