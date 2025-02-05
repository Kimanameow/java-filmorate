package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User changeUser(User user);

    void deleteUser(int id);

    List<User> allUsers();

    User findUserById(int id);

    boolean userExists(int userId);

}
