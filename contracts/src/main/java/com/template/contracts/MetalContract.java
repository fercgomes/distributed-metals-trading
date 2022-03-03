package com.template.contracts;

import com.sun.istack.NotNull;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class MetalContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.MetalContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull  LedgerTransaction tx) throws IllegalArgumentException {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a signle transaction.*/
        //final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
//        final CommandData commandData = tx.getCommands().get(0).getValue();
//
//        if (commandData instanceof Commands.Send) {
//            //Retrieve the output state of the transaction
//            TemplateState output = tx.outputsOfType(TemplateState.class).get(0);
//
//            //Using Corda DSL function requireThat to replicate conditions-checks
//            requireThat(require -> {
//                require.using("No inputs should be consumed when sending the Hello-World message.", tx.getInputStates().size() == 0);
//                require.using("The message must be Hello-World", output.getMsg().equals("Hello-World"));
//                return null;
//            });
//        }



        if(tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Transaction must have one command.");
        }

        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        // Issue command contract rules
        if(commandType instanceof Commands.Issue) {
            // Issue transaction logic

            // Shape rules
            if(tx.getInputs().size() != 0) throw new IllegalArgumentException("Issue cannot have inputs");
            if(tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Issue can only have one output");

            // Content rules
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof MetalState)) throw new IllegalArgumentException("Output must be a metal state");

            MetalState metalState = (MetalState) outputState;

            if(!metalState.getMetalName().equals("Gold") && !metalState.getMetalName().equals("Silver"))
                throw new IllegalArgumentException("Metal is not Gold or Silver");

            // Signer rules
            Party issuer = metalState.getIssuer();
            PublicKey issuersKey = issuer.getOwningKey();

            if(!(requiredSigners.contains(issuersKey)))
                throw new IllegalArgumentException("Issuer has to sign the issuance");

        }

        // Transfer command contract rules
        else if(commandType instanceof Commands.Transfer) {
            // Issue transaction logic

            // Shape rules
            if(tx.getInputs().size() != 1) throw new IllegalArgumentException("Transfer cannot have inputs");
            if(tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Transfer can only have one output");

            // Content rules
            ContractState inputState = tx.getInput(0);
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof MetalState)) throw new IllegalArgumentException("Output must be a metal state");

            MetalState metalState = (MetalState) inputState;

            if(!metalState.getMetalName().equals("Gold") && !metalState.getMetalName().equals("Silver"))
                throw new IllegalArgumentException("Metal is not Gold or Silver");

            // Signer rules
            Party owner = metalState.getOwner();
            PublicKey ownersKey = owner.getOwningKey();

            if(!(requiredSigners.contains(ownersKey)))
                throw new IllegalArgumentException("Owner has to sign the issuance");

        } else {
            throw new IllegalArgumentException("Unrecognized command.");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Issue implements Commands {}
        class Transfer implements Commands {}
    }
}