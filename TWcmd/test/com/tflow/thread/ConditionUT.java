package com.tflow.thread;

import com.tflow.UTBase;
import com.tflow.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUT extends UTBase {

    ScheduledThreadPoolExecutor scheduler;
    ScheduledFuture<?> scheduledFuture;
    Lock lock;
    Condition condition;
    int id;

    void printThreads() {
        printThreads(null);
    }

    void printThreads(String string) {
        List<String> nameList = new ArrayList<>();
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        for (Thread thread : threadMap.keySet()) {
            nameList.add(thread.getName());
        }
        nameList.sort(String::compareTo);
        println("Threads:" + (string == null ? " " : string + ": ") + Arrays.toString(nameList.toArray()));
    }

    @BeforeEach
    void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(10);
        scheduledFuture = null;
        lock = new ReentrantLock();
        condition = lock.newCondition();
        id = 0;
    }

    Thread awaitThread;

    public void await() {
        awaitThread = new Thread("AWAIT") {
            @Override
            public String toString() {
                println("AWAIT:TICK");
                return "";
            }

            @Override
            public void run() {
                String msg = "Command #" + (++id);
                println("AWAIT:BEFORE: " + msg);

                try {
                    lock.lock();
                    condition.await();
                    println("AWAIT:AFTER: " + msg + " (blocked by condition)");
                } catch (Exception ex) {
                    println(ex.getClass().getSimpleName() + " on AWAIT: " + ex.getMessage());
                } finally {
                    lock.unlock();
                    printThreads("AWAIT:AFTER");
                }
            }
        };
        awaitThread.start();
    }

    public void signal(long delayMs) {
        String msg = "Command #" + (++id);
        println("SIGNAL:BEFORE-SCHEDULE: " + msg);
        scheduledFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    condition.signal();
                } catch (Exception ex) {
                    println(ex.getClass().getSimpleName() + " on SIGNAL: " + ex.getMessage());
                } finally {
                    lock.unlock();
                }
                println("SIGNAL:AFTER-RUN: " + msg);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
        println("SIGNAL:AFTER-SCHEDULE: " + msg);
    }

    public void tick() {
        lock.lock();
        condition.signal();
        lock.unlock();
        println("tick from another thread completed");
    }

    @Test
    public void testBlockMainThread() {
        printThread = true;

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                println("call another function on main thread");
            }
        }, 2, TimeUnit.SECONDS);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                println("scheduledTask: run after 3 seconds");
                tick();
            }
        }, 3, TimeUnit.SECONDS);

        println("---- Await ----");

        lock.lock();
        try {
            condition.await();
            lock.unlock();
        } catch (InterruptedException e) {
            println("ERROR: "+ e.getMessage());
            e.printStackTrace();
        }

        println("---- Signal ----");

    }

    @Test
    public void testAwaitSignal() {
        println("-- begin:blocking by condition --");
        indent();

        printThreads();
        await();
        awaitThread.toString();
        printThreads("after await x2");
        signal(3000);
        awaitThread.toString();
        printThreads("after signal 1");

        indent(-1);
        println("-- end:blocking by condition --");
        println("");
        println("-- begin:Blocking by scheduledFuture.get --");
        indent();

        if (scheduledFuture != null) {
            println("waiting for scheduledFuture.get");
            try {
                Object something = scheduledFuture.get();
                println("scheduledFuture.get: return " + something + " (blocked by scheduledFuture.get)");
                awaitThread.toString();
            } catch (Exception ex) {
                println("scheduledFuture.get failed, " + ex.getMessage());
            }
        }
        scheduler.shutdownNow();
        printThreads("after scheduler.shutdownNow");

        indent(-1);
        println("-- end:Blocking by scheduledFuture.get --");
    }
}
