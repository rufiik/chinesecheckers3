package chinesecheckers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import chinesecheckers.server.GameServer;
import chinesecheckers.service.GameService;
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        // Uruchomienie serwera
        GameServer server = context.getBean(GameServer.class);
        server.initialize(12345); // Ustawienie portu
        new Thread(server::start).start();
    }
}