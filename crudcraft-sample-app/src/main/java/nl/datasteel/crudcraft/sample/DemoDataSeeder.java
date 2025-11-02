/**
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.sample;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.account.Account;
import nl.datasteel.crudcraft.sample.account.AccountLimits;
import nl.datasteel.crudcraft.sample.audit.AuditLog;
import nl.datasteel.crudcraft.sample.branch.Branch;
import nl.datasteel.crudcraft.sample.card.Card;
import nl.datasteel.crudcraft.sample.card.CardPin;
import nl.datasteel.crudcraft.sample.customer.AccountHolder;
import nl.datasteel.crudcraft.sample.customer.Customer;
import nl.datasteel.crudcraft.sample.customer.KycProfile;
import nl.datasteel.crudcraft.sample.enums.AccountStatus;
import nl.datasteel.crudcraft.sample.enums.CardNetwork;
import nl.datasteel.crudcraft.sample.enums.CardStatus;
import nl.datasteel.crudcraft.sample.enums.CustomerStatus;
import nl.datasteel.crudcraft.sample.enums.HolderType;
import nl.datasteel.crudcraft.sample.enums.KycStatus;
import nl.datasteel.crudcraft.sample.enums.RoleType;
import nl.datasteel.crudcraft.sample.enums.TransactionDirection;
import nl.datasteel.crudcraft.sample.enums.TransactionStatus;
import nl.datasteel.crudcraft.sample.tenant.Tenant;
import nl.datasteel.crudcraft.sample.transaction.Transaction;
import nl.datasteel.crudcraft.sample.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory database with sample data for demonstration purposes.
 * Populates users of different roles so the security examples can be tested.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    /** Direct JPA access used to insert demo rows without going through CrudCraft. */
    private final EntityManager entityManager;

    public DemoDataSeeder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Long existing = entityManager.createQuery("select count(t) from Tenant t", Long.class)
                .getSingleResult();
        if (existing != null && existing > 0) {
            return;
        }

        Tenant redBank = createTenant("RedBank");
        Tenant blueBank = createTenant("BlueBank");

        Branch redHq = createBranch(redBank, "RB001", "RedBank HQ");
        Branch redCity = createBranch(redBank, "RB002", "RedBank City");
        Branch blueHq = createBranch(blueBank, "BB001", "BlueBank HQ");
        Branch blueCity = createBranch(blueBank, "BB002", "BlueBank City");

        createUser(redBank, redHq, "alice", RoleType.ADMIN);
        createUser(redBank, redCity, "bob", RoleType.TELLER);
        createUser(blueBank, blueHq, "carol", RoleType.ADMIN);
        createUser(blueBank, blueCity, "dave", RoleType.TELLER);

        createUser(redBank, redHq, "erin", RoleType.AUDITOR);
        createUser(redBank, redCity, "frank", RoleType.CUSTOMER);
        createUser(blueBank, blueHq, "grace", RoleType.AUDITOR);
        createUser(blueBank, blueCity, "heidi", RoleType.CUSTOMER);

        Customer redJohn = createCustomer(redBank, "John Doe", "john@redbank.test");
        Customer redJane = createCustomer(redBank, "Jane Roe", "jane@redbank.test");
        Customer blueJim = createCustomer(blueBank, "Jim Beam", "jim@bluebank.test");
        Customer blueJill = createCustomer(blueBank, "Jill Smith", "jill@bluebank.test");

        createKyc(redJohn, KycStatus.APPROVED);
        createKyc(redJane, KycStatus.PENDING);
        createKyc(blueJim, KycStatus.APPROVED);
        createKyc(blueJill, KycStatus.PENDING);

        Account redAcc1 = createAccount(redBank, redHq, redJohn, "NL01RB0000000001", new BigDecimal("1000.00"));
        Account redAcc2 = createAccount(redBank, redCity, redJane, "NL01RB0000000002", new BigDecimal("2000.00"));
        Account blueAcc1 = createAccount(blueBank, blueHq, blueJim, "NL01BB0000000001", new BigDecimal("1500.00"));
        Account blueAcc2 = createAccount(blueBank, blueCity, blueJill, "NL01BB0000000002", new BigDecimal("2500.00"));

        createCard(redAcc1, CardNetwork.VISA, "tok1", "1234", 12, 2030);
        createCard(redAcc2, CardNetwork.MASTERCARD, "tok2", "5678", 11, 2031);
        createCard(blueAcc1, CardNetwork.VISA, "tok3", "4321", 10, 2030);
        createCard(blueAcc2, CardNetwork.MASTERCARD, "tok4", "8765", 9, 2031);

        createTransaction(redBank, redAcc1, TransactionDirection.DEBIT, new BigDecimal("50.00"), "Ref-1");
        createTransaction(redBank, redAcc1, TransactionDirection.CREDIT, new BigDecimal("120.00"), "Ref-2");
        createTransaction(redBank, redAcc2, TransactionDirection.DEBIT, new BigDecimal("300.00"), "Ref-3");
        createTransaction(redBank, redAcc2, TransactionDirection.CREDIT, new BigDecimal("500.00"), "Ref-4");
        createTransaction(blueBank, blueAcc1, TransactionDirection.DEBIT, new BigDecimal("70.00"), "Ref-5");
        createTransaction(blueBank, blueAcc1, TransactionDirection.CREDIT, new BigDecimal("90.00"), "Ref-6");
        createTransaction(blueBank, blueAcc2, TransactionDirection.DEBIT, new BigDecimal("40.00"), "Ref-7");
        createTransaction(blueBank, blueAcc2, TransactionDirection.CREDIT, new BigDecimal("110.00"), "Ref-8");

        createAuditLog(redBank.getName());
        createAuditLog(blueBank.getName());
    }

    private Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        entityManager.persist(tenant);
        return tenant;
    }

    private Branch createBranch(Tenant tenant, String code, String name) {
        Branch branch = new Branch();
        branch.setTenant(tenant);
        branch.setCode(code);
        branch.setName(name);
        entityManager.persist(branch);
        return branch;
    }

    private void createUser(Tenant tenant, Branch branch, String username, RoleType role) {
        User user = new User();
        user.setTenant(tenant);
        user.setBranch(branch);
        user.setUsername(username);
        user.setPasswordHash("password");
        user.setRoles(new HashSet<>(Set.of(role)));
        entityManager.persist(user);
    }

    private Customer createCustomer(Tenant tenant, String name, String email) {
        Customer customer = new Customer();
        customer.setTenant(tenant);
        customer.setName(name);
        customer.setEmail(email);
        customer.setStatus(CustomerStatus.ACTIVE);
        entityManager.persist(customer);
        return customer;
    }

    private void createKyc(Customer customer, KycStatus status) {
        KycProfile profile = new KycProfile();
        profile.setCustomer(customer);
        profile.setStatus(status);
        profile.setCompletedAt(OffsetDateTime.now());
        entityManager.persist(profile);
    }

    private Account createAccount(Tenant tenant, Branch branch, Customer holder, String iban,
            BigDecimal balance) {
        Account account = new Account();
        account.setTenant(tenant);
        account.setBranch(branch);
        account.setIban(iban);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCurrentBalance(balance);
        account.setCurrency("EUR");
        entityManager.persist(account);

        AccountLimits limits = new AccountLimits();
        limits.setAccount(account);
        limits.setDailyLimit(new BigDecimal("1000.00"));
        limits.setMonthlyLimit(new BigDecimal("10000.00"));
        limits.setOverdraftEnabled(true);
        entityManager.persist(limits);
        account.setLimits(limits);

        AccountHolder holderLink = new AccountHolder();
        holderLink.setAccount(account);
        holderLink.setCustomer(holder);
        holderLink.setHolderType(HolderType.OWNER);
        entityManager.persist(holderLink);

        return account;
    }

    private void createCard(Account account, CardNetwork network, String panToken, String last4,
            int expiryMonth, int expiryYear) {
        Card card = new Card();
        card.setAccount(account);
        card.setStatus(CardStatus.ACTIVE);
        card.setNetwork(network);
        card.setPanToken(panToken);
        card.setPanLast4(last4);
        card.setExpiryMonth(expiryMonth);
        card.setExpiryYear(expiryYear);
        entityManager.persist(card);

        CardPin pin = new CardPin();
        pin.setCard(card);
        pin.setPinHash("hash");
        pin.setPinSalt("salt");
        pin.setPinVersion(1);
        pin.setTryCount(0);
        entityManager.persist(pin);
    }

    private void createTransaction(Tenant tenant, Account account, TransactionDirection direction,
            BigDecimal amount, String reference) {
        Transaction tx = new Transaction();
        tx.setTenant(tenant);
        tx.setAccount(account);
        tx.setTimestamp(OffsetDateTime.now());
        tx.setDirection(direction);
        tx.setAmount(amount);
        tx.setCurrency("EUR");
        tx.setClientReference(reference);
        tx.setCounterpartyIban("NL00EXAMPLE00000000");
        tx.setCounterpartyName("Example Co");
        tx.setStatus(TransactionStatus.BOOKED);
        entityManager.persist(tx);
    }

    private void createAuditLog(String tenantName) {
        AuditLog log = new AuditLog();
        log.setTimestamp(OffsetDateTime.now());
        log.setAction("SEED");
        log.setSubjectType("Tenant");
        log.setSubjectId(tenantName);
        log.setCorrelationId(UUID.randomUUID().toString());
        entityManager.persist(log);
    }
}

