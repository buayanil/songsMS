package htwb.ai.songsservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    private long id;

    private String title;
    private String artist;
    private String album;
    private int released;

    public Song() {
    }

    public Song(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.artist = builder.artist;
        this.album = builder.album;
        this.released = builder.released;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getReleased() {
        return released;
    }

    public static class Builder {
        private long id;
        private String title;
        private String artist;
        private String album;
        private int released;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder album(String album) {
            this.album = album;
            return this;
        }

        public Builder released(int released) {
            this.released = released;
            return this;
        }

        public Song build() {
            return new Song(this);
        }
    }

    @Override
    public String toString() {
        return "Song [id=" + id + ", title=" + title + ", artist=" + artist + ", album=" + album + ", released=" + released + "]";
    }
}
