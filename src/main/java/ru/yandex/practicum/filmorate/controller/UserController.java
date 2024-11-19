package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping(value = "/users")
public class UserController {
    public HashMap<Integer, User> users = new HashMap();
    private int id = 1;

    @PostMapping
    public User addUser(@RequestBody User user) {
        user.setId(id);
        id++;
        checkUser(user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User changeUser(@RequestBody User user) {
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

    @GetMapping
    public Collection<User> allUsers() {
        return users.values();
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
