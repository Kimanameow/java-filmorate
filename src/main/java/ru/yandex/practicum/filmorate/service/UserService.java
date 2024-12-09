package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FriendException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(int id, int friendId) {
        if (id == friendId) {
            throw new FriendException("Нельзя добавить себя в друзья");
        }
        userStorage.getUserById(id).getFriends().add(friendId);
        userStorage.getUserById(friendId).getFriends().add(id);
    }

    public void deleteFriend(int id, int friendId) {
        if (id == friendId) {
            throw new FriendException("Нельзя удалить себя из друзей");
        }
        userStorage.getUserById(id).getFriends().remove((Integer) friendId);
        userStorage.getUserById(friendId).getFriends().remove((Integer) id);
    }

    public List<User> generalFriends(int id, int friendId) {
        validateFriends(id, friendId);
        Set<Integer> anotherUserFriends = userStorage.getUserById(friendId).getFriends();
        Set<Integer> yourFriends = userStorage.getUserById(id).getFriends();
        if (yourFriends.isEmpty() || anotherUserFriends.isEmpty()) {
            throw new NotFoundException("Список друзей пуст");
        }
        List<User> commonFriends = new ArrayList<>();
        for (Integer i : yourFriends) {
            if (anotherUserFriends.contains(i)) {
                commonFriends.add(userStorage.getUserById(i));
            }
        }
        return commonFriends;
    }

    private void validateFriends(int id, int friendId) {
        if (userStorage.getUserById(id).getFriends().isEmpty()) {
            throw new FriendException("У вас нет друзей");
        }
        if (userStorage.getUserById(friendId).getFriends().isEmpty()) {
            throw new FriendException("У " + friendId + " нет друзей");
        }
    }

    public List<User> getFriend(int id) {
        List<User> nameFriend = new ArrayList<>();
        if (userStorage.getUserById(id).getFriends().isEmpty()) {
            return nameFriend;
        }
        for (Integer i : userStorage.getUserById(id).getFriends().stream().toList()) {
            nameFriend.add(userStorage.getUserById(i));
        }
        return nameFriend;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User changeUser(User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.changeUser(user);
    }
}
