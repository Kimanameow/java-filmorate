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
        userStorage.getUsers().get(friendId).getFriends().add(id);
    }

    public void deleteFriend(int id, int friendId) {
        checkUserInSystem(id, friendId);
        if (id == friendId) {
            throw new FriendException("Нельзя удалить себя из друзей");
        }
        userStorage.getUsers().get(id).getFriends().remove((Integer) friendId);
        userStorage.getUsers().get(friendId).getFriends().remove((Integer) id);
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
        if (userStorage.getUsers().get(id).getFriends().isEmpty()) {
            throw new FriendException("У вас нет друзей");
        }
        if (userStorage.getUsers().get(friendId).getFriends().isEmpty()) {
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
        List<User> nameFriend = new ArrayList<>();
        if (userStorage.getUsers() == null || userStorage.getUsers().get(id) == null) {
            throw new NotFoundException("Пользователя не существует");
        }
        if (userStorage.getUsers().get(id).getFriends().isEmpty()) {
            return nameFriend;
        }
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
        if (userStorage.getUsers().isEmpty() || !userStorage.getUsers().containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.changeUser(user);
    }
}
