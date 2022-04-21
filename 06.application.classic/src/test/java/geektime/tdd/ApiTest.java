package geektime.tdd;

import geektime.tdd.api.StudentApplication;
import geektime.tdd.api.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest extends JerseyTest {
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

    @AfterAll
    public static void after() {
        manager.clear();
        manager.close();
        factory.close();
    }

    @Test
    public void should_fetch_students_from_api() {
        Student[] students = target("students").request().get(Student[].class);
        assertEquals(1, students.length);
        assertEquals("John", students[0].getFirstName());
        assertEquals("Smith", students[0].getLastName());
        assertEquals("john.smith@email.com", students[0].getEmail());
        assertEquals(1, students[0].getId());
    }
}