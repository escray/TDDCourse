package geektime.tdd;

import geektime.tdd.model.Student;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new geektime.tdd.Application();
    }

    @Test
    public void should_fetch_all_students_from_api() {
        Student[] students = target("students").request().get(Student[].class);
        assertEquals(3, students.length);
        assertEquals("john", students[0].getFirstName());
        assertEquals("smith", students[0].getLastName());
        assertEquals("john.smith@email.com", students[0].getEmail());
        assertEquals(1, students[0].getId());
    }

    @Test
    public void should_be_able_fetch_student_by_id() {
        Student student = target("students/1").request().get(Student.class);
        assertEquals("john", student.getFirstName());
        assertEquals("smith", student.getLastName());
        assertEquals("john.smith@email.com", student.getEmail());
        assertEquals(1, student.getId());
    }

    @Test
    public void should_return_404_if_no_student_found() {
        Response response = target("students/99").request().get(Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
