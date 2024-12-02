package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/users")
public class UserController {
    InMemoryUserStorage userStorage;
    UserService userService;

    @Autowired
    private UserController(InMemoryUserStorage storage, UserService service) {
        this.userStorage = storage;
        this.userService = service;
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        return userStorage.addUser(user);
    }

    @PutMapping
    public User changeUser(@RequestBody User user) {
        return userStorage.changeUser(user);
    }

    @GetMapping
    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> usersFriends(@PathVariable int id) {
        return userService.getFriend(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> mutualFriend(@PathVariable int id, @PathVariable int friendId) {
        return userService.generalFriends(id, friendId);
    }
}
