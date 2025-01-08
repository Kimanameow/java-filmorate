package ru.yandex.practicum.filmorate.DAO;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.friendships.FriendshipStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM friendships f JOIN \"user\" u ON f.friend_id = u.id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeUser(rs)), userId);
    }

    @Override
    public List<User> getMutualFriends(int userId, int friendId) {
        String sql = "SELECT u.* FROM friendships f1 " +
                "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                "JOIN \"user\" u ON f1.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeUser(rs)), userId, friendId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
