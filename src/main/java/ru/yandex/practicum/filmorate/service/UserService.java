package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FriendException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final Map<Integer, List<Integer>> userAndHisFriend;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage us) {
        this.userAndHisFriend = new HashMap<>();
        this.userStorage = us;
    }

    public void addFriend(int id, int friendId) {
        checkUserInSystem(id, friendId);
        if (id == friendId) {
            throw new FriendException("Нельзя добавить себя в друзья");
        }
        userAndHisFriend.putIfAbsent(id, new ArrayList<>());
        userAndHisFriend.putIfAbsent(friendId, new ArrayList<>());
        if (userAndHisFriend.get(id).contains(friendId)) {
            throw new FriendException(id + " уже является вашим другом");
        }
        userAndHisFriend.get(id).add(friendId);
        userAndHisFriend.get(friendId).add(id);
    }

    public void deleteFriend(int id, int friendId) {
        checkUserInSystem(id, friendId);
        if (id == friendId) {
            throw new FriendException("Нельзя удалить себя из друзей");
        }
        validateFriends(id, friendId);
        if (!userAndHisFriend.get(id).contains(friendId)) {
            throw new FriendException(id + " не ваш друг");
        }
        userAndHisFriend.get(id).remove((Integer) friendId);
        userAndHisFriend.get(friendId).remove((Integer) id);
    }

    public List<User> generalFriends(int id, int friendId) {
        checkUserInSystem(id, friendId);
        validateFriends(id, friendId);
        List<Integer> anotherUserFriends = userAndHisFriend.get(friendId);
        List<Integer> yourFriends = userAndHisFriend.get(id);
        if (yourFriends.isEmpty() || anotherUserFriends.isEmpty()) {
            throw new NotFoundException("Список друзей пуст");
        }
        List<User> nameFriend = new ArrayList<>();
        for (Integer i : yourFriends.stream().filter(anotherUserFriends::contains).toList()) {
            nameFriend.add(userStorage.getUsers().get(i));
        }
        return nameFriend;
    }

    private void validateFriends(int id, int friendId) {
        if (!userAndHisFriend.containsKey(id)) {
            throw new FriendException("У вас нет друзей");
        }
        if (!userAndHisFriend.containsKey(friendId)) {
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
        if (userAndHisFriend.isEmpty() || !userAndHisFriend.containsKey(id)) {
            throw new NotFoundException("У пользователя нет друзей");
        }
        List<User> nameFriend = new ArrayList<>();
        for (Integer i : userAndHisFriend.get((Integer) id).stream().toList()) {
            nameFriend.add(userStorage.getUsers().get(i));
        }
        return nameFriend;
    }
}
