package ru.job4j.service;

import ru.job4j.model.Place;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс Service - объявляет методы для работы с хранилищем.
 *
 * @author Evgeniy Zaytsev
 * @version 1.0
 */
public interface Service {

    Collection<Place> findAllPlaces();

    boolean byuTicket(List<Place> placeList);

}
