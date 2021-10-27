package ru.job4j.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.model.Place;
import ru.job4j.model.User;
import ru.job4j.service.CinemaService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс HallServlet - сервлет заказа билета.
 *
 * @author Evgeniy Zaytsev
 * @version 1.0
 */
public class HallServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Метод вызывается со страницы заказа билета автоматически
     * с заданной периодичностью и возвращает список занятых мест
     * на указанный сеанс.
     *
     * @param req - запрос
     * @param resp - ответ
     * @throws ServletException - ошибка сервлета
     * @throws IOException - ошибка ввода вывода
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Place> places = (List<Place>) CinemaService.instOf().findAllPlaces();
        int sessionId = Integer.parseInt(req.getParameter("session_id"));
        List<Place> sessionPlaces = new ArrayList<>();
        for (Place place : places) {
            if (sessionId == place.getSessionId()) {
                sessionPlaces.add(place);
            }
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        OutputStream output = resp.getOutputStream();
        String json = GSON.toJson(sessionPlaces);
        output.write(json.getBytes(StandardCharsets.UTF_8));
        output.flush();
        output.close();
    }

    /**
     * Метод вызывается при нажатии на кнопку оплаты билета(ов),
     * отправляет введенные данные в сервис для сохранения в базе данных
     * и переводит на страницу результата.
     *
     * @param req - запрос
     * @param resp - ответ
     * @throws ServletException - ошибка сервлета
     * @throws IOException - ошибка ввода вывода
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        User user = new User(
                0,
                username,
                email,
                phone
        );
        int sessionId = Integer.parseInt(req.getParameter("session_id"));
        String[] placesFromHtml = req.getParameter("places").split(",");
        List<Place> placeList = new ArrayList<>();
        for (String s : placesFromHtml) {
            int row = Integer.parseInt(s.split("")[0]);
            int cell = Integer.parseInt(s.split("")[1]);
            placeList.add(new Place(
                    0,
                    sessionId,
                    row,
                    cell,
                    user
            ));
        }
        if (!CinemaService.instOf().byuTicket(placeList)) {
            req.setAttribute("error", "Ошибка! К сожалению это место(эти места) уже заняты!");
        } else {
            req.setAttribute("rsl", "Успешно! Приятного просмотра!");
        }
        req.getRequestDispatcher("result.jsp").forward(req, resp);
    }
}
