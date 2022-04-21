package geektime.tdd;

import geektime.tdd.api.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;

public class ApiTest {
    private static EntityManagerFactory factory = Persistence.createEntityManagerFactory("student");
    private static EntityManager manager = factory.createEntityManager();

    @BeforeAll
    public static void before() {
        manager.getTransaction().begin();
        manager.persist(new Student("John", "Smith", "john.smith@email.com"));
        manager.getTransaction().commit();
    }

    @Override
    protected Application configure() {
        return new StudentApplication(manager);
    }


}
