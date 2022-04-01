package geektime.tdd.resources;

import geektime.tdd.model.Student;
import jakarta.ws.rs.core.Form;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormHelperTest {

    @Test
    public void should_read_students_from_form() {
        Form form = new Form();
        form.param("students[first_name]", "Hannah")
                .param("students[last_name]", "Abbott")
                .param("students[email]", "hannah.abbott@hogwarts.edu");
        form.param("students[first_name]", "Cuthbert")
                .param("students[last_name]", "Binns")
                .param("students[email]", "cuthbert.binns@hogwarts.edu");

        Student[] students = FormHelper.toStudents(form.asMap()).toArray(Student[]::new);

        assertEquals(2, students.length);

        assertEquals("Hannah", students[0].getFirstName());
        assertEquals("Abbott", students[0].getLastName());
        assertEquals("hannah.abbott@hogwarts.edu", students[0].getEmail());
    }
}
