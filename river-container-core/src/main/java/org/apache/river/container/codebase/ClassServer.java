/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 Originally taken from com.sun.jini.tool.ClassServer, then refactored to
 plug into the surrogate container.
 */
package org.apache.river.container.codebase;

import com.sun.jini.logging.Levels;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.vfs2.FileObject;
import org.apache.river.container.Init;
import org.apache.river.container.Injected;
import org.apache.river.container.InjectionStyle;
import org.apache.river.container.LocalizedRuntimeException;
import org.apache.river.container.MessageNames;
import org.apache.river.container.Shutdown;
import org.apache.river.container.work.WorkManager;

/**
 * A simple HTTP server, for serving up JAR and class files. <p> The following
 * items are discussed below: <ul> <li>{@linkplain #main Command line options}
 * <li><a href="#logging">Logging</a> <li><a href="#running">Examples for
 * running ClassServer</a> </ul> <p> <a name="logging"><h3>Logging</h3></a> <p>
 *
 * This implementation uses the {@link Logger} named
 * <code>com.sun.jini.tool.ClassServer</code> to log information at the
 * following logging levels: <p> <table border="1" cellpadding="5"
 * summary="Describes logging performed by ClassServer at different logging
 * levels"> <caption halign="center" valign="top"><b><code>
 *    com.sun.jini.tool.ClassServer</code></b></caption>
 *
 * <tr> <th scope="col">Level</th> <th scope="col">Description</th> </tr> <tr> <td>{@link Level#SEVERE SEVERE}</td>
 * <td>failure to accept an incoming connection</td> </tr> <tr> <td>{@link Level#WARNING WARNING}</td>
 * <td>failure to read the contents of a requested file, failure to find the
 * message resource bundle, failure while executing the
 * <code>-stop</code> option </td> </tr> <tr> <td>{@link Level#INFO INFO}</td>
 * <td>server startup and termination</td> </tr> <tr> <td>{@link Level#CONFIG CONFIG}</td>
 * <td>the JAR files being used for
 * <code>-trees</code></td> </tr> <tr> <td>{@link Levels#HANDLED HANDLED}</td>
 * <td>failure reading an HTTP request or writing a response</td> </tr> <tr> <td>{@link Level#FINE FINE}</td>
 * <td>bad HTTP requests, HTTP requests for nonexistent files</td> </tr> <tr> <td>{@link Level#FINER FINER}</td>
 * <td>good HTTP requests</td> </tr> </table>
 *
 *
 */
public class ClassServer implements CodebaseHandler {

    private static final Logger logger =
            Logger.getLogger(ClassServer.class.getName(), MessageNames.BUNDLE_NAME);
    /**
     * Server socket to accept connections on
     */
    private ServerSocket server;
    @Injected(style = InjectionStyle.BY_TYPE)
    private WorkManager workManager = null;
    @Injected(Strings.CLASS_SERVER_PROPERTIES)
    private Properties properties;
    Map<String, ClassServerCodebaseContext> contexts =
            new HashMap<String, ClassServerCodebaseContext>();

    @Init
    public void init() {
        try {
            establishServerSocket();
            workManager.queueTask(Thread.currentThread().getContextClassLoader(),
                    new Runnable() {

                        @Override
                        public void run() {
                            ClassServer.this.run();
                        }
                    });
        } catch (IOException ex) {
            logger.log(Level.SEVERE, MessageNames.CLASS_SERVER_INIT_FAILED, ex);
            throw new RuntimeException(ex);
        }
    }

    private void establishServerSocket() throws IOException, SocketException {
        server = new ServerSocket();
        server.setReuseAddress(true);
        String initialPortStr = properties.getProperty(Strings.INITIAL_PORT);
        if (initialPortStr == null) {
            throw new LocalizedRuntimeException(
                    MessageNames.BUNDLE_NAME,
                    MessageNames.MISSING_PROPERTY_ENTRY,
                    new Object[]{
                        Strings.CLASS_SERVER_PROPERTIES,
                        Strings.INITIAL_PORT
                    });
        }
        int initialPort = Integer.parseInt(initialPortStr);
        for(int port=initialPort; port<initialPort+100 ;port++)
        try {
            server.bind(new InetSocketAddress(port));
            logger.log(Level.INFO, MessageNames.CLASS_SERVER_ESTABLISHED,
                    new Object[]{server.getLocalSocketAddress(),
                        server.getLocalPort()});
            break;
        } catch (BindException be) {
            logger.log(Level.FINE, MessageNames.PORT_IN_USE, new Integer[] {port});
        }
    }

    /**
     * Just keep looping, spawning a new thread for each incoming request. It's
     * tempting here to have the last operation queue another accept() task
     * rather than setup a loop. Wonder what the ramifications would be? We'd
     * have more opportunities to end the service task, but possibly more
     * development effort.
     */
    public void run() {
        try {
            while (true) {
                final Socket connectedSocket = server.accept();
                /*
                 * Boy, would this be a nice spot to have closures!
                 */
                workManager.queueTask(
                        Thread.currentThread().getContextClassLoader(),
                        new Runnable() {

                            @Override
                            public void run() {
                                processRequest(connectedSocket);
                            }
                        });
            }
        } catch (IOException e) {
            synchronized (this) {
                if (!server.isClosed()) {
                    logger.log(Level.SEVERE, MessageNames.CLASS_SERVER_ERROR_ACCEPTING_CONNECTIONS, e);
                }
                terminate();
            }
        }
    }

    /**
     * Close the server socket, causing the thread to terminate.
     */
    @Shutdown
    public synchronized void terminate() {
        try {
            server.close();
        } catch (IOException e) {
            logger.log(Level.FINE, MessageNames.CLASS_SERVER_EXCEPTION_DURING_SHUTDOWN, e);
        }
        logger.log(Level.INFO, MessageNames.CLASS_SERVER_TERMINATED,
                new Object[]{server.getLocalSocketAddress(),
                    server.getLocalPort()});
    }

    /**
     * Returns the port on which this server is listening.
     */
    public int getPort() {
        return server.getLocalPort();
    }

    /**
     * Returns the hostname that the server is listening to.
     *
     * @return
     */
    public String getHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getCanonicalHostName();
    }

    /**
     * Read up to CRLF, return false if EOF
     */
    private static boolean readLine(InputStream in, StringBuffer buf)
            throws IOException {
        while (true) {
            int c = in.read();
            if (c < 0) {
                return buf.length() > 0;
            }
            /*
             * The characters below are part of the http protocol and not
             * localizable, so we're OK with character literals.
             */
            if (c == '\r') {
                in.mark(1);
                c = in.read();
                if (c != '\n') {
                    in.reset();
                }
                return true;
            }
            if (c == '\n') {
                return true;
            }
            buf.append((char) c);
        }
    }

    /**
     * Parse % HEX HEX from s starting at i
     */
    private static char decode(String s, int i) {
        return (char) Integer.parseInt(s.substring(i + 1, i + 3), 16);
    }

    /**
     * Decode escape sequences
     */
    private static String decode(String path) {
        try {
            for (int i = path.indexOf('%');
                    i >= 0;
                    i = path.indexOf('%', i + 1)) {
                char c = decode(path, i);
                int n = 3;
                if ((c & 0x80) != 0) {
                    switch (c >> 4) {
                        case 0xC:
                        case 0xD:
                            n = 6;
                            c = (char) (((c & 0x1F) << 6)
                                    | (decode(path, i + 3) & 0x3F));
                            break;
                        case 0xE:
                            n = 9;
                            c = (char) (((c & 0x0f) << 12)
                                    | ((decode(path, i + 3) & 0x3F) << 6)
                                    | (decode(path, i + 6) & 0x3F));
                            break;
                        default:
                            return null;
                    }
                }
                path = path.substring(0, i) + c + path.substring(i + n);
            }
        } catch (Exception e) {
            return null;
        }
        return path;
    }

    /**
     * Read the request/response and return the initial line.
     */
    private static String getInput(Socket sock, boolean isRequest)
            throws IOException {
        BufferedInputStream in =
                new BufferedInputStream(sock.getInputStream(), 256);
        StringBuffer buf = new StringBuffer(80);
        do {
            if (!readLine(in, buf)) {
                return null;
            }
        } while (isRequest && buf.length() == 0);
        String initial = buf.toString();
        do {
            buf.setLength(0);
        } while (readLine(in, buf) && buf.length() > 0);
        return initial;
    }

    @Override
    public CodebaseContext createContext(String appId) {
        // Create a context
        ClassServerCodebaseContext context = new ClassServerCodebaseContext(this, appId);
        // Assign a context prefix (url-shortened)
        contexts.put(appId, context);
        return context;
    }

    @Override
    public void destroyContext(CodebaseContext context) {
        // Remove all the jar mappings.
        //destroy the context.
        contexts.remove(context.getAppId());
    }
    private static ResourceBundle resources;
    private static boolean resinit = false;

    /**
     * Canonicalize the path
     */
    private String canon(String path) {
        if (path.regionMatches(true, 0, "http://", 0, 7)) {
            int i = path.indexOf('/', 7);
            if (i < 0) {
                path = "/";
            } else {
                path = path.substring(i);
            }
        }
        path = decode(path);
        if (path == null || path.length() == 0 || path.charAt(0) != '/') {
            return null;
        }
        return path.substring(1);
    }

    private String parsePathFromRequest(String req, boolean get) {
        String path = req.substring(get ? 4 : 5);
        int i = path.indexOf(' ');
        if (i > 0) {
            path = path.substring(0, i);
        }
        path = canon(path);
        return path;
    }

    private boolean processBadRequest(String[] args, DataOutputStream out) throws IOException {
        logger.log(Level.FINE, MessageNames.CLASS_SERVER_BAD_REQUEST,
                args);
        out.writeBytes("HTTP/1.0 400 Bad Request\r\n\r\n");
        out.flush();
        return true;
    }

    /**
     * Read specified number of bytes and always close the stream.
     */
    private byte[] getBytes(FileObject fo)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        InputStream in = fo.getContent().getInputStream();
        int bytesRead = in.read(buffer);
        while (bytesRead > 0) {
            out.write(buffer, 0, bytesRead);
            bytesRead = in.read(buffer);
        }
        byte[] bytes = out.toByteArray();
        out.close();
        in.close();
        return bytes;
    }

    /**
     * Return the bytes of the requested file, or null if not found.
     */
    private byte[] getBytes(String path) throws IOException {
        FileObject fo = findFileObjectForPath(path);
        if (fo == null) {
            return null;
        }
        return getBytes(fo);
    }

    FileObject findFileObjectForPath(String path) {
        /*
         * First path segment is appid.
         */
        StringTokenizer tok = new StringTokenizer(path, Strings.SLASH, false);
        FileObject ret = null;
        try {
            String appId = tok.nextToken();
            String jarName = tok.nextToken();
            ClassServerCodebaseContext context = contexts.get(appId);
            ret = context.fileEntries.get(jarName);
        } catch (Throwable t) {
            logger.log(Level.INFO, MessageNames.CLASS_SERVER_REJECTED_PATH,
                    path);
        }
        return ret;
    }

    private void writeHeader(DataOutputStream out, byte[] bytes) throws IOException {
        out.writeBytes("HTTP/1.0 200 OK\r\n");
        out.writeBytes("Content-Length: " + bytes.length + "\r\n");
        out.writeBytes("Content-Type: application/java\r\n\r\n");
    }

    private boolean processRequest(Socket sock) {
        try {
            DataOutputStream out =
                    new DataOutputStream(sock.getOutputStream());
            String req;
            try {
                req = getInput(sock, true);
            } catch (Exception e) {
                logger.log(Level.FINE, "reading request", e);
                return true;
            }
            if (req == null) {
                return true;
            }
            String[] args = new String[3];
            boolean get = req.startsWith("GET ");
            if (!get && !req.startsWith("HEAD ")) {
                processBadRequest(args, out);
            }
            String path = parsePathFromRequest(req, get);
            if (path == null) {
                return processBadRequest(args, out);
            }
            if (args != null) {
                args[0] = path;
            }
            args[1] = sock.getInetAddress().getHostName();
            args[2] = Integer.toString(sock.getPort());

            logger.log(Level.FINER,
                    get
                    ? MessageNames.CLASS_SERVER_RECEIVED_REQUEST
                    : MessageNames.CLASS_SERVER_RECEIVED_PROBE,
                    args);
            byte[] bytes;
            try {
                bytes = getBytes(path);
            } catch (Exception e) {
                logger.log(Level.WARNING, MessageNames.CLASS_SERVER_EXCEPTION_GETTING_BYTES, e);
                out.writeBytes("HTTP/1.0 500 Internal Error\r\n\r\n");
                out.flush();
                return true;
            }
            if (bytes == null) {
                logger.log(Level.FINE, MessageNames.CLASS_SERVER_NO_CONTENT_FOUND, path);
                out.writeBytes("HTTP/1.0 404 Not Found\r\n\r\n");
                out.flush();
                return true;
            }
            writeHeader(out, bytes);
            if (get) {
                out.write(bytes);
            }
            out.flush();
            return false;
        } catch (Exception e) {
            logger.log(Level.FINE, MessageNames.CLASS_SERVER_EXCEPTION_WRITING_RESPONSE, e);
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
            }
        }
        return false;
    }
}
