package geektime.tdd.api;

import geektime.tdd.StudentsResource;
import geektime.tdd.api.model.StudentRepository;
import jakarta.persistence.EntityManager;
import org.glassfish.jersey.server.ResourceConfig;

public class StudentApplication extends ResourceConfig {

    private EntityManager manager;
    private StudentRepository repository;

    public StudentApplication(EntityManager manager) {
        this.manager = manager;
        ResourceConfig config = new ResourceConfig();
        config.register(StudentsResource.class);
//        config.register(new StudentRepository(manager));
    }

    public StudentApplication(StudentRepository repository) {

        this.repository = repository;
    }
}
