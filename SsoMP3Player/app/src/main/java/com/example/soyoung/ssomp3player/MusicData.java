package com.example.soyoung.ssomp3player;

public class MusicData {

    private String title;
    private String singer;
    private String genre;
    private String score;
    private int album;

    public MusicData(String title, String singer, String genre, String score, int album) {
        this.title = title;
        this.singer = singer;
        this.genre = genre;
        this.score = score;
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }
}
