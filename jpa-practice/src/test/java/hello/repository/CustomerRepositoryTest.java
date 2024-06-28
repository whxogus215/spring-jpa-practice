package hello.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import hello.domain.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveTest() {
        customerRepository.save(new Customer("test", "test"));
        final Iterable<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(1);
        assertThat(customers.iterator().next().getId()).isEqualTo(1L);
    }
}
