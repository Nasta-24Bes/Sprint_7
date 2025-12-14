package org.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sprint 7: Тесты API Яндекс.Самокат");
        System.out.println("=====================================");
        System.out.println("Для запуска тестов выполните в консоли:");
        System.out.println("mvn clean test");
        System.out.println("\nДля генерации отчёта Allure выполните:");
        System.out.println("mvn allure:report");
        System.out.println("\nДля открытия отчёта в браузере:");
        System.out.println("mvn allure:serve");
    }
}