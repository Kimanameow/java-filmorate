package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FriendException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.friendships.FriendshipStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public void addFriend(int id, int friendId) {
        if (id == friendId) {
            throw new FriendException("Нельзя добавить себя в друзья");
        }
        checkUser(id, friendId);
        friendshipStorage.addFriend(id, friendId);
    }

    public void deleteFriend(int id, int friendId) {
        if (id == friendId) {
            throw new FriendException("Нельзя удалить себя из друзей");
        }
        checkUser(id, friendId);
        friendshipStorage.deleteFriend(id, friendId);
    }

    public List<User> generalFriends(int id, int friendId) {
        checkUser(id, friendId);
        return friendshipStorage.getMutualFriends(id, friendId);
    }

    public List<User> getFriend(int id) {
        if (userStorage.findUserById(id) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return friendshipStorage.getFriends(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User changeUser(User user) {
        if (userStorage.findUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.changeUser(user);
    }

    private void checkUser(int userId, int friendId) {
        if (!userStorage.userExists(userId) || !userStorage.userExists(friendId)) {
            throw new NotFoundException("Not found");
        }
    }
}
