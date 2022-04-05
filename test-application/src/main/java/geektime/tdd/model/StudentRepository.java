package geektime.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Optional;

public class StudentRepository {
    private EntityManager manager;

    public StudentRepository(EntityManager manager) {
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
        // Criteria API
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Student> criteria = builder.createQuery(Student.class);

        Root<Student> student = criteria.from(Student.class);
        return manager.createQuery(criteria.where(builder.equal(student.get("email"), email)).select(student))
                .getResultList().stream().findFirst();
        // JPQL
//        TypedQuery<Student> query = manager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
//        return query.setParameter("email", email).getResultList().stream().findFirst();
    }
}
