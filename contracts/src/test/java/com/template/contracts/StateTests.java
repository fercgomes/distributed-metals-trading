package com.template.contracts;

import com.template.states.MetalState;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

public class StateTests {
    private Party mint = new TestIdentity(new CordaX500Name("Mint","New York" ,"US")).getParty();
    private Party trader = new TestIdentity(new CordaX500Name("Trader","London" ,"GB")).getParty();

    @Test
    public void metalStateImplementsContractState() {
        assert(new MetalState("Gold", 10, mint, trader) instanceof ContractState);
    }

    @Test
    public void metalStateHasTwoParticipantsTheIssuerAndOwner() {
        MetalState metalState = new MetalState("Gold", 10, mint, trader);
        assert(metalState.getParticipants().size() == 2);
        assert(metalState.getParticipants().contains(mint) && metalState.getParticipants().contains(trader));
    }

    @Test
    public void metalStateHasGettersForAllFields() {
        MetalState metalState = new MetalState("Gold", 10, mint, trader);
        assert(metalState.getMetalName() == "Gold");
        assert(metalState.getWeight() == 10);
        assert(metalState.getIssuer() == mint);
        assert(metalState.getOwner() == trader);

    }
}