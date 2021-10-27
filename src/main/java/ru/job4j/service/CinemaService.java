package ru.job4j.service;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.job4j.model.Place;
import ru.job4j.model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Класс CinemaService - описывает сервис по хранению информации
 * о заказанных и свободных билетах, а также о зарегистрированных
 * пользователях в базе данных.
 *
 * @author Evgeniy Zaytsev
 * @version 1.0
 */
public class CinemaService implements Service {

    private static final Logger LOG = LogManager.getLogger(CinemaService.class.getName());

    private final BasicDataSource pool = new BasicDataSource();

    private CinemaService() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(CinemaService.class.getClassLoader()
                                .getResourceAsStream("db.properties"))
                )
        )) {
            cfg.load(io);
        } catch (Exception e) {
            LOG.error("IO Error:", e);
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            LOG.error("Database driver error:", e);
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Service INST = new CinemaService();
    }

    public static Service instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Place> findAllPlaces() {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT t.id, t.session_id, t.row, t.cell, a.id AS user_id, "
                + "a.username, a.email, a.phone FROM ticket t "
                + "JOIN account a ON a.id = t.account_id";
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    places.add(new Place(
                            it.getInt("id"),
                            it.getInt("session_id"),
                            it.getInt("row"),
                            it.getInt("cell"),
                            new User(
                                    it.getInt("user_id"),
                                    it.getString("username"),
                                    it.getString("email"),
                                    it.getString("phone")
                            )));
                }
            }
        } catch (Exception e) {
            LOG.error("Database error:", e);
        }
        return places;
    }

    @Override
    public boolean byuTicket(List<Place> placeList) {
        boolean rsl = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (Place place : placeList) {
            stringBuilder
                    .append("INSERT INTO ticket (session_id, row, cell, account_id) ")
                    .append("VALUES (")
                    .append(place.getSessionId()).append(", ")
                    .append(place.getRow()).append(", ")
                    .append(place.getCell()).append(", ")
                    .append(saveUser(place.getUser())).append(");")
                    .append(System.lineSeparator());
        }
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(stringBuilder.toString())) {
            cn.setAutoCommit(false);
            if (ps.executeUpdate() > 0) {
                cn.commit();
                rsl = true;
            }
            cn.setAutoCommit(true);
        } catch (SQLException e) {
            LOG.error("Database error:", e);
        }
        return rsl;
    }

    private int saveUser(User user) {
        String sql = "INSERT INTO account (username, email, phone) "
                + "VALUES (?, ?, ?)";
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.execute();
            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getInt("id"));
                }
            }
        } catch (SQLException e) {
            LOG.error("Database error:", e);
        }
        if (user.getId() != 0) {
            return user.getId();
        }
        return getIdExistingUser(user);
    }

    private int getIdExistingUser(User user) {
        String sql = "SELECT id FROM account WHERE email = ? AND phone = ?";
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPhone());
            ps.execute();
            try (ResultSet resultSet = ps.getResultSet()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getInt("id"));
                }
            }
        } catch (SQLException e) {
            LOG.error("Database error:", e);
        }
        return user.getId();
    }

}
