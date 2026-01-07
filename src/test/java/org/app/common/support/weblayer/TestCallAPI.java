package org.app.common.support.weblayer;

import org.app.common.utils.JacksonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCallAPI {

    @Test
    void callCatApiDirectly() {
        RestTemplate restTemplate = new RestTemplate();

        String response = restTemplate.getForObject(
            "https://api.thecatapi.com/v1/images/search",
            String.class
        );

        System.out.println(JacksonUtils.toJson(response));

        assertThat(response).isNotNull();
    }
}
