package chinesecheckers.service;

import chinesecheckers.model.Board;
import chinesecheckers.model.Game;
import chinesecheckers.model.Move;
import chinesecheckers.repository.BoardRepository;
import chinesecheckers.repository.GameRepository;
import chinesecheckers.repository.MoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private BoardRepository boardRepository;

    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }

    public Move saveMove(Move move) {
        return moveRepository.save(move);
    }

    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    public List<Move> getMovesByGame(Game game) {
        return moveRepository.findByGame(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    public List<Move> getMovesByGameId(Long gameId) {
        Game game = getGameById(gameId);
        return game != null ? moveRepository.findByGame(game) : null;
    }

    public Board saveBoard(Board board) {
        System.out.println("Zapisuję boardModel do bazy danych:");
        System.out.println(board.getState());
        return boardRepository.save(board);
    }

    public Board getBoardByGame(Game game) {
        return boardRepository.findByGame(game);
    }
}