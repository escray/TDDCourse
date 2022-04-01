package geektime.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudentsTest {
    private EntityManagerFactory factory;
    private EntityManager manager;
    private Students students;

    @BeforeEach
    public void before() {
        factory = Persistence.createEntityManagerFactory("default");
        manager = factory.createEntityManager();

        students = new Students(manager);
    }

    static class Students {
        private EntityManager manager;
        public Students(EntityManager manager) {
            this.manager = manager;
        }

        public Student save(Student student) {
            manager.persist(student);
            return student;
        }
    }

    @AfterEach
    public void after() {
        manager.clear();
        manager.close();
        factory.close();
    }

    // save
    @Test
    public void should_save_student_to_db() {
        // exercise
        manager.getTransaction().begin();
        Student saved = students.save(new Student("john", "smith", "john.smith@email.com"));
        manager.getTransaction().commit();

        // verify
        List result = manager.createNativeQuery("select id, first_name, last_name, email from STUDENT s").getResultList();

        assertEquals(1, result.size());

        Object[] john = (Object[]) result.get(0);

//        assertEquals(saved.getId(), john[0]);
        assertEquals(saved.getFirstName(), john[1]);
        assertEquals(saved.getLastName(), john[2]);
        assertEquals(saved.getEmail(), john[3]);
    }
    // findById

    // findByEmail


}
