package geektime.tdd;

import geektime.tdd.model.StudentRepository;
import geektime.tdd.resources.StudentsResource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    public Application() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
        EntityManager manager = factory.createEntityManager();
        StudentRepository repository = new StudentRepository(manager);

        ResourceConfig config = new ResourceConfig();
        config.register(StudentsResource.class);
        config.register(repository);
    }
}
