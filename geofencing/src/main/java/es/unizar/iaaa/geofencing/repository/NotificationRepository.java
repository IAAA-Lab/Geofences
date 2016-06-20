package es.unizar.iaaa.geofencing.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

import es.unizar.iaaa.geofencing.model.Notification;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.nick = ?1 ORDER BY n.date DESC")
    List<Notification> find(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN 'true' ELSE 'false' END FROM User u, Notification n" +
            " WHERE n.id = ?1 AND u.nick = ?2 AND u.id = n.user.id")
    Boolean existsByUsername(Long id, String username);
}