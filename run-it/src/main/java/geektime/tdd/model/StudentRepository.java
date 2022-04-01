package geektime.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.*;

public class StudentRepository {
    private EntityManager manager;
    private List<Student> students;

    public StudentRepository() {
        this.students = new ArrayList<>();
    }

    public StudentRepository(EntityManager manager) {
        this.manager = manager;
    }

    public StudentRepository(Student... students) {
//        this.students = Arrays.asList(students);
        this.students = new ArrayList<>(Arrays.asList(students));
    }

    public Student saveToDB(Student student) {
        manager.persist(student);
        return student;
    }

    public Optional<Student> findByIdFromDB(long id) {
        return Optional.ofNullable(manager.find(Student.class, id));
    }

    public Optional<Student> findByEmail(String email) {
        TypedQuery<Student> query = manager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
        return query.setParameter("email", email).getResultList().stream().findFirst();
    }

    public Optional<Student> findById(long id) {
        return students.stream().filter(it -> it.getId() == id).findFirst();
    }

    public void save(Student student) {

        if (student.getId() == 0) {
            student = new Student(students.size() == 0 ? 1L : students.get(students.size() - 1).getId() + 1,
                    student.getFirstName(), student.getLastName(), student.getEmail());
        }

        students.add(student);
    }

    public List<Student> all() {
        return Collections.unmodifiableList(students);
    }

}
