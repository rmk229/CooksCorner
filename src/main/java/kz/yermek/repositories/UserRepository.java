package kz.yermek.repositories;

import kz.yermek.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("select u from User u where u.isEnabled = false")
    List<User> findNotEnabledUsers();

    @Query("SELECT c FROM User c WHERE c.name LIKE CONCAT('%', :query, '%')")
    List<User> searchUsers(@Param("query") String query);
}
