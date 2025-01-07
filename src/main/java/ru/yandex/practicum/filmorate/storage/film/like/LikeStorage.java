package ru.yandex.practicum.filmorate.storage.film.like;

public interface LikeStorage {
    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}
