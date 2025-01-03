package ru.yandex.practicum.filmorate.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.friendships.FriendshipStorage;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class FriendshipDbStorage implements FriendshipStorage {
    private final DataSource dataSource;

    @Autowired
    public FriendshipDbStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Not found");
        }
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, friendId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Not found");
        }
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, friendId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Not found");
        }
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM friendships f JOIN \"user\" u ON f.friend_id = u.id WHERE f.user_id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                User friend = new User();
                friend.setId(resultSet.getInt("id"));
                friend.setLogin(resultSet.getString("login"));
                friend.setEmail(resultSet.getString("email"));
                friend.setName(resultSet.getString("name"));
                friend.setBirthday(resultSet.getDate("birthday").toLocalDate());
                friends.add(friend);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    @Override
    public List<User> getMutualFriends(int userId, int friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Not found");
        }
        List<User> mutualFriends = new ArrayList<>();
        String sql = "SELECT u.* FROM friendships f1 " +
                "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                "JOIN \"user\" u ON f1.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, friendId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                User mutualFriend = new User();
                mutualFriend.setId(resultSet.getInt("id"));
                mutualFriend.setLogin(resultSet.getString("login"));
                mutualFriend.setEmail(resultSet.getString("email"));
                mutualFriend.setName(resultSet.getString("name"));
                mutualFriend.setBirthday(resultSet.getDate("birthday").toLocalDate());
                mutualFriends.add(mutualFriend);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mutualFriends;
    }

    private boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE id = ?";
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
