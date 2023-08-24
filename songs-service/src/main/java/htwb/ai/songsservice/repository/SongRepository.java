package htwb.ai.songsservice.repository;

import htwb.ai.songsservice.model.Song;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends CrudRepository<Song, Long> {
    // No additional methods required
}
