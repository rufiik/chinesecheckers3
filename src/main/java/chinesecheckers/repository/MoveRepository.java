package chinesecheckers.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import chinesecheckers.model.Game;
import chinesecheckers.model.Move;
import java.util.List;
public interface MoveRepository extends JpaRepository<Move, Long> {
        List<Move> findByGame(Game game);
}