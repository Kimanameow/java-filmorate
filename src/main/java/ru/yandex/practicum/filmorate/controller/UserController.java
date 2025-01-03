package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.friendships.FriendshipStorage;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/users")
public class UserController {
    UserService userService;

    @Autowired
    private UserController(UserService service) {
        this.userService = service;
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User changeUser(@RequestBody User user) {
        return userService.changeUser(user);
    }

    @GetMapping
    public Collection<User> allUsers() {
        return userService.allUsers();
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

    @GetMapping("/{id}/friends/common/{friendId}")
    public List<User> mutualFriend(@PathVariable int id, @PathVariable int friendId) {
        return userService.generalFriends(id, friendId);
    }
}
