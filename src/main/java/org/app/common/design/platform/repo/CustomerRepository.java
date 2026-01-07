package org.app.common.design.platform.repo;

import org.app.common.design.platform.domain.model.customer.Customer;
import org.app.common.design.platform.domain.model.customer.CustomerTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByTier(CustomerTier tier);
}
