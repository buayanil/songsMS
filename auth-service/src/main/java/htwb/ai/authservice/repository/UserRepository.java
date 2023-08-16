package htwb.ai.authservice.repository;

import htwb.ai.authservice.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findUsersByUserId(String userId);
    List<User> findUserByUserId(String userId);

    @Query(value = "SELECT * FROM UserTable WHERE userid = ?", nativeQuery = true)
    List<User> selectUserByUserId(String userId);
    @Query(value = "SELECT * FROM UserTable WHERE userid = ? LIMIT 1", nativeQuery = true)
    User selectUserId(String userId);

    @Query(value = "SELECT * FROM UserTable WHERE userid = ?1 AND password = ?2 LIMIT 1", nativeQuery = true)
    User findByUsernameAndPassword(String userId, String password);

    @Query(value = "SELECT * FROM UserTable WHERE userid = ?1 LIMIT 1", nativeQuery = true)
    User findByUserId(String userId);
}

