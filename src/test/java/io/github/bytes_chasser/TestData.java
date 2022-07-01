package io.github.bytes_chasser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestData {

    public static Properties props = new Properties();

    static {
        try(InputStream is = ClassLoader.getSystemResourceAsStream("test.properties")) {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Output{
        public final String text;


        public Output(String text) {
            this.text = text;
        }
    }

    public static class OutputParser implements ESSHParser<Output> {

        @Override
        public Output parse(String output) {
            return new Output(output);
        }
    }

    public interface SimpleClient extends ESSHClient {

        @Exec(commands = "")
        String enter(String host, String user, String pass);

        @Exec(commands = {"", "", "", "ls"})
        String list(String host, String user, String pass);


        @Exec(commands = {"", "", "", "ls"})
        String list();

        @Exec(commands = {"", "", "", "ls"})
        TestData.Output list2();


        String ls(String host, String user, String pass);

        String ls();

        @Exec(commands = {"", "", "", "ls"})
        TestData.Output ls2();
    }

}
