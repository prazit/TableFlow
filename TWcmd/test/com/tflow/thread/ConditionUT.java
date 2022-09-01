package com.tflow.thread;

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

public class ConditionUT {

    ScheduledThreadPoolExecutor scheduler;
    ScheduledFuture<?> scheduledFuture;
    Lock lock;
    Condition condition;
    int id;

    String indent = "";
    String indentChars = "\t";

    void println(String string) {
        System.out.println(
                DateTimeUtil.getStr(DateTimeUtil.now(), "[dd/MM/yyyy HH:mm:ss.SSS] ") +
                        indent +
                        " Thread-" + Thread.currentThread().getName() +
                        ": " + string
        );
    }

    void indent() {
        indent(1);
    }

    void indent(int addIndent) {
        if (addIndent > 0) {
            StringBuilder builder = new StringBuilder(indent);
            for (; addIndent > 0; addIndent--) builder.append(indentChars);
            indent = builder.toString();
            return;
        }
        // addIndex < 0
        int remove = Math.abs(addIndent) * indentChars.length();
        if (remove > indent.length()) {
            indent = "";
        } else {
            indent = indent.substring(0, indent.length() - remove);
        }
    }

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

    public void await() {
        Thread thread = new Thread("AWAIT") {
            @Override
            public void run() {
                String msg = "Command #" + (++id);
                println("AWAIT:BEFORE: " + msg);

                try {
                    lock.lock();
                    condition.await();
                    println("AWAIT:AFTER: " + msg);
                } catch (Exception ex) {
                    println(ex.getClass().getSimpleName() + " on AWAIT: " + ex.getMessage());
                } finally {
                    lock.unlock();
                    printThreads("AWAIT:AFTER");
                }
            }
        };
        thread.start();
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

    @Test
    public void test() {
        println("-- blocking queue --");
        indent();

        printThreads();
        await();
        printThreads("after await x2");
        signal(1000);
        printThreads("after signal 1");

        if (scheduledFuture != null) {
            /*while (!scheduledFuture.isDone()) {
                try {
                    println("waiting for scheduledFuture.isDone");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }*/
            println("waiting for scheduledFuture.get");
            try {
                Object something = scheduledFuture.get();
                println("scheduledFuture.get: return " + something);
            } catch (Exception ex) {
                println("scheduledFuture.get failed, " + ex.getMessage());
            }
        }
        scheduler.shutdownNow();
        printThreads("after scheduler.shutdownNow");

        indent(-1);
        println("-- ended --");
    }
}
