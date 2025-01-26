package chinesecheckers.repository;

import chinesecheckers.model.Board;
import chinesecheckers.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Board findByGame(Game game);
}