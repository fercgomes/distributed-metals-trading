package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;


public class ContractTests {
    private final MockServices ledgerServices = new MockServices();
//    private final MockServices ledgerServices = new MockServices(Arrays.asList("com.template"));
    TestIdentity mint = new TestIdentity(new CordaX500Name("Mint",  "TestLand",  "US"));
    TestIdentity traderA = new TestIdentity(new CordaX500Name("TraderA",  "TestLand",  "US"));
    TestIdentity traderB = new TestIdentity(new CordaX500Name("TraderB",  "TestLand",  "US"));

    private final MetalState metalState = new MetalState("Gold", 10, mint.getParty(), traderA.getParty());
    private final MetalState metalStateInput = new MetalState("Gold", 10, mint.getParty(), traderA.getParty());
    private final MetalState metalStateOutput = new MetalState("Gold", 10, mint.getParty(), traderB.getParty());

    @Test
    public void metalContractImplementsContract() {
        assert(new MetalContract() instanceof Contract);
    }

    // Issue Command Tests

    @Test
    public void metalContractRequiresZeroInputsOnIssueTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail
            tx.input(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an input, will verify
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresOneOutputOnIssueTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail
            tx.output(MetalContract.ID, metalState);
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an input, will verify
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTransactionOutputToBeMetalState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output, will faill
            tx.output(MetalContract.ID, new DummyState());
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output, will verify
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTransactionCommandToBeAnIssueCommand() {
        transaction(ledgerServices, tx -> {
            // Has wrong command, will fail
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command, will verify
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail
            tx.output(MetalContract.ID, metalState);
            tx.command(traderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command, will verify
            tx.output(MetalContract.ID, metalState);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    // Transfer Command Tests

    @Test
    public void metalContractRequiresOneInputAndOneOutputInTransferTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail
            tx.input(MetalContract.ID, metalStateInput);
            tx.output(MetalContract.ID, metalStateOutput);
            tx.command(traderA.getPublicKey(), new MetalContract.Commands.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.ID, metalStateOutput);
            tx.command(traderA.getPublicKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Does not have an input, will verify
            tx.input(MetalContract.ID, metalStateInput);
            tx.command(traderA.getPublicKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheTransactionCommandToBeATransferCommand() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has wrong command, will fail
                tx.input(MetalContract.ID, metalStateInput);
                tx.output(MetalContract.ID, metalStateOutput);
                tx.command(traderA.getPublicKey(), DummyCommandData.INSTANCE);
                return tx.fails();
            });

            l.transaction(tx -> {
                // Does not have an input, will verify
                tx.input(MetalContract.ID, metalStateInput);
                tx.output(MetalContract.ID, metalStateOutput);
                tx.command(traderA.getPublicKey(), new MetalContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheOwnerToBeARequiredSigner() {
        transaction(ledgerServices, tx -> {
            // Owner is required signer, will verify
            tx.input(MetalContract.ID, metalStateInput);
            tx.output(MetalContract.ID, metalStateOutput);
            tx.command(traderA.getPublicKey(), new MetalContract.Commands.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Owner is not the required signer, will fail
            tx.input(MetalContract.ID, metalStateInput);
            tx.output(MetalContract.ID, metalStateOutput);
            tx.command(mint.getPublicKey(), new MetalContract.Commands.Transfer());
            tx.fails();
            return null;
        });
    }
}