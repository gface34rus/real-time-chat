package com.example.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Real-time Chat.
 * Запускает Spring Boot приложение.
 */
@SpringBootApplication
public class ChatApplication {

	/**
	 * Точка входа в приложение.
	 * 
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
