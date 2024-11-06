package controller;

import model.Taxi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaxiPool {
    private static final long EXPIRED_TIME_IN_MILLISECOND = 1200;
    private static final int NUMBER_OF_TAXI = 4;
    private final List<Taxi> available = Collections.synchronizedList(new ArrayList<>());
    private final List<Taxi> inUse = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicBoolean waiting = new AtomicBoolean(false);

    public void release(Taxi taxi) {
        inUse.remove(taxi);
        available.add(taxi);
        System.out.println(taxi.getName() + " is free");
    }

    public synchronized Taxi getTaxi() {
        if (!available.isEmpty()) {
            Taxi taxi = available.remove(0);
            inUse.add(taxi);
            return taxi;
        }
        if (count.get() == NUMBER_OF_TAXI) {
            this.waitingUntilTaxiAvailable();
            return this.getTaxi();
        }
        Taxi taxi = this.createTaxi();
        inUse.add(taxi);
        return taxi;
    }

    private Taxi createTaxi() {
        waiting(200);
        Taxi taxi = new Taxi("Taxi " + count.incrementAndGet());
        System.out.println(taxi.getName() + " is Created");
        return taxi;
    }


    private void waitingUntilTaxiAvailable() {
        if (waiting.get()) {
            waiting.set(false);
            throw new TaxiNotFoundException("No taxi avaiable");
        }
        waiting.set(true);
        waiting(EXPIRED_TIME_IN_MILLISECOND);
    }

    private void waiting(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}