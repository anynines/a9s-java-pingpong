package com.example;

import static java.util.concurrent.TimeUnit.SECONDS;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class PingActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(PingActor.class);
    }

    public static class Initialize {
    }

    public static class PingMessage {
        private final String text;

        public PingMessage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private ActorRef pongActor = getContext().actorOf(PongActor.props(), "pongActor");

    public void onReceive(Object message) throws Exception {
        if (message instanceof Initialize) {
            log.info("In PingActor - starting ping-pong");
            pongActor.tell(new PingMessage("ping"), getSelf());
        } else if (message instanceof PongActor.PongMessage) {
            PongActor.PongMessage pong = (PongActor.PongMessage) message;
            log.info("In PingActor - received message: {}", pong.getText());
            
            // schedule ping to be send in 1 second
            getContext()
            .system()
            .scheduler()
            .scheduleOnce(Duration.create(1, SECONDS), getSender(),
            		new PingMessage("ping"), getContext().dispatcher(), getSelf());
        } else {
            unhandled(message);
        }
    }
}