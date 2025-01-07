package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        checkUser(user);
        String sql = "INSERT INTO \"user\" (login, email, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, user.getLogin());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getName());
                    ps.setDate(4, Date.valueOf(user.getBirthday()));
                    return ps;
                }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User changeUser(User user) {
        if (findUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        String sql = "UPDATE \"user\" SET login = ?, email = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getLogin(), user.getEmail(), user.getName(), Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<User> allUsers() {
        String sql = "SELECT * FROM \"user\"";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs));
        if (users.isEmpty()) {
            throw new NotFoundException("Нет пользователей");
        }
        return users;
    }

    @Override
    public User findUserById(int id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        );
    }

    private void checkUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidateException("Некорректная электронная почта.");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidateException("Некорректный логин.");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        } else if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidateException("Некорректная дата рождения.");
        }
    }

    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Integer.class);
        return Optional.ofNullable(count).orElse(0) > 0;
    }
}
