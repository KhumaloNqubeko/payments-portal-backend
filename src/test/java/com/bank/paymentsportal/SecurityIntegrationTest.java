package com.bank.paymentsportal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.paymentsportal.entity.CurrencyCode;
import com.bank.paymentsportal.entity.PaymentProvider;
import com.bank.paymentsportal.entity.PaymentTransaction;
import com.bank.paymentsportal.entity.TransactionStatus;
import com.bank.paymentsportal.entity.User;
import com.bank.paymentsportal.repository.PaymentTransactionRepository;
import com.bank.paymentsportal.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        paymentTransactionRepository.deleteAll();
    }

    @Test
    void invalidSwiftCodeIsRejected() throws Exception {
        String customerToken = login("/api/auth/login", "ama.dlamini", "Cust0mer!Pass1");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "amount": 1200.50,
                                  "currency": "USD",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Acme Imports",
                                  "beneficiaryAccountNumber": "DE12345678",
                                  "swiftCode": "bad-swift"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void invalidSouthAfricanIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Lebo Maseko",
                                  "username": "lebo.maseko",
                                  "southAfricanIdNumber": "12345",
                                  "accountNumber": "12345678",
                                  "password": "MyVery$ecure123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]", containsString("South African ID number")));
    }

    @Test
    void passwordIsStoredHashed() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Lebo Maseko",
                                  "username": "lebo.maseko",
                                  "southAfricanIdNumber": "9901015800084",
                                  "accountNumber": "23456789",
                                  "password": "MyVery$ecure123"
                                }
                                """))
                .andExpect(status().isOk());

        User saved = userRepository.findByUsername("lebo.maseko").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(saved.getPasswordHash()).isNotEqualTo("MyVery$ecure123");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("MyVery$ecure123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void customersCannotAccessOtherCustomersTransactions() throws Exception {
        User owner = userRepository.findByUsername("ama.dlamini").orElseThrow();
        paymentTransactionRepository.save(PaymentTransaction.builder()
                .customer(owner)
                .amount(BigDecimal.valueOf(100))
                .currency(CurrencyCode.USD)
                .provider(PaymentProvider.SWIFT)
                .beneficiaryName("Global Supplies")
                .beneficiaryAccountNumber("GB12345678")
                .swiftCode("ABCDEFGH")
                .status(TransactionStatus.PENDING_VERIFICATION)
                .build());

        Long transactionId = paymentTransactionRepository.findAll().get(0).getId();
        String otherCustomerToken = login("/api/auth/login", "thabo.naidoo", "Cust0mer!Pass2");

        mockMvc.perform(get("/api/payments/{id}", transactionId)
                        .header("Authorization", bearer(otherCustomerToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeeEndpointsAreBlockedForCustomers() throws Exception {
        String customerToken = login("/api/auth/login", "ama.dlamini", "Cust0mer!Pass1");

        mockMvc.perform(get("/api/employee/transactions")
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCanVerifyAndSubmitTransaction() throws Exception {
        String customerToken = login("/api/auth/login", "ama.dlamini", "Cust0mer!Pass1");
        String employeeToken = login("/api/employee/auth/login", "employee.naledi", "Employ3e!Pass1");

        MvcResult paymentResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "amount": 8900.25,
                                  "currency": "EUR",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Nordic Trade BV",
                                  "beneficiaryAccountNumber": "NL12BANK34567890",
                                  "swiftCode": "DEUTDEFF500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andReturn();

        Long transactionId = objectMapper.readTree(paymentResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/employee/transactions/{id}/verify", transactionId)
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        mockMvc.perform(patch("/api/employee/transactions/{id}/submit-swift", transactionId)
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED_TO_SWIFT"));
    }

    @Test
    void transactionResponsesMaskSensitiveAccountNumbers() throws Exception {
        String customerToken = login("/api/auth/login", "ama.dlamini", "Cust0mer!Pass1");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "amount": 210.00,
                                  "currency": "GBP",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Atlas Partners",
                                  "beneficiaryAccountNumber": "GB12TEST12345678",
                                  "swiftCode": "BARCGB22"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedBeneficiaryAccountNumber", containsString("5678")))
                .andExpect(content().string(not(containsString("GB12TEST12345678"))));
    }

    private String login(String endpoint, String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrAccountNumber": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
