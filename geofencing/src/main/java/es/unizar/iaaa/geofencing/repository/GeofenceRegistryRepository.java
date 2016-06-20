package es.unizar.iaaa.geofencing.repository;

import org.springframework.data.repository.CrudRepository;

import es.unizar.iaaa.geofencing.model.GeofenceRegistry;

public interface GeofenceRegistryRepository extends CrudRepository<GeofenceRegistry, Long> {

    GeofenceRegistry findFirstByUserIdOrderByDateDesc(Long user_id);
}