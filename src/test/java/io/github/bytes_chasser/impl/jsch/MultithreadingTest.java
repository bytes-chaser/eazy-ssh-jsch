package io.github.bytes_chasser.impl.jsch;

import io.github.bytes_chasser.ESSHContext;
import io.github.bytes_chasser.ESSHContextImpl;
import io.github.bytes_chasser.TestData;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultithreadingTest {


    private static String host;
    private static String user;
    private static String pass;
    private static ESSHContext esshContext;

    static class Runner implements Callable<String> {

        ESSHContext esshContext;

        public Runner(ESSHContext esshContext) {
            this.esshContext = esshContext;
        }

        @Override
        public String call() {
            TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
            return simpleClient.list(host, user, pass);

        }
    }

    @Test
    @Order(1)
    void configure() {
        host = TestData.props.getProperty("ssh.host.1");
        user = TestData.props.getProperty("ssh.user.1");
        pass = TestData.props.getProperty("ssh.pass.1");

        Jsch jsch = new Jsch();
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        jsch.logger.addHandler(consoleHandler);
        jsch.logger.setLevel(Level.FINE);

        esshContext = Assertions.assertDoesNotThrow(() ->
                new ESSHContextImpl()
                        .register(TestData.SimpleClient.class)
                        .create(jsch)
        );
    }

    @Test
    @Order(2)
    void test() {
        Runner runner0 = new Runner(esshContext);
        Runner runner1 = new Runner(esshContext);
        Runner runner2 = new Runner(esshContext);
        Runner runner3 = new Runner(esshContext);
        Runner runner4 = new Runner(esshContext);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = assertDoesNotThrow(() -> executorService.invokeAll(
                List.of(runner0, runner1, runner2, runner3, runner4)));


        List<String> list = futures.stream().map(stringFuture -> {
            try {
                return stringFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList();

        assertEquals(5, list.size());

        assertEquals(list.get(0), list.get(1));
        assertEquals(list.get(0), list.get(2));
        assertEquals(list.get(0), list.get(3));
        assertEquals(list.get(0), list.get(4));

    }
}
