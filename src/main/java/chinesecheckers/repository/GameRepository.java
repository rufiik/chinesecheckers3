package chinesecheckers.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import chinesecheckers.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {

}



