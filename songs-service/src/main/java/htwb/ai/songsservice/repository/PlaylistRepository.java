package htwb.ai.songsservice.repository;

import htwb.ai.songsservice.model.Playlist;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends CrudRepository<Playlist, Integer> {
    @Query(value = "SELECT * FROM playlists WHERE userid = ?", nativeQuery = true)
    List<Playlist> findByUserId(String userId);
    /**
     List<Playlist> findPlaylistsByUserId(String userId);
     List<Playlist> findPublicPlaylistsByUserId(String userId);
     **/
}
