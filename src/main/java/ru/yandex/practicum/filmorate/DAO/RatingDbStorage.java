package ru.yandex.practicum.filmorate.DAO;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.rating.RatingStorage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

@Component
public class RatingDbStorage implements RatingStorage {
    private final DataSource dataSource;
    private final HashMap<Integer, Mpa> ratings = new HashMap<>();

    @Autowired
    public RatingDbStorage(DataSource dataSource) {
        this.dataSource = dataSource;
        loadRatings();
    }

    private void loadRatings() {
        String sql = "SELECT id, name FROM rating";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                ratings.put(id, new Mpa(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mpa getRatingById(int id) {
        if (ratings.isEmpty() || !ratings.containsKey(id)) {
            throw new NotFoundException("Not Found");
        }
        return ratings.get(id);
    }

    @Override
    public List<Mpa> getAllRatings() {
        if (ratings.isEmpty()) {
            throw new NotFoundException("Not found");
        }
        return ratings.values().stream().toList();
    }
}