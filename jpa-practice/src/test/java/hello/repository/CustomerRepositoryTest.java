package hello.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import hello.domain.Customer;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
class CustomerRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveTest() {
        customerRepository.save(new Customer("test", "test"));
        final Iterable<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        assertThat(customers.iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    void persistTest() {
        Customer customer = new Customer("first name", "last name");
        assertThat(entityManager.contains(customer)).isFalse();

        entityManager.persist(customer);
        assertThat(entityManager.contains(customer)).isTrue();

        String sql = "select * from customer";
        final List<Customer> afterCustomers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));
        assertThat(afterCustomers).hasSize(0);
    }

    @Test
    void flush() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        assertThat(entityManager.contains(customer)).isTrue();

        String sql = "select * from customer";
        final List<Customer> customers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));
        assertThat(customers).hasSize(0);

        entityManager.flush();
        assertThat(entityManager.contains(customer)).isTrue(); // 플러시 했음에도 여전히 엔티티 매니저(영속성 컨텍스트)에 남아있음.

        List<Customer> afterCustomers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));
        assertThat(afterCustomers).hasSize(1);
    }

    @Test
    void detach() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        assertThat(entityManager.contains(customer)).isTrue();

        entityManager.detach(customer);
        assertThat(entityManager.contains(customer)).isFalse();
    }

    @Test
    void detachAgainstPersist() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        entityManager.flush();

        System.out.println("영속화");

        entityManager.detach(customer); // <-- 준영속 상태로 변경
        customer.updateName("new first name", "new last name");
        entityManager.flush(); // 쿼리 발생X

        System.out.println("준 영속화");

        String sql = "select * from customer";
        List<Customer> customers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        assertThat(customers.get(0).getFirstName()).isEqualTo("first name");
    }

    @Test
    void persistAgainstDetach() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        entityManager.flush();

        customer.updateName("new first name", "new last name");
        entityManager.flush(); // update 쿼리 실행됨(영속상태인 객체의 변경이 일어났으므로)

        String sql = "select * from customer";
        List<Customer> customers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        assertThat(customers.get(0).getFirstName()).isEqualTo("new first name");
    }

    @Test
    void merge() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        entityManager.flush();

        entityManager.detach(customer); // <-- 준영속 상태로 변경
        customer.updateName("new first name", "new last name");

        System.out.println("before Merge");
        entityManager.merge(customer); // <-- 다시 영속 상태로 변경
        System.out.println("after Merge");
        entityManager.flush();
        System.out.println("after flush");

        String sql = "select * from customer";
        List<Customer> customers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        System.out.println("after JDBC Query");

        assertThat(customers.get(0).getFirstName()).isEqualTo("new first name");
    }

    @Test
    void remove() {
        Customer customer = new Customer("first name", "last name");
        entityManager.persist(customer);
        entityManager.flush();

        String sql = "select * from customer";
        List<Customer> customers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        assertThat(customers.size()).isEqualTo(1);

        System.out.println("before Remove");
        entityManager.remove(customer);
        System.out.println("after Remove");

        List<Customer> afterRemoveCustomers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        assertThat(afterRemoveCustomers.size()).isEqualTo(1);

        System.out.println("before Flush");
        entityManager.flush();
        System.out.println("after Flush");

        List<Customer> afterRemoveAndFlushCustomers = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));

        assertThat(afterRemoveAndFlushCustomers.size()).isEqualTo(0);
    }
}
