package com.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestData {

    // Константы для тестовых данных
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_FIRST_NAME = "TestName";
    private static final String ORDER_FIRST_NAME = "Naruto";
    private static final String ORDER_LAST_NAME = "Uzumaki";
    private static final String ORDER_ADDRESS = "Konoha, 142 apt.";
    private static final String ORDER_METRO_STATION = "4";
    private static final String ORDER_PHONE = "+7 800 355 35 35";
    private static final int ORDER_RENT_TIME = 5;
    private static final String ORDER_DELIVERY_DATE = "2023-12-31";
    private static final String ORDER_COMMENT = "Test comment";
    private static final List<String> DEFAULT_COLORS = Collections.singletonList("BLACK");

    /**
     * Создаёт случайного курьера с уникальным логином
     */
    public static Courier getRandomCourier() {
        String uniqueLogin = "courier" + System.currentTimeMillis() + "_" +
                (int)(Math.random() * 1000);
        return new Courier(uniqueLogin, DEFAULT_PASSWORD, DEFAULT_FIRST_NAME);
    }

    /**
     * Создаёт стандартный заказ
     */
    public static Order getRandomOrder() {
        return new Order(
                ORDER_FIRST_NAME,
                ORDER_LAST_NAME,
                ORDER_ADDRESS,
                ORDER_METRO_STATION,
                ORDER_PHONE,
                ORDER_RENT_TIME,
                ORDER_DELIVERY_DATE,
                ORDER_COMMENT,
                DEFAULT_COLORS
        );
    }

    /**
     * Класс курьера
     */
    public static class Courier {
        private String login;
        private String password;
        private String firstName;

        public Courier(String login, String password, String firstName) {
            this.login = login;
            this.password = password;
            this.firstName = firstName;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String toJson() {
            StringBuilder json = new StringBuilder("{");
            boolean hasPrevious = false;

            if (login != null) {
                json.append("\"login\":\"").append(login).append("\"");
                hasPrevious = true;
            }

            if (password != null) {
                if (hasPrevious) json.append(",");
                json.append("\"password\":\"").append(password).append("\"");
                hasPrevious = true;
            }

            if (firstName != null) {
                if (hasPrevious) json.append(",");
                json.append("\"firstName\":\"").append(firstName).append("\"");
            }

            json.append("}");
            return json.toString();
        }
    }

    /**
     * Класс заказа
     */
    public static class Order {
        private String firstName;
        private String lastName;
        private String address;
        private String metroStation;
        private String phone;
        private int rentTime;
        private String deliveryDate;
        private String comment;
        private List<String> color;

        public Order(String firstName, String lastName, String address,
                     String metroStation, String phone, int rentTime,
                     String deliveryDate, String comment, List<String> color) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.metroStation = metroStation;
            this.phone = phone;
            this.rentTime = rentTime;
            this.deliveryDate = deliveryDate;
            this.comment = comment;
            this.color = color;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getAddress() {
            return address;
        }

        public String getMetroStation() {
            return metroStation;
        }

        public String getPhone() {
            return phone;
        }

        public int getRentTime() {
            return rentTime;
        }

        public String getDeliveryDate() {
            return deliveryDate;
        }

        public String getComment() {
            return comment;
        }

        public List<String> getColor() {
            return color;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setMetroStation(String metroStation) {
            this.metroStation = metroStation;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public void setRentTime(int rentTime) {
            this.rentTime = rentTime;
        }

        public void setDeliveryDate(String deliveryDate) {
            this.deliveryDate = deliveryDate;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public void setColor(List<String> color) {
            this.color = color;
        }

        public String toJson() {
            StringBuilder json = new StringBuilder("{");

            json.append("\"firstName\":\"").append(firstName).append("\",");
            json.append("\"lastName\":\"").append(lastName).append("\",");
            json.append("\"address\":\"").append(address).append("\",");
            json.append("\"metroStation\":\"").append(metroStation).append("\",");
            json.append("\"phone\":\"").append(phone).append("\",");
            json.append("\"rentTime\":").append(rentTime).append(",");
            json.append("\"deliveryDate\":\"").append(deliveryDate).append("\",");
            json.append("\"comment\":\"").append(comment).append("\"");

            if (color != null && !color.isEmpty()) {
                json.append(",\"color\":[");
                for (int i = 0; i < color.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(color.get(i)).append("\"");
                }
                json.append("]");
            }

            json.append("}");
            return json.toString();
        }
    }
}