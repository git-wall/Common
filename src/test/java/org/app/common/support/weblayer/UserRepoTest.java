package org.app.common.support.weblayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

@DataJpaTest
public class UserRepoTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByUsername() {
        User user = new User(1L, "tester");

        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername())
            .isEqualTo("testuser");
    }
}
