package chinesecheckers.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import chinesecheckers.patterns.Observer;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {
    private GameServer server;
    private Observer observer;
    private ServerSocket mockServerSocket;

    @BeforeEach
    void setUp() {
        ServerGUI gui = mock(ServerGUI.class);
        when(gui.getSelectedPlayers()).thenReturn(2);
        server = GameServer.getInstance(12345, gui);
        observer = mock(Observer.class);
        mockServerSocket = mock(ServerSocket.class);
    }

    @Test
    void testAddObserver() {
        server.addObserver(observer);
        List<Observer> observers = server.getObservers();
        assertTrue(observers.contains(observer));
    }

    @Test
    void testRemoveObserver() {
        server.addObserver(observer);
        server.removeObserver(observer);
        List<Observer> observers = server.getObservers();
        assertFalse(observers.contains(observer));
    }

    @Test
    void testNotifyObservers() {
        server.addObserver(observer);
        server.notifyObservers("Test message");
        verify(observer, times(1)).update("Test message");
    }
    @Test
    void testStart() throws IOException {
    
        doNothing().when(mockServerSocket).close();
        server.startWithMockSocket(mockServerSocket);
        assertTrue(server.isRunning());
    }

    @Test
    void testStartWhenPortAlreadyInUse() throws IOException {

        doThrow(new BindException()).when(mockServerSocket).bind(any());
        server.startWithMockSocket(mockServerSocket);
        assertFalse(server.isRunning());
    }
    @Test
    void testBroadcastMessage() {
        server.addObserver(observer);
        server.broadcastMessage("Test message");
        verify(observer, times(1)).update("Test message");
    }

}