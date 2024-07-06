import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author nimatullah
 */
class TrafficLight {
    enum State {RED, GREEN, YELLOW}

    private final int id;
    private State state;
    private int queue;
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private final Timer timer = new Timer();
    private static final Map<Integer, TrafficLight> trafficLights = new ConcurrentHashMap<>();

    public TrafficLight(int id) {
        this.id = id;
        this.state = State.RED;
        this.queue = 0;
        trafficLights.put(id, this);
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public void sendEvent(Event event) {
        eventQueue.add(event);
    }

    public void processEvents() {
        while (true) {
            try {
                Event event = eventQueue.take();
                handleEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleEvent(Event event) {
        switch (event.type) {
            case "UPDATE_QUEUE":
                this.queue = event.data;
                break;
            case "CHANGE_STATE":
                this.state = State.valueOf(String.valueOf(event.data));
                break;
            case "TIMER_EVENT":
                sendEventToLight(event.targetId, new Event("TIMER_EVENT", 0, this.id));
                break;
        }
    }

    public void sendEventToLight(int targetId, Event event) {
        trafficLights.get(targetId).sendEvent(event);
    }

    public void setTimer(long delay, int targetId, String eventType) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendEventToLight(targetId, new Event(eventType, 0, id));
            }
        }, delay);
    }

    public void optimizeTraffic() {
        if (queue > 5) {
            setState(State.GREEN);
            setTimer(10000, id, "TIMER_EVENT");
        } else {
            setState(State.RED);
        }
    }

    public static void main(String[] args) {
        List<TrafficLight> lights = Arrays.asList(
                new TrafficLight(1),
                new TrafficLight(2),
                new TrafficLight(3),
                new TrafficLight(4)
        );

        for (TrafficLight light : lights) {
            new Thread(light::processEvents).start();
        }

        // Simulate traffic updates and optimization
        Random random = new Random();
        for (TrafficLight light : lights) {
            int queueSize = random.nextInt(10);
            light.sendEvent(new Event("UPDATE_QUEUE", queueSize, light.id));
            light.optimizeTraffic();
        }
    }
}

class Event {
    public int targetId;
    String type;
    int data;
    int senderId;

    public Event(String type, int data, int senderId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
    }
}
