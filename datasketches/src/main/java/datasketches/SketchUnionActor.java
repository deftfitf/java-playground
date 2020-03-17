package datasketches;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.apache.datasketches.theta.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

public class SketchUnionActor extends AbstractBehavior<SketchUnionActor.Protocol> {

    public interface Protocol {}

    public static final class UnionSketch implements Protocol {
        private final Sketch sketch;
        public UnionSketch(Sketch sketch) {
            this.sketch = sketch;
        }
        public Sketch getSketch() {
            return sketch;
        }
    }

    private static final class SnapshotRequest implements Protocol {
        private SnapshotRequest() {}
        private final static SnapshotRequest instance = new SnapshotRequest();
        public static SnapshotRequest getInstance() {
            return instance;
        }
    }

    private final Union union;

    private SketchUnionActor(ActorContext<Protocol> context) {
        super(context);
        union = SetOperation.builder().buildUnion();
    }

    public static Behavior<Protocol> create(Duration snapshotInitialDuration, Duration snapshotRate) {
        return Behaviors.setup(ctx -> {
            ctx.getSystem().scheduler()
                    .scheduleAtFixedRate(
                            snapshotInitialDuration, snapshotRate,
                            () -> ctx.getSelf().tell(SnapshotRequest.getInstance()),
                            ctx.getSystem().executionContext());

            return new SketchUnionActor(ctx);
        });
    }

    @Override
    public Receive<Protocol> createReceive() {
        return newReceiveBuilder()
                .onMessage(UnionSketch.class, this::onUnionSketch)
                .onMessage(SnapshotRequest.class, this::onSnapshotRequest)
                .build();
    }

    private Behavior<Protocol> onUnionSketch(UnionSketch unionSketch) {
        union.update(unionSketch.getSketch());
        return Behaviors.same();
    }

    private Behavior<Protocol> onSnapshotRequest(SnapshotRequest snapshotRequest) {
        final var compacted = union.getResult();
        final var snapshot = compacted.toByteArray();

        try (final var out = new FileOutputStream("snapshot.bin")) {
            System.out.println("snapshot start.");
            out.write(snapshot);
            System.out.println("snapshot end.");
            System.out.println(compacted.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Behaviors.same();
    }

}
