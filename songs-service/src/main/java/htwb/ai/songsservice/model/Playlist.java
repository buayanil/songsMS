package htwb.ai.songsservice.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "isprivate")
    private boolean isPrivate;

    @Column(name = "name")
    private String name;

    private String userid;

    @ManyToMany
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> songs;

    // Private constructor
    private Playlist() {
    }

    // Builder pattern
    public static class Builder {
        private boolean isPrivate;
        private String name;
        private String userid;
        private Set<Song> songs;

        public Builder isPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder user(String userid) {
            this.userid = userid;
            return this;
        }

        public Builder songs(Set<Song> songs) {
            this.songs = songs;
            return this;
        }

        public Playlist build() {
            Playlist playlist = new Playlist();
            playlist.isPrivate = this.isPrivate;
            playlist.name = this.name;
            playlist.userid = this.userid;
            playlist.songs = this.songs;
            return playlist;
        }
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getName() {
        return name;
    }

    public String getUserid() {
        return userid;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }
}

