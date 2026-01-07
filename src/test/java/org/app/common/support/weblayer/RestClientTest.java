package org.app.common.support.weblayer;

import org.app.common.utils.JacksonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Component
class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUser(Long id) {
        return restTemplate
            .getForObject(
                "http://user-service/user/" + id,
                String.class
            );
    }
}

@org.springframework.boot.test.autoconfigure.web.client.RestClientTest(UserClient.class)
public class RestClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldFetchCatImage() {
        server.expect(requestTo("http://order-service/orders/1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    "{\"id\":1,\"name\":\"order1\"}",
                    MediaType.APPLICATION_JSON
                )
            );

        String result = userClient.getUser(1L);

        assertThat(result).contains("order1");
    }

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
