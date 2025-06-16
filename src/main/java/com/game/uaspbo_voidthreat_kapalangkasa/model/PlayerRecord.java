package com.game.uaspbo_voidthreat_kapalangkasa.model;

public class PlayerRecord {
    private final String no;
    private final String playerName;
    private final String score;

    public PlayerRecord(String no, String playerName, String score){
        this.no = no;
        this.playerName = playerName;
        this.score = score;
    }

    public String getNo(){ return no; }
    public String getPlayerName(){ return playerName; }
    public String getScore(){ return score; }

}
