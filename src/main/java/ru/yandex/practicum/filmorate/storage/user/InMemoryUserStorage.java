package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Getter
    private final HashMap<Integer, User> users = new HashMap();
    private int id = 1;

    @Override
    public User addUser(User user) {
        user.setId(id);
        id++;
        checkUser(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User changeUser(User user) {
        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            if (user.getName() == null) {
                user.setName(oldUser.getName());
            }
            if (user.getLogin() == null) {
                user.setLogin(oldUser.getLogin());
            }
            if (user.getEmail() == null) {
                user.setEmail(oldUser.getEmail());
            }
            if (user.getBirthday() == null) {
                user.setBirthday(oldUser.getBirthday());
            }
            users.remove(user.getId());
            users.put(user.getId(), user);
            return user;
        } else throw new NotFoundException("Пользователь не найден.");
    }

    @Override
    public void deleteUser(int id) {
        if (!users.isEmpty() && users.containsKey(id)) {
            users.remove(id);
        }
    }

    @Override
    public List<User> allUsers() {
        if (users.isEmpty()) {
            throw new NotFoundException("Нет пользователей");
        }
        return users.values().stream().toList();
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
