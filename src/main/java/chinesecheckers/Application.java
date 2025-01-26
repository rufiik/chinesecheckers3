package chinesecheckers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import chinesecheckers.server.ServerGUI;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        // Uruchomienie GUI
        ServerGUI serverGUI = context.getBean(ServerGUI.class);
        new Thread(serverGUI::waitForWindowClose).start();

        // Serwer uruchomi się automatycznie
    }
}