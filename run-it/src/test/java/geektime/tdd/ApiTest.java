package geektime.tdd;

import geektime.tdd.model.Student;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ApiTest extends JerseyTest {
    Student origin;

    @BeforeEach
    public void setUp() {
        origin = new Student(1, "john", "smith", "john.smith@email.com");
    }

    @Override
    protected Application configure() {
        return new geektime.tdd.Application();
    }

    @Test
    public void should_fetch_all_students_from_api() {
        Student[] students = target("students").request().get(Student[].class);
        assertEquals(3, students.length);
        Student loaded = students[0];
        assertSameStudent(origin, loaded);
    }

    private void assertSameStudent(Student origin, Student loaded) {
        assertEquals(origin.getFirstName(), loaded.getFirstName());
        assertEquals(origin.getLastName(), loaded.getLastName());
        assertEquals(origin.getEmail(), loaded.getEmail());
        assertEquals(origin.getId(), loaded.getId());
    }

    @Test
    public void should_be_able_fetch_student_by_id() {
        Student student = target("students/1").request().get(Student.class);
        assertSameStudent(origin, student);
    }

    @Test
    public void should_return_404_if_no_student_found() {
        Response response = target("students/99").request().get(Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void should_create_student_via_api() {
        Student[] before = target("students").request().get(Student[].class);
        assertEquals(3, before.length);

        Form form = new Form();
        form.param("students[first_name]", "Hannah")
                .param("students[last_name]", "Abbott")
                .param("students[email]", "hannah.abbott@hogwarts.edu");
        form.param("students[first_name]", "Cuthbert")
                .param("students[last_name]", "Binns")
                .param("students[email]", "cuthbert.binns@hogwarts.edu");
        Response response = target("students").request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Student[] after = target("students").request().get(Student[].class);
        assertEquals(5, after.length);
        assertSameStudent(new Student(4, "Hannah", "Abbott", "hannah.abbott@hogwarts.edu"), after[4]);
        assertSameStudent(new Student(5, "Cuthbert", "Binns", "cuthbert.binns@hogwarts.edu"), after[5]);
    }
}
