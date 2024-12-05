package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FriendException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;

@Service
public class UserService {

    private final InMemoryUserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage us) {
        this.userStorage = us;
    }

    public void addFriend(int id, int friendId) {
        checkUserInSystem(id, friendId);
        if (id == friendId) {
            throw new FriendException("Нельзя добавить себя в друзья");
        }
        userStorage.getUsers().get(id).getFriends().add(friendId);
    }

    public void deleteFriend(int id, int friendId) {
        checkUserInSystem(id, friendId);
        validateFriends(id, friendId);
        if (id == friendId) {
            throw new FriendException("Нельзя удалить себя из друзей");
        }
        if (!userStorage.getUsers().get(id).getFriends().contains(friendId)) {
            throw new FriendException(id + " не ваш друг");
        }
        userStorage.getUsers().get(id).getFriends().remove(friendId);
        userStorage.getUsers().get(friendId).getFriends().remove(id);
    }

    public List<User> generalFriends(int id, int friendId) {
        checkUserInSystem(id, friendId);
        validateFriends(id, friendId);
        Set<Integer> anotherUserFriends = userStorage.getUsers().get(friendId).getFriends();
        Set<Integer> yourFriends = userStorage.getUsers().get(id).getFriends();
        if (yourFriends.isEmpty() || anotherUserFriends.isEmpty()) {
            throw new NotFoundException("Список друзей пуст");
        }
        List<User> commonFriends = new ArrayList<>();
        for (Integer i : yourFriends) {
            if (anotherUserFriends.contains(i)) {
                commonFriends.add(userStorage.getUsers().get(i));
            }
        }
        return commonFriends;
    }

    private void validateFriends(int id, int friendId) {
        if (!userStorage.allUsers().get(id).getFriends().isEmpty()) {
            throw new FriendException("У вас нет друзей");
        }
        if (!userStorage.allUsers().get(friendId).getFriends().isEmpty()) {
            throw new FriendException("У " + friendId + " нет друзей");
        }
    }

    private void checkUserInSystem(int id, int friendId) {
        if (userStorage.getUsers().isEmpty() || !userStorage.getUsers().containsKey(id)
                || !userStorage.getUsers().containsKey(friendId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<User> getFriend(int id) {
        if (userStorage.getUsers().isEmpty() || userStorage.getUsers().get(id).getFriends().isEmpty()) {
            throw new NotFoundException("У пользователя нет друзей");
        }
        List<User> nameFriend = new ArrayList<>();
        for (Integer i : userStorage.getUsers().get(id).getFriends().stream().toList()) {
            nameFriend.add(userStorage.getUsers().get(i));
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
        if (!userStorage.getUsers().containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.changeUser(user);
    }
}
