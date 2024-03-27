package kz.yermek.repositories;

import kz.yermek.models.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    @Query("select  t from AccessToken  t inner join User u on t.user.id = u.id where u.id  = :userId and (t.expired = false or t.revoked = false )")
    List<AccessToken> findAllValidTokensByUser(Long userId);
    Optional<AccessToken> findByToken(String token);
}
