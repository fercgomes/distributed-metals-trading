package com.template;

import com.google.common.collect.ImmutableList;
import com.template.contracts.MetalContract;
import com.template.flows.IssueMetalFlow.*;
import com.template.flows.TransferMetalFlow.*;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.Future;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode mint;
    private StartedMockNode traderA;
    private StartedMockNode traderB;

    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));

        mint = network.createPartyNode(null);
        traderA = network.createPartyNode(null);
        traderB = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    // Issue Metal Flow
    @Test
    public void transactionHasNoInputsHasOneMetalStateOutputWithTheCorrectOwner() throws Exception {
        IssueMetalFlowInitiator flow = new IssueMetalFlowInitiator("Gold",10, traderA.getInfo().getLegalIdentities().get(0));
        Future<SignedTransaction> future = mint.startFlow(flow);

        network.runNetwork();

        SignedTransaction signedTransaction = future.get();

        assertEquals(0, signedTransaction.getTx().getInputs().size());
        assertEquals(1, signedTransaction.getTx().getOutputStates().size());

        MetalState output = signedTransaction.getTx().outputsOfType(MetalState.class).get(0);
        assertEquals(traderA.getInfo().getLegalIdentities().get(0), output.getOwner());
    }

    @Test
    public void transactionHasTheCorrectContractWithOneIssueCommandAsSigner() throws Exception {
        IssueMetalFlowInitiator flow = new IssueMetalFlowInitiator("Gold",10, traderA.getInfo().getLegalIdentities().get(0));
        Future<SignedTransaction> future = mint.startFlow(flow);
        network.runNetwork();

        SignedTransaction signedTransaction = future.get();

        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
        assertEquals("com.template.contracts.MetalContract", output.getContract());
        assertEquals(1, signedTransaction.getTx().getCommands().size());

        Command command = signedTransaction.getTx().getCommands().get(0);
        assert(command.getValue() instanceof MetalContract.Commands.Issue);

        // Has one signer, the mint
        assertEquals(1, command.getSigners().size());
        assertTrue(command.getSigners().contains(mint.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    // Transfer Metal Flow

    @Test
    public void transactionHasOneInputAndOneOutput() throws Exception {
        IssueMetalFlowInitiator flow = new IssueMetalFlowInitiator("Gold",10, traderA.getInfo().getLegalIdentities().get(0));
        TransferMetalFlowInitiator transferFlow = new TransferMetalFlowInitiator("Gold",10, traderB.getInfo().getLegalIdentities().get(0));

        Future<SignedTransaction> future = mint.startFlow(flow);
        network.runNetwork();

        Future<SignedTransaction> transferFuture = traderA.startFlow(transferFlow);
        network.runNetwork();

//        SignedTransaction signedTransaction2 = future.get();
        SignedTransaction signedTransaction = transferFuture.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        assertEquals(1, signedTransaction.getTx().getInputs().size());
    }

    @Test
    public void transactionHasTransferCommandWithOwnerAsSigner() throws Exception {
        IssueMetalFlowInitiator flow = new IssueMetalFlowInitiator("Gold",10, traderA.getInfo().getLegalIdentities().get(0));
        TransferMetalFlowInitiator transferFlow = new TransferMetalFlowInitiator("Gold",10, traderB.getInfo().getLegalIdentities().get(0));

        Future<SignedTransaction> future = mint.startFlow(flow);
        network.runNetwork();

        Future<SignedTransaction> transferFuture = traderA.startFlow(transferFlow);
        network.runNetwork();

//        SignedTransaction signedTransaction2 = future.get();
        SignedTransaction signedTransaction = transferFuture.get();


        Command command = signedTransaction.getTx().getCommands().get(0);

        assert(command.getValue() instanceof MetalContract.Commands.Transfer);
        assertTrue(command.getSigners().contains(traderA.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }
}
