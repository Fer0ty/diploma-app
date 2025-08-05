package diploma.ecommerce.backend.shopbase.config;

//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpRequest;
//import org.springframework.http.client.ClientHttpRequestExecution;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.IOException;
//import java.util.Base64;
//import java.util.UUID;
//
//@Configuration
//@ConfigurationProperties(prefix = "yookassa")
//@Data
public class YooKassaConfig {
//    private String apiUrl = "https://api.yookassa.ru/v3";
//    private String shopId;
//    private String secretKey;
//    private int connectionTimeout = 30000;
//    private int readTimeout = 60000;
//
//    @Bean
//    public RestTemplate yooKassaRestTemplate() {
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Настройка таймаутов
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setConnectTimeout(connectionTimeout);
//        factory.setReadTimeout(readTimeout);
//        restTemplate.setRequestFactory(factory);
//
//        // Добавление Basic Auth interceptor
//        restTemplate.getInterceptors().add(new BasicAuthInterceptor());
//
//        return restTemplate;
//    }
//
//    private class BasicAuthInterceptor implements ClientHttpRequestInterceptor {
//        @Override
//        public ClientHttpResponse intercept(
//                HttpRequest request,
//                byte[] body,
//                ClientHttpRequestExecution execution) throws IOException {
//
//            String auth = shopId + ":" + secretKey;
//            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//
//            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
//            request.getHeaders().add("Content-Type", "application/json");
//            request.getHeaders().add("Idempotence-Key", UUID.randomUUID().toString());
//
//            return execution.execute(request, body);
//        }
//    }
}
