package com.hip.darp.darpconnector.actors.impl;

import com.hip.darp.darpconnector.actors.SimpleActor;
import io.dapr.actors.ActorId;
import io.dapr.actors.runtime.AbstractActor;
import io.dapr.actors.runtime.ActorRuntimeContext;
import io.dapr.actors.runtime.Remindable;
import io.dapr.utils.TypeRef;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.time.Duration.ofSeconds;

@Slf4j
public class SimpleEchoActor extends AbstractActor implements SimpleActor, Remindable<Integer> {

    /**
     * Time zone ID for UTC/GMT.
     */
    public static final String GMT = "GMT";
    /**
     * Thread-safe formatter to output date and time.
     */
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.of(GMT));

    protected SimpleEchoActor(ActorRuntimeContext runtimeContext, ActorId id) {
        super(runtimeContext, id);
    }

    /**
     * Register a timer.
     */
    @Override
    public void registerTimer(String state) {
        // For example, the state will be formatted as `ping! {INDEX}` where INDEX is the index of the actor related to ID.
        super.registerActorTimer(
                null,
                "clock",
                state,
                ofSeconds(2),
                ofSeconds(1)).block();
    }

    /**
     * Registers a reminder.
     */
    @Override
    public void registerReminder(int index) {
        // For this example, the state reminded by the reminder is deterministic to be the index(not ID) of the actor.
        super.registerReminder(
                "myremind",
                index,
                ofSeconds(5),
                ofSeconds(2)).block();
    }

    /**
     * Prints a message and appends the timestamp.
     * @param something Something to be said.
     * @return What was said appended with timestamp.
     */
    @Override
    public String say(String something) {
        String utcNowAsString = DATE_TIME_FORMAT.format(Instant.now());

        // Handles the request by printing message.
        log.info("Server say method for actor "
                + super.getId() + ": "
                + (something == null ? "" : something + " @ " + utcNowAsString));

        super.getActorStateManager().set("lastmessage", something).block();

        // Now respond with current timestamp.
        return utcNowAsString;
    }

    /**
     * Increments a persistent counter, saves and returns its updated value.
     * Example of method implemented with Reactor's Mono class.
     * This method could be rewritten with blocking calls in Mono, using block() method:
     *
     * <p>public int incrementAndGet(int delta) {
     *   int counter = 0;
     *   if (super.getActorStateManager().contains("counter").block()) {
     *     counter = super.getActorStateManager().get("counter", int.class).block();
     *   }
     *   counter = counter + 1;
     *   super.getActorStateManager().set("counter", counter).block();
     *   return counter;
     * }</p>
     * @param delta Amount to be added to counter.
     * @return Mono response for the incremented value.
     */
    @Override
    public Mono<Integer> incrementAndGet(int delta) {
        return super.getActorStateManager().contains("counter")
                .flatMap(exists -> exists ? super.getActorStateManager().get("counter", int.class) : Mono.just(0))
                .map(c -> c + delta)
                .flatMap(c -> super.getActorStateManager().set("counter", c).thenReturn(c));
    }

    /**
     * Method invoked by timer.
     * @param message Message to be printed.
     */
    @Override
    public void clock(String message) {
        String utcNowAsString = DATE_TIME_FORMAT.format(Instant.now());

        // Handles the request by printing message.
        log.info("Server timer triggered with state "
                + (message == null ? "" : message) + " for actor "
                + super.getId() + "@ " + utcNowAsString);
    }

    /**
     * Method used to determine reminder's state type.
     * @return Class for reminder's state.
     */
    @Override
    public TypeRef<Integer> getStateType() {
        return TypeRef.INT;
    }

    /**
     * Method used be invoked for a reminder.
     * @param reminderName The name of reminder provided during registration.
     * @param state        The user state provided during registration.
     * @param dueTime      The invocation due time provided during registration.
     * @param period       The invocation period provided during registration.
     * @return Mono result.
     */
    @Override
    public Mono<Void> receiveReminder(String reminderName, Integer state, Duration dueTime, Duration period) {
        return Mono.fromRunnable(() -> {
            String utcNowAsString = DATE_TIME_FORMAT.format(Instant.now());

            String message = String.format("Reminder %s with state {%d} triggered for actor %s @ %s",
                    reminderName, state, this.getId(), utcNowAsString);

            // Handles the request by printing message.
            log.info(message);
        });
    }
}
