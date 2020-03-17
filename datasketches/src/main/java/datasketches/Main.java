package datasketches;

import java.time.Duration;
import java.util.stream.IntStream;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public final class Main {

    public static void main(String[] args) {
        final var guardian = Behaviors.setup(ctx -> {
            final var union = ctx.spawn(SketchUnionActor.create(
                    Duration.ofSeconds(180), Duration.ofSeconds(180)),
                    "union").<SketchUnionActor.UnionSketch>narrow();
            final var sketch1 = ctx.spawn(
                    SketchActor.create(union, Duration.ofSeconds(30), Duration.ofSeconds(30)), "sketch1");
            final var sketch2 = ctx.spawn(
                    SketchActor.create(union, Duration.ofSeconds(30), Duration.ofSeconds(30)), "sketch2");

            IntStream.range(0, 100000).forEach(i -> sketch1.tell(new SketchActor.SketchEvent(i)));
            IntStream.range(50000, 150000).forEach(i -> sketch2.tell(new SketchActor.SketchEvent(i)));

            return new AbstractBehavior<Object>(ctx) {
                @Override
                public Receive<Object> createReceive() {
                    return newReceiveBuilder().build();
                }
            };
        });

        ActorSystem.create(guardian, "system");
    }

}
