package org.app.common.support.weblayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.boot.test.autoconfigure.json.JsonTest
public class JsonTest {

    @Autowired
    private JacksonTester<User> jacksonTester;

    @Test
    void shouldSerializeUser() throws IOException {
        User user = new User(1L, "tester");
        assertThat(jacksonTester.write(user))
            .hasJsonPathNumberValue("@.id")
            .hasJsonPathStringValue("@.name")
            .extractingJsonPathStringValue("@.name").isEqualTo("tester");
    }
}
