package datasketches;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.apache.datasketches.theta.UpdateSketch;

import java.time.Duration;

public class SketchActor extends AbstractBehavior<SketchActor.Protocol> {

    public interface Protocol {}

    public static final class SketchEvent implements Protocol {
        private final long value;
        public SketchEvent(long value) {
            this.value = value;
        }
        public long getValue() {
            return value;
        }
    }

    private static final class PersistRequest implements Protocol {
        private PersistRequest() {}
        private final static PersistRequest instance = new PersistRequest();
        public static PersistRequest getInstance() {
            return instance;
        }
    }

    private final UpdateSketch updateSketch;
    private final ActorRef<SketchUnionActor.UnionSketch> unionActorRef;

    private SketchActor(
            ActorContext<Protocol> context,
            UpdateSketch updateSketch,
            ActorRef<SketchUnionActor.UnionSketch> unionActorRef) {
        super(context);
        this.updateSketch = updateSketch;
        this.unionActorRef = unionActorRef;
    }

    public static Behavior<Protocol> create(
            ActorRef<SketchUnionActor.UnionSketch> unionActorRef,
            Duration unionInitialDuration, Duration unionRate) {
        return Behaviors.setup(ctx -> {
            ctx.getSystem().scheduler()
                    .scheduleAtFixedRate(
                            unionInitialDuration, unionRate,
                            () -> ctx.getSelf().tell(PersistRequest.getInstance()),
                            ctx.getSystem().executionContext());

            return new SketchActor(ctx, UpdateSketch.builder().build(), unionActorRef);
        });
    }

    @Override
    public Receive<Protocol> createReceive() {
        return newReceiveBuilder()
                .onMessage(SketchEvent.class, this::onSketchEvent)
                .onMessage(PersistRequest.class, this::onPersist)
                .build();
    }

    private Behavior<Protocol> onSketchEvent(SketchEvent event) {
        updateSketch.update(event.getValue());
        return Behaviors.same();
    }

    private Behavior<Protocol> onPersist(PersistRequest persist) {
        unionActorRef.tell(new SketchUnionActor.UnionSketch(updateSketch.compact()));
        return Behaviors.same();
    }

}
