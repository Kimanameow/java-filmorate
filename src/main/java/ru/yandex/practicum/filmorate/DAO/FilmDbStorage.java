package ru.yandex.practicum.filmorate.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmDbStorage implements FilmStorage {

    private final DataSource dataSource;
    private static final LocalDate START_FILMS = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmDbStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Film changeFilm(Film film) {
        Film thisFilm = getFilmById(film.getId());
        if (thisFilm == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (film.getName() == null) {
            film.setName(thisFilm.getName());
        }
        if (film.getDescription() == null) {
            film.setDescription(thisFilm.getDescription());
        }
        if (film.getDuration() == 0) {
            film.setDuration(thisFilm.getDuration());
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(thisFilm.getReleaseDate());
        }
        if (film.getMpa() == null) {
            film.setMpa(thisFilm.getMpa());
        }
        validateNewFilm(film);
        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.setInt(6, film.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении фильма: " + e.getMessage(), e);
        }
        updateGenresForFilm(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        validateNewFilm(film);
        if (!ratingExists(film.getMpa().getId())) {
            throw new ValidateException("Рейтинг с ID " + film.getMpa().getId() + " не существует");
        }
        String sql = "INSERT INTO film (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    film.setId(generatedKeys.getInt(1));
                }
            }
            addGenresToFilm(film.getId(), film.getGenres());
        } catch (SQLException e) {
            throw new ValidateException("Ошибка при добавлении фильма: " + e.getMessage());
        }
        return film;
    }

    @Override
    public List<Film> allFilms() {
        List<Film> films = new ArrayList<>();
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "       f.rating_id, r.name AS rating_name " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                films.add(mapRowToFilm(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return films;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "       f.rating_id, r.name AS rating_name " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "WHERE f.id = ?;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Film film = mapRowToFilm(resultSet);
                film.setGenres(getGenresForFilm(film.getId()));
                return film;
            } else {
                throw new NotFoundException("Фильм с ID " + id + " не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении фильма по ID: " + id, e);
        }
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        return Film.builder().id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")))
                .genres(getGenresForFilm(rs.getInt("id")))
                .build();
    }

    private Set<Genre> getGenresForFilm(int filmId) {
        Set<Genre> genres = new HashSet<>();
        String sql = "SELECT g.id, g.name FROM film_genre fg JOIN genre g ON fg.genre_id = g.id WHERE fg.film_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, filmId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                genres.add(new Genre(resultSet.getInt("id"), resultSet.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении жанров для фильма с ID: " + filmId, e);
        }
        return genres;
    }

    private void validateNewFilm(Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidateException("Название не может быть пустым.");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new ValidateException("Максимальная длина строки - 200 символов.");
        }
        if (film.getDuration() < 1) {
            throw new ValidateException("Продолжительность не может быть отрицательной.");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(START_FILMS)) {
            throw new ValidateException("Релиз не может быть раньше 28 декабря 1985 года.");
        }
    }

    private void updateGenresForFilm(int filmId, Set<Genre> genres) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.setInt(1, filmId);
                deleteStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                for (Genre genre : genres) {
                    insertStatement.setInt(1, filmId);
                    insertStatement.setInt(2, genre.getId());
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении жанров фильма: " + e.getMessage(), e);
        }
    }

    private void addGenresToFilm(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Genre genre : genres) {
                if (!genreExists(genre.getId())) {
                    throw new ValidateException("Жанр с ID " + genre.getId() + " не существует");
                }
                statement.setInt(1, filmId);
                statement.setInt(2, genre.getId());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении жанров к фильму: " + e.getMessage(), e);
        }
    }

    private boolean ratingExists(int ratingId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, ratingId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean genreExists(int genreId) {
        String sql = "SELECT COUNT(*) FROM genre WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, genreId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}