package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.MetalState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;

public class SearchVaultFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class SearchVaultFlowInitiator extends FlowLogic<Void>{

        //public constructor
        public SearchVaultFlowInitiator() {}

        void searchForAllState() {
            QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
            List<StateAndRef<MetalState>> consumedMetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, consumedCriteria).getStates();

            if(consumedMetalStates.size() < 1) {
                System.out.println("No consumed metal states found.");
            } else {
                System.out.println("Total consumed metal states found = " + consumedMetalStates.size());
            }

            int c = consumedMetalStates.size();
            for(int i = 0; i < c; i++) {
                System.out.println("Name: " + consumedMetalStates.get(i).getState().getData().getMetalName());
                System.out.println("Owner: " + consumedMetalStates.get(i).getState().getData().getOwner());
                System.out.println("Weight: " + consumedMetalStates.get(i).getState().getData().getWeight());
                System.out.println("Issuer: " + consumedMetalStates.get(i).getState().getData().getIssuer());
            }

            QueryCriteria unconsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
            List<StateAndRef<MetalState>> unconsumedMetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, unconsumedCriteria).getStates();

            if(unconsumedMetalStates.size() < 1) {
                System.out.println("No unconsumed metal states found.");
            } else {
                System.out.println("Total unconsumed metal states found = " + unconsumedMetalStates.size());
            }

            int z = unconsumedMetalStates.size();
            for(int i = 0; i < z; i++) {
                System.out.println("Name: " + unconsumedMetalStates.get(i).getState().getData().getMetalName());
                System.out.println("Owner: " + unconsumedMetalStates.get(i).getState().getData().getOwner());
                System.out.println("Weight: " + unconsumedMetalStates.get(i).getState().getData().getWeight());
                System.out.println("Issuer: " + unconsumedMetalStates.get(i).getState().getData().getIssuer());
            }

        }

        @Override
        @Suspendable
        public Void call() throws FlowException {

            searchForAllState();
//            //Hello World message
//            String msg = "Hello-World";
//            this.sender = getOurIdentity();
//
//            // Step 1. Get a reference to the notary service on our network and our key pair.
//            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
//            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
//
//            //Compose the State that carries the Hello World message
//            final TemplateState output = new TemplateState(msg,sender,receiver);
//
//            // Step 3. Create a new TransactionBuilder object.
//            final TransactionBuilder builder = new TransactionBuilder(notary);
//
//            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
//            builder.addOutputState(output);
//            builder.addCommand(new TemplateContract.Commands.Send(), Arrays.asList(this.sender.getOwningKey(),this.receiver.getOwningKey()) );
//
//
//            // Step 5. Verify and sign it with our KeyPair.
//            builder.verify(getServiceHub());
//            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);
//
//
//            // Step 6. Collect the other party's signature using the SignTransactionFlow.
//            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
//            otherParties.remove(getOurIdentity());
//            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
//
//            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));
//
//            // Step 7. Assuming no exceptions, we can now finalise the transaction
//            return subFlow(new FinalityFlow(stx, sessions));
            return null;
        }
    }

//    @InitiatedBy(SearchVaultFlowInitiator.class)
//    public static class TemplateFlowResponder extends FlowLogic<Void>{
//        //private variable
//        private FlowSession counterpartySession;
//
//        //Constructor
//        public TemplateFlowResponder(FlowSession counterpartySession) {
//            this.counterpartySession = counterpartySession;
//        }
//
//        @Suspendable
//        @Override
//        public Void call() throws FlowException {
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
//            //Stored the transaction into data base.
//            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
//            return null;
//        }
//    }

}