package ru.yandex.practicum.filmorate.DAO;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class UserDbStorage implements UserStorage {
    private final DataSource dataSource;

    public UserDbStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User addUser(User user) {
        checkUser(user);
        String sql = "INSERT INTO \"user\" (login, email, name, birthday) VALUES (?, ?, ?, ?)";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getName());
            pstmt.setDate(4, Date.valueOf(user.getBirthday()));
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при добавлении пользователя в базу данных.");
        }
    }

    @Override
    public User changeUser(User user) {
        if (findUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        String sql = "UPDATE \"user\" SET login = ?, email = ?, name = ?, birthday = ? WHERE id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getName());
            pstmt.setDate(4, Date.valueOf(user.getBirthday()));
            pstmt.setInt(5, user.getId());
            pstmt.executeUpdate();
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ValidateException("Ошибка при обновлении пользователя в базе данных.");
        }
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ValidateException("Ошибка при удалении пользователя из базы данных.");
        }
    }

    @Override
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"user\"";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении всех пользователей из базы данных.");
        }
        if (users.isEmpty()) {
            throw new NotFoundException("Нет пользователей");
        }
        return users;
    }

    @Override
    public User findUserById(int id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении пользователя из базы данных.");
        }
        throw new NotFoundException("Пользователь не найден");
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
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
}
