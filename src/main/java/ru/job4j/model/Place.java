package ru.job4j.model;

import java.util.Objects;

/**
 * Класс Place - описывает модель данных места в зале.
 *
 * @author Evgeniy Zaytsev
 * @version 1.0
 */
public class Place {
    private int id;
    private int sessionId;
    private int row;
    private int cell;
    private User user;

    public Place() {
    }

    public Place(int id, int sessionId, int row, int cell, User user) {
        this.id = id;
        this.sessionId = sessionId;
        this.row = row;
        this.cell = cell;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCell() {
        return cell;
    }

    public void setCell(int cell) {
        this.cell = cell;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Place place = (Place) o;
        return id == place.id && sessionId == place.sessionId && row == place.row && cell == place.cell;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sessionId, row, cell);
    }

    @Override
    public String toString() {
        return "Place{"
                + "id=" + id
                + ", sessionId=" + sessionId
                + ", row=" + row
                + ", cell=" + cell
                + ", user=" + user
                + '}';
    }
}
