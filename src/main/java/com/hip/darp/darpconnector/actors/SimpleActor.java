package com.hip.darp.darpconnector.actors;

import io.dapr.actors.ActorMethod;
import io.dapr.actors.ActorType;
import reactor.core.publisher.Mono;

@ActorType(name = "SimpleActor")
public interface SimpleActor {
    void registerTimer(String state);
    void registerReminder(int index);
    @ActorMethod(name = "echo_message")
    String say(String something);
    void clock(String message);
    @ActorMethod(returns = Integer.class)
    Mono<Integer> incrementAndGet(int delta);
}
