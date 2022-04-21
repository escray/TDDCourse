package geektime.tdd.api.model;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll();

    Student findBy(long id);
}
