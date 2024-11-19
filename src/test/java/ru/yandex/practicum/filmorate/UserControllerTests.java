package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {
    UserController us = new UserController();

    @Test
    void checkEmail() {
        User user1 = new User("someWords", "Itsme", "Name", LocalDate.now().minusYears(20));

        assertThrows(ValidateException.class, () -> {
            us.addUser(user1);
        });
    }

    @Test
    void emptyEmail() {
        User user1 = new User(" ", "Itsme", "Name", LocalDate.now().minusYears(20));

        assertThrows(ValidateException.class, () -> {
            us.addUser(user1);
        });
    }

    @Test
    void emptyName() {
        User user1 = new User("myemail@practicum.ru", "Itsme", " ", LocalDate.now().minusYears(20));
        us.addUser(user1);
        assertEquals(user1.getLogin(), us.users.get(user1.getId()).getName());
    }

    @Test
    void emptyLogin() {
        User user1 = new User("myemail@practicum.ru", "", "Name", LocalDate.now().minusYears(20));

        assertThrows(ValidateException.class, () -> {
            us.addUser(user1);
        });
    }

    @Test
    void birthdayInFuture() {
        User user1 = new User("myemail@practicum.ru", "Itsme", "Name", LocalDate.now().plusMonths(20));

        assertThrows(ValidateException.class, () -> us.addUser(user1));
    }

    @Test
    void spaceInLogin() {
        User user1 = new User("myemail@practicum.ru", "Its me", "Name", LocalDate.now());

        assertThrows(ValidateException.class, () -> us.addUser(user1));
    }

    @Test
    void testChangingUserWithEmptyName() {
        User user1 = new User("myemail@practicum.ru", "Itsme", "Name1", LocalDate.now());
        User user2 = new User("myemail@practicum.ru", "Itsme1", null, LocalDate.now());
        us.addUser(user1);
        user2.setId(user1.getId());
        us.changeUser(user2);

        assertTrue(us.allUsers().contains(user2));
        assertFalse(us.allUsers().contains(user1));
        assertEquals(user2.getName(), user1.getName());
    }

    @Test
    void correctlyAllUsers() {

    }
}
