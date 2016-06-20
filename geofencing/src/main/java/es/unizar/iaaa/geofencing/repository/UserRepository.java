package es.unizar.iaaa.geofencing.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import es.unizar.iaaa.geofencing.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByNick(String username);

    @Modifying
    @Transactional
    Integer deleteByNick(String username);
}