package com.bms.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8082");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("BMS Payment Service");
        contact.setEmail("support@bms.com");

        Info info = new Info()
                .title("BMS Payment Service API")
                .version("1.0.0")
                .description("""
                        # BMS Payment Service

                        This service handles payment processing using Stripe for the BMS (Building Management System) application.

                        ## Features
                        - Card payments (Credit/Debit cards)
                        - ACH/Bank account payments
                        - Payment intent management
                        - Payment cancellation

                        ## Testing

                        ### Test Mode
                        All API keys are in **TEST MODE** (keys start with `sk_test_` and `pk_test_`). No real money is charged.

                        ### Test Card Numbers
                        Use these card numbers for testing different scenarios:

                        | Card Number          | Scenario                    |
                        |---------------------|----------------------------|
                        | 4242 4242 4242 4242 | Success                    |
                        | 4000 0000 0000 0002 | Card declined              |
                        | 4000 0025 0000 3155 | Requires authentication    |
                        | 4000 0000 0000 9995 | Insufficient funds         |

                        - **Expiry Date**: Any future date (e.g., 12/34)
                        - **CVC**: Any 3 digits (e.g., 123)
                        - **ZIP**: Any 5 digits (e.g., 12345)

                        ### Amount Format
                        All amounts are in **cents**:
                        - $50.00 = 5000
                        - $100.00 = 10000
                        - $1.00 = 100

                        ### Quick Test Example
                        ```json
                        POST /api/payments/create-card-intent
                        {
                          "amount": 5000,
                          "currency": "usd",
                          "description": "Test payment"
                        }
                        ```

                        This creates a payment intent. Use the `clientSecret` from the response to confirm the payment on your frontend.

                        ## Stripe Documentation
                        - [Stripe Testing](https://stripe.com/docs/testing)
                        - [Payment Intents](https://stripe.com/docs/payments/payment-intents)
                        - [Test Cards](https://stripe.com/docs/testing#cards)
                        """)
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
