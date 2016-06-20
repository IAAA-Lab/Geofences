package es.unizar.iaaa.geofencing.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import es.unizar.iaaa.geofencing.model.Rule;

public interface RuleRepository extends CrudRepository<Rule, Long> {

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN 'true' ELSE 'false' END FROM User u, Rule r" +
            " WHERE r.id = ?1 AND u.nick = ?2 AND u.id = r.geofence.user.id")
    Boolean existsByUsername(Long id, String username);
}