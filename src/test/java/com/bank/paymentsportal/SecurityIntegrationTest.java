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
import com.bank.paymentsportal.entity.UserRole;
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

    private static final String CUSTOMER_ONE_USERNAME = "nonhlekhumzie";
    private static final String CUSTOMER_ONE_PASSWORD = "Cust0mer!Pass1";
    private static final String CUSTOMER_TWO_USERNAME = "bob";
    private static final String CUSTOMER_TWO_PASSWORD = "Cust0mer!Pass2";
    private static final String EMPLOYEE_USERNAME = "nonokhumzie";
    private static final String EMPLOYEE_PASSWORD = "Employ3e!Pass1";

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
        userRepository.deleteAll();
        seedUser("Nokwandisa Khumalo", "nonhlekhumzie", "nokwandisa.khumalo@secure.com", "9001015800081", "12345678", "Cust0mer!Pass1", UserRole.CUSTOMER);
        seedUser("Bonginkosi Tlou", "bob", "bob@secure.com", "9102025800082", "123456789", "Cust0mer!Pass2", UserRole.CUSTOMER);
        seedUser("Nokwanda Khumalo", "nonokhumzie", "nokwanda.khumalo@secure.com", "8803035800083", "87654321", "Employ3e!Pass1", UserRole.EMPLOYEE);
    }

    @Test
    void invalidSwiftCodeIsRejected() throws Exception {
        String customerToken = login("/api/auth/login", CUSTOMER_ONE_USERNAME, CUSTOMER_ONE_PASSWORD);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "amount": 1200.50,
                                  "senderFullName": "Nokwandisa Khumalo",
                                  "currency": "USD",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Acme Imports",
                                  "beneficiaryBankName": "Acme Global Bank",
                                  "beneficiaryAccountNumber": "DE12345678",
                                  "swiftCode": "bad-swift",
                                  "country": "Germany",
                                  "paymentReference": "Invoice 2026-001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void publicRegistrationEndpointIsUnavailable() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Lebo Maseko",
                                  "username": "lebo.maseko",
                                  "email": "lebo.maseko@examplebank.test",
                                  "southAfricanIdNumber": "12345",
                                  "accountNumber": "12345678",
                                  "password": "MyVery$ecure123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWorksOnlyForPreconfiguredUsers() throws Exception {
        mockMvc.perform(post("/api/employee/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrAccountNumber": "unknown.employee",
                                  "password": "Employ3e!Pass1"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededPasswordsAreStoredHashed() {
        User saved = userRepository.findByUsername(EMPLOYEE_USERNAME).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(saved.getPasswordHash()).isNotEqualTo(EMPLOYEE_PASSWORD);
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(EMPLOYEE_PASSWORD, saved.getPasswordHash())).isTrue();
    }

    @Test
    void customersCannotAccessOtherCustomersTransactions() throws Exception {
        User owner = userRepository.findByUsername(CUSTOMER_ONE_USERNAME).orElseThrow();
        paymentTransactionRepository.save(PaymentTransaction.builder()
                .customer(owner)
                .senderFullName("Nokwandisa Khumalo")
                .amount(BigDecimal.valueOf(100))
                .currency(CurrencyCode.USD)
                .provider(PaymentProvider.SWIFT)
                .beneficiaryName("Global Supplies")
                .beneficiaryBankName("Global Reserve Bank")
                .beneficiaryAccountNumber("GB12345678")
                .swiftCode("ABCDEFGH")
                .country("United Kingdom")
                .paymentReference("Reference 100")
                .status(TransactionStatus.PENDING_VERIFICATION)
                .build());

        Long transactionId = paymentTransactionRepository.findAll().get(0).getId();
        String otherCustomerToken = login("/api/auth/login", CUSTOMER_TWO_USERNAME, CUSTOMER_TWO_PASSWORD);

        mockMvc.perform(get("/api/payments/{id}", transactionId)
                        .header("Authorization", bearer(otherCustomerToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeeEndpointsAreBlockedForCustomers() throws Exception {
        String customerToken = login("/api/auth/login", CUSTOMER_ONE_USERNAME, CUSTOMER_ONE_PASSWORD);

        mockMvc.perform(get("/api/employee/transactions")
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCanVerifyAndSubmitTransaction() throws Exception {
        String customerToken = login("/api/auth/login", CUSTOMER_ONE_USERNAME, CUSTOMER_ONE_PASSWORD);
        String employeeToken = login("/api/employee/auth/login", EMPLOYEE_USERNAME, EMPLOYEE_PASSWORD);

        MvcResult paymentResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "senderFullName": "Nokwandisa Khumalo",
                                  "amount": 8900.25,
                                  "currency": "EUR",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Nordic Trade BV",
                                  "beneficiaryBankName": "Nordic Trade Bank",
                                  "beneficiaryAccountNumber": "NL12BANK34567890",
                                  "swiftCode": "DEUTDEFF500",
                                  "country": "Netherlands",
                                  "paymentReference": "Trade settlement April 2026"
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
        String customerToken = login("/api/auth/login", CUSTOMER_ONE_USERNAME, CUSTOMER_ONE_PASSWORD);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(customerToken))
                        .content("""
                                {
                                  "senderFullName": "Nokwandisa Khumalo",
                                  "amount": 210.00,
                                  "currency": "GBP",
                                  "provider": "SWIFT",
                                  "beneficiaryName": "Atlas Partners",
                                  "beneficiaryBankName": "Atlas Reserve Bank",
                                  "beneficiaryAccountNumber": "GB12TEST12345678",
                                  "swiftCode": "BARCGB22",
                                  "country": "United Kingdom",
                                  "paymentReference": "Consulting invoice 245"
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

    private void seedUser(String fullName,
                          String username,
                          String email,
                          String southAfricanIdNumber,
                          String accountNumber,
                          String rawPassword,
                          UserRole role) {
        userRepository.save(User.builder()
                .fullName(fullName)
                .username(username)
                .email(email)
                .southAfricanIdNumber(southAfricanIdNumber)
                .accountNumber(accountNumber)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
    }
}
