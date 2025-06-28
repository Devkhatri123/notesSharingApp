package com.notesSharingApp.notesSharingApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class NotesSharingAppApplication {

	public static void main(String[] args) {

		SpringApplication.run(NotesSharingAppApplication.class, args);

	}

}
