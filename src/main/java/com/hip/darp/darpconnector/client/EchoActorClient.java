package com.hip.darp.darpconnector.client;

import com.hip.darp.darpconnector.actors.SimpleActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoActorClient {

    /**
     * Makes multiple method calls into actor until interrupted.
     * @param actorId Actor's identifier.
     * @param actor Actor to be invoked.
     */
    private void callActorForever(int index, String actorId, SimpleActor actor) {
        // First, register reminder.
        actor.registerReminder(index);
        // Second register timer.
        actor.registerTimer("ping! {" + index + "} ");

        // Now, we run until thread is interrupted.
        while (!Thread.currentThread().isInterrupted()) {
            // Invoke actor method to increment counter by 1, then build message.
            int messageNumber = actor.incrementAndGet(1).block();
            String message = String.format("Message #%d received from actor at index %d with ID %s", messageNumber,
                    index, actorId);

            // Invoke the 'say' method in actor.
            String result = actor.say(message);
            log.info("Reply %s received from actor at index %d with ID %s ", result,
                    index, actorId);

            try {
                // Waits for up to 1 second.
                Thread.sleep((long) (1000 * Math.random()));
            } catch (InterruptedException e) {
                // We have been interrupted, so we set the interrupted flag to exit gracefully.
                Thread.currentThread().interrupt();
            }
        }
    }
}
