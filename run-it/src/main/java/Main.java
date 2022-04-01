import entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;

public class Main {
    public static void main(String[] args) {
        insertEmployee();
    }

    private static void insertEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("Hermione");
        employee.setLastName("Granger");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(employee);
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
    }
}