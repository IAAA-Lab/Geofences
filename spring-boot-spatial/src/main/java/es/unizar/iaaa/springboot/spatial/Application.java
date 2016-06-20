package es.unizar.iaaa.springboot.spatial;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    CustomerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Override
    public void run(String... strings) throws Exception {
        // save a couple of customers
        repository.save(new Customer("Jack", "Bauer", null));
        repository.save(new Customer("Chloe", "O'Brian", null));
        repository.save(new Customer("Kim", "Bauer", null));
        repository.save(new Customer("David", "Palmer", null));
        repository.save(new Customer("Michelle", "Dessler", null));

        repository.save(new Customer("Roger", "Rabbit", wktToGeometry("POINT(-105 40)")));

        // fetch all customers
        System.out.println("Customers found with findAll():");
        System.out.println("-------------------------------");

        for(Customer c: repository.findAll()) {
            System.out.println(c);
        }
        System.out.println();

        // fetch an individual customer by ID
        Customer customer = repository.findOne(1L);
        System.out.println("Customer found with findOne(1L):");
        System.out.println("--------------------------------");
        System.out.println(customer);
        System.out.println();

        // fetch customers by last name
        System.out.println("Customer found with findByLastName('Bauer'):");
        System.out.println("--------------------------------------------");

        repository.findByLastName("Bauer").forEach(System.out::println);
        System.out.println();

        System.out.println("Customers found within POLYGON((-107 39, -102 39, -102 41, -107 41, -107 39)):");
        System.out.println("--------------------------------");
        repository.findWithin(wktToGeometry("POLYGON((-107 39, -102 39, -102 41, -107 41, -107 39))")).forEach(System.out::println);
        System.out.println();
    }

    //utility method to create a Geometry from a WKT string
    private Geometry wktToGeometry(String wktString) {
        WKTReader fromText = new WKTReader();
        Geometry geom = null;
        try {
            geom = fromText.read(wktString);
        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktString);
        }
        return geom;
    }
}