package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.IQueryCriteriaParser;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import javax.management.Query;
import javax.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TransferMetalFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class TransferMetalFlowInitiator extends FlowLogic<SignedTransaction>{
        private final String metalName;
        private final int weight;
        private final Party newOwner;

        private int inputIdx = 0;

        private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary");
        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing the transaction with our public key");
        private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty");
        private final ProgressTracker.Step FINALIZING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");
        private final ProgressTracker progressTracker = new ProgressTracker(RETRIEVING_NOTARY, GENERATING_TRANSACTION, SIGNING_TRANSACTION, COUNTERPARTY_SESSION, FINALIZING_TRANSACTION);

        //public constructor
        public TransferMetalFlowInitiator(String metalName, int weight, Party newOwner) {
            this.metalName = metalName;
            this.weight = weight;
            this.newOwner = newOwner;
        }

        StateAndRef<MetalState> checkForMetalStates() throws FlowException {
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            List<StateAndRef<MetalState>> metalStates = getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();
            boolean inputFound = false;

            for(int x = 0; x < metalStates.size(); x++){
                if(metalStates.get(x).getState().getData().getMetalName().equals(metalName)
                        && metalStates.get(x).getState().getData().getWeight() == weight
                ) {
                    inputIdx = x;
                    inputFound = true;
                }
            }

            if(inputFound) {
                System.out.println("Input found");
            } else {
                System.out.println("Input not found");
                throw new FlowException();
            }

            return metalStates.get(inputIdx);
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            // Retrieve Notary identity
            progressTracker.setCurrentStep(RETRIEVING_NOTARY);
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            StateAndRef<MetalState> inputState = null;
            inputState = checkForMetalStates();
            Party issuer = inputState.getState().getData().getIssuer();

            // Create transaction components
            MetalState outputState = new MetalState(metalName, weight, issuer, newOwner);
            Command cmd = new Command(new MetalContract.Commands.Transfer(), getOurIdentity().getOwningKey());

            // Create transaction builder
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(outputState, MetalContract.ID)
                    .addCommand(cmd);

            txBuilder.addInputState(inputState);


            // Sign the transaction
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            txBuilder.verify(getServiceHub());
            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Create session with counterparty
            progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
            FlowSession otherPartySession = initiateFlow(newOwner);

            List<Party> otherParties = outputState.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

            SignedTransaction stx = subFlow(new CollectSignaturesFlow(signedTx, sessions));

            // Finalize and send to counterparty
            progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
            return subFlow(new FinalityFlow(stx, sessions));

        }

        @Override
        public ProgressTracker getProgressTracker() { return progressTracker; }
    }

    @InitiatedBy(TransferMetalFlowInitiator.class)
    public static class TransferMetalFlowResponder extends FlowLogic<SignedTransaction>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public TransferMetalFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
//            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
//                @Suspendable
//                @Override
//                protected void checkTransaction(SignedTransaction stx) throws FlowException {
//                    /*
//                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
//                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
//                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
//                     * or we’re not happy with the transaction’s structure? checkTransaction
//                     * allows us to define these additional checks. If any of these conditions are not met,
//                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
//                     * ----------
//                     * For this hello-world cordapp, we will not implement any aditional checks.
//                     * */
//                }
//            });

            System.out.println("Received precious metal");

            //Stored the transaction into data base.
            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }
    }

}
