package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Like {
    private int filmId;
    private long userId;
}
