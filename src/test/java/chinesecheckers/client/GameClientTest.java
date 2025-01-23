package chinesecheckers.client;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.Socket;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GameClientTest {

    @Mock
    private Socket mockSocket;

    @Mock
    private PrintWriter mockOut;

    @Mock
    private BufferedReader mockIn;

    private GameClient gameClient;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        closeable = MockitoAnnotations.openMocks(this);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        
        mockOut = mock(PrintWriter.class);
        mockIn = mock(BufferedReader.class);
        gameClient = new GameClient("localhost", 12345);

        var outField = GameClient.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(gameClient, mockOut);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSendMove() {
        gameClient.sendMove(1, 2, 3, 4);

        verify(mockOut, atLeastOnce()).println("Ruch-1,2:3,4");
    }

    @Test
    void testSkipTurn() {
        gameClient.skipTurn();

        verify(mockOut, atLeastOnce()).println("SKIP TURN");
    }

    @Test
    void testStopConnection() {
        gameClient.stopConnection();

        assertFalse(gameClient.getPlayerTurn());
    }

    @Test
    void testVariantSetting() {
        gameClient.setVariant("Custom");

        assertEquals("Custom", gameClient.getVariant());
    }
}
