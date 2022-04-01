package geektime.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        public Optional<Student> findById(long id) {
            return Optional.ofNullable(manager.find(Student.class, id));
        }

        public Optional<Student> findByEmail(String email) {
            TypedQuery<Student> query = manager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
            return query.setParameter("email", email).getResultList().stream().findFirst();
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
        int before = manager.createNativeQuery("select id, first_name from STUDENT s").getResultList().size();

        // exercise
        manager.getTransaction().begin();
        Student saved = students.save(new Student("john", "smith", "john.smith@email.com"));
        manager.getTransaction().commit();

        // verify
        List result = manager.createNativeQuery("select id, first_name, last_name, email from STUDENT s").getResultList();

        // weak point
        assertEquals(before + 1, result.size());

        Object[] john = (Object[]) result.get(0);

        assertEquals(saved.getId(), john[0]);
        assertEquals(saved.getFirstName(), john[1]);
        assertEquals(saved.getLastName(), john[2]);
        assertEquals(saved.getEmail(), john[3]);
    }

    // findById
    @Test
    public void should_be_able_to_load_saved_student_by_id() {
        manager.getTransaction().begin();
        Student john = students.save(new Student("john", "smith", "john.smith@email.com"));
        manager.getTransaction().commit();

        Optional<Student> loaded = students.findById(john.getId());

        assertTrue(loaded.isPresent());

        assertEquals(john.getFirstName(), loaded.get().getFirstName());
        assertEquals(john.getLastName(), loaded.get().getLastName());
        assertEquals(john.getEmail(), loaded.get().getEmail());
        assertEquals(john.getId(), loaded.get().getId());
    }

    // findByEmail
    public void should_be_able_to_load_saved_student_by_email() {
        manager.getTransaction().begin();
        Student john = students.save(new Student("john", "smith", "john.smith@email.com"));
        manager.getTransaction().commit();

        Optional<Student> loaded = students.findByEmail(john.getEmail());

        assertTrue(loaded.isPresent());

        assertEquals(john.getFirstName(), loaded.get().getFirstName());
        assertEquals(john.getLastName(), loaded.get().getLastName());
        assertEquals(john.getEmail(), loaded.get().getEmail());
        assertEquals(john.getId(), loaded.get().getId());
    }
}
