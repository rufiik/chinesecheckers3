package chinesecheckers.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientHandlerTest {
    private ClientHandler clientHandler;
    private Socket mockSocket;
    private PrintWriter mockOut;
    private BufferedReader mockIn;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockOut = mock(PrintWriter.class);
        mockIn = mock(BufferedReader.class);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        clientHandler = new ClientHandler(mockSocket, 1, 2, "Rozgrywka klassyczna");
        clientHandler.setOut(mockOut);
        clientHandler.setIn(mockIn);
    }

    @Test
    void testStartMessage() throws IOException {
        clientHandler.sendMessage("Witaj, Graczu 1!");
        clientHandler.sendMessage("PLAYER_ID:1");
        clientHandler.sendMessage("Liczba graczy:2");

        verify(mockOut).println("Witaj, Graczu 1!");
        verify(mockOut).println("PLAYER_ID:1");
        verify(mockOut).println("Liczba graczy:2");
    }

    @Test
    void testSendMessage() throws IOException {
        clientHandler.sendMessage("Test message");
        verify(mockOut).println("Test message");
    }

    @Test
    void testReceiveMessage() throws IOException {
        when(mockIn.readLine()).thenReturn("Test message");
        String message = clientHandler.receiveMessage();
        assertEquals("Test message", message);
    }
    @Test
    void testUpdate(){
        String message = "Test message";
        clientHandler.update(message);
        verify(mockOut).println(message);
    }
    @Test
    void testSendGameState() throws IOException {
        Board board = new Board();
        clientHandler.sendGameState(board);
        verify(mockOut).println("Stan planszy:" + board.toString());
    }
    @Test
    void testIsConnected() throws IOException {
        doNothing().when(mockSocket).sendUrgentData(0xFF);
        assertTrue(clientHandler.isConnected());
    }
    @Test
    void testIsNotConnected() throws IOException {
        doThrow(new IOException()).when(mockSocket).sendUrgentData(0xFF);
        assertFalse(clientHandler.isConnected());
    }

    @Test
    void testClose() throws IOException {
        clientHandler.close();
        verify(mockSocket).close();
        verify(mockIn).close();
        verify(mockOut).close();
    }
    @Test
    void testGetPlayerId(){
        assertEquals(1, clientHandler.getPlayerId());
    }
    @Test
    void testGetMaxPlayers(){
        assertEquals(2, clientHandler.getMaxPlayers());
    }
    @Test
    void testGetOut(){
        assertEquals(mockOut, clientHandler.getOut());
    }
    @Test
    void testGetIn(){
        assertEquals(mockIn, clientHandler.getIn());
    }

}