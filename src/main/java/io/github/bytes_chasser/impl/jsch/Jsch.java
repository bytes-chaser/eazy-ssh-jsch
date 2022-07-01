package io.github.bytes_chasser.impl.jsch;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.bytes_chasser.ConnectionData;
import io.github.bytes_chasser.ExecData;
import io.github.bytes_chasser.SSH;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SSH implementation over Jsch.
 *
 */
public final class Jsch implements SSH {

    public static final int OUTPUT_WAIT_MS      = 350;
    public static final int OUTPUT_WAIT_REPEATS = 5;
    public static final int CONNECTION_TIMEOUT  = 0;

    public static final Properties SESSION_CFG;

    static {
        SESSION_CFG = new Properties();
        SESSION_CFG.put("StrictHostKeyChecking", "no");
    }

    final Logger logger;

    private final int outputWaitMs;
    private final int outputWaitRepeats;
    private final int connectionTimeout;
    private final Properties sessionConfig;

    /**
     *  Creates new Jsch SSH pool
     * @param outputWaitMs - new SSH channel data await time.
     *                     Makes sense to make bigger for slow performance systems for reliable output.
     *                     For fast performing systems makes sense to make it smaller in order to increase execution performance.
     * @param outputWaitRepeats - number of the channel data awaiting repeats. Makes sense to make bigger for slow performance systems for reliable output.
     *                 For fast performing systems makes sense to make it smaller in order to increase execution performance.
     * @param connectionTimeout - Jsch channel connect timeout
     * @param sessionConfig - Jsch SSH session configuration properties
     */
    public Jsch(int outputWaitMs, int outputWaitRepeats, int connectionTimeout, Properties sessionConfig) {
        this.outputWaitMs = outputWaitMs;
        this.outputWaitRepeats = outputWaitRepeats;
        this.connectionTimeout = connectionTimeout;
        this.sessionConfig = sessionConfig;
        this.logger = Logger.getLogger("essh.jsch." + this);
    }


    /**
     * Creates new Jsch SSH pool with outputWaitMs=OUTPUT_WAIT_MS, outputWaitRepeats=OUTPUT_WAIT_REPEATS, connectionTimeout=CONNECTION_TIMEOUT, sessionConfig=SESSION_CFG
     */
    public Jsch() {
        this(OUTPUT_WAIT_MS, OUTPUT_WAIT_REPEATS, CONNECTION_TIMEOUT, SESSION_CFG);
    }


    public String execute(ExecData execData) throws Exception {

        ConnectionData connData = execData.getConnectionData();
        String[] commands = execData.getCommands();
        int outputStartCommandIndex = execData.getOutputStartCommandIndex();

        StringBuilder result = new StringBuilder();
        String host = connData.getHost();
        String user = connData.getUser();
        String pass = connData.getPassword();
        int port = connData.getPort();

        String pfx = Thread.currentThread().getName() + ":" + user + "@" + host;

        Session session = getSession(pfx, host, user, pass, port);
        ChannelShell channel = getChannel(session, pfx);
        PipedInputStream inputStream = getInputStream(channel);
        BufferedWriter bufferedWriter = getBufferedWriter(channel);
        connect(pfx, session, channel);

        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            logger.log(Level.FINE,pfx + ": Executing'" + command + "'");
            bufferedWriter.write(command);
            bufferedWriter.flush();

            if(i == outputStartCommandIndex) result = new StringBuilder();
            int readCounter = 0;

            while (readCounter < outputWaitRepeats) {
                logger.log(Level.FINE,pfx + ": Checking response update. Try " + readCounter);
                while (inputStream.available() > 0) {
                    readCounter = 0;
                    char read = (char) inputStream.read();
                    logger.log(Level.FINEST,pfx + ": Response reading '" + read + "'");
                    result.append(read);
                }
                readCounter++;
                Thread.sleep(outputWaitMs);
            }
        }

        inputStream.close();
        bufferedWriter.close();
        channel.disconnect();
        session.disconnect();

        return result.toString();
    }


    private void connect(String key, Session session, ChannelShell channel) throws JSchException {
        if (!channel.isConnected()) {
            logger.log(Level.FINE,key + ": chanel connection");
            try {
                channel.connect(connectionTimeout);
            } catch (JSchException e) {
                if("session is down".equals(e.getMessage())) {
                    logger.log(Level.FINE,key + ": session is down. Restarting Session and channel connections");
                    session.connect();
                    channel.connect();
                }
            }
        }
    }


    private BufferedWriter getBufferedWriter(ChannelShell channel) throws IOException, JSchException {
        BufferedWriter outputStream;
        try {
            outputStream = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
        } catch (Exception e) {
            channel.disconnect();
            channel.getSession().disconnect();
            throw e;
        }
        return outputStream;
    }


    private PipedInputStream getInputStream(ChannelShell channel) throws IOException, JSchException {
        PipedInputStream inputStream;
        try {
            inputStream = (PipedInputStream) channel.getInputStream();
        } catch (Exception e) {
            channel.disconnect();
            channel.getSession().disconnect();
            throw e;
        }
        return inputStream;
    }


    private ChannelShell getChannel(Session session, String key) throws JSchException {

        ChannelShell channel;
        try {
            channel = createChannel(key, session);
        } catch (JSchException channelException) {
            if("session is down".equals(channelException.getMessage())) {
                logger.log(Level.FINE, key +": Session is down. Trying to reconnect...");
                session.connect();
                try {
                    channel = createChannel(key, session);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, key + ": Unable to reconnect the session");
                    session.disconnect();
                    throw channelException;
                }
            } else {
                session.disconnect();
                throw channelException;
            }
        }
        return channel;
    }


    private Session getSession(String key, String host, String user, String pass, int port) throws JSchException {
        Session session = createSession(key,host, user, pass, port);
        session.connect();
        return session;
    }


    private Session createSession(String key, String host, String user, String pass, int port) throws JSchException {
        logger.log(Level.FINE, "New Session creation for " + key);
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(pass);
        session.setConfig(sessionConfig);
        return session;
    }


    private ChannelShell createChannel(String key, Session session) throws JSchException {
        logger.log(Level.FINE, "New Shell Channel creation for " + key);
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPty(true);
        return channel;
    }
}