package io.github.bytes_chasser.impl.jsch;


import io.github.bytes_chasser.ESSHContext;
import io.github.bytes_chasser.ESSHContextImpl;
import io.github.bytes_chasser.TestData;
import org.junit.jupiter.api.*;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleTest {
    private static ESSHContext esshContext;
    static String host;
    static String user;
    static String pass;


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
    void MP_SINGL_ARGS() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        String text;

        text = simpleClient.ls(host, user, pass);
        System.out.println(text);
        assertNotNull(text);
        assertTrue(text.length() > 0);

    }

    @Test
    @Order(2)
    void EX_MULT_ARGS() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        String text;
        text = simpleClient.list(host, user, pass);
        System.out.println(text);
        assertNotNull(text);
        assertTrue(text.length() > 0);

    }

    @Test
    @Order(3)
    void configure_context_cred() {
        esshContext.setHost(host);
        esshContext.setUser(user);
        esshContext.setPass(pass);
    }

    @Test
    @Order(4)
    void MP_SINGL() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        String text;

        text = simpleClient.ls();
        System.out.println(text);
        assertNotNull(text);
        assertTrue(text.length() > 0);
    }

    @Test
    @Order(4)
    void EX_MULT() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        String text;
        text = simpleClient.list();
        System.out.println(text);
        assertNotNull(text);
        assertTrue(text.length() > 0);
    }

    @Test
    @Order(5)
    void configure_context_parser() {
        esshContext.parser(TestData.Output.class, new TestData.OutputParser());
    }

    @Test
    @Order(6)
    void MP_SINGL_PARSE() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        TestData.Output output;
        output = simpleClient.ls2();
        System.out.println(output.text);
        assertNotNull(output.text);
        assertTrue(output.text.length() > 0);
    }

    @Test
    @Order(6)
    void EX_MULT_PARSE() {
        TestData.SimpleClient simpleClient = esshContext.client(TestData.SimpleClient.class);
        TestData.Output output;
        output = simpleClient.list2();
        System.out.println(output.text);
        assertNotNull(output.text);
        assertTrue(output.text.length() > 0);
    }
}
