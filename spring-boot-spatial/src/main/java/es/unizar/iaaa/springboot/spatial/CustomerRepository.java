package es.unizar.iaaa.springboot.spatial;

import org.springframework.data.repository.CrudRepository;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    @Query("SELECT c FROM Customer c WHERE within(c.geom, ?1) = true")
    List<Customer> findWithin(Geometry filter);
}