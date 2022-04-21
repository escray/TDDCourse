package geektime.tdd;

import geektime.tdd.api.StudentApplication;
import geektime.tdd.api.model.Student;
import geektime.tdd.api.model.StudentRepository;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiLondonTest extends JerseyTest {
    private static StudentRepository repository = mock(StudentRepository.class);

    @Override
    protected Application configure() {
        return new StudentApplication(repository);
    }

    @Test
    public void should_fetch_all_students_from_api() {
        when(repository.findAll()).thenReturn(Arrays.asList(
           new Student("John", "Smith", "john.smith@email.com"),
           new Student("John", "Smith", "john.smith@email.com")
        ));

        Student[] students = target("students").request().get(Student[].class);
        assertEquals(1, students.length);
        assertEquals("John", students[0].getFirstName());
        assertEquals("Smith", students[0].getLastName());
        assertEquals("john.smith@email.com", students[0].getEmail());
    }

    @Test
    public void should_fetch_student_by_id() {
        when(repository.findBy(eq(1L))).thenReturn(
                new Student("John", "Smith", "john.smith@email.com"));

        Student student = target("students/1").request().get(Student.class);

        assertEquals("John", student.getFirstName());
        assertEquals("Smith", student.getLastName());
        assertEquals("john.smith@email.com", student.getEmail());
    }
}
