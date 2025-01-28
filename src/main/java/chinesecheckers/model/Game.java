package chinesecheckers.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String variant;
    private int maxPlayers;
    private int humanPlayers;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Move> moves;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL)
    private Board board;

    @ElementCollection
    @CollectionTable(name = "player_order", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "player_id")
    private List<Integer> playerOrder;
    // Gettery i settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getHumanPlayers() {
        return humanPlayers;
    }

    public void setHumanPlayers(int humanPlayers) {
        this.humanPlayers = humanPlayers;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
    public List<Integer> getPlayerOrder() {
        return playerOrder;
    }

    public void setPlayerOrder(List<Integer> playerOrder) {
        this.playerOrder = playerOrder;
    }
}