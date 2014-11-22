package prog2.concur.exercice3;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;

public class ModernElementalHttpServer {

	public static void main(String[] args) throws Exception {
		// Si aucun arguments ne sont passés à l'application,
		// elle s'arrête.
		if (args.length < 1) {
			System.err.println("Please specify document root directory");
			System.exit(1);
		}

		// Répertoire racine du serveur
		String docRoot = args[0];
		int port = 8080;

		// Si un port est spécifié, il est récupéré
		if (args.length >= 2) {
			port = Integer.parseInt(args[1]);
		}

		// Création la réponse HTTP
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate())
				.add(new ResponseServer("EduJavaServer/1.0"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Définition du gestionnaire de requêtes
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpFileHandler(docRoot));
		
		// Définition du service HTTP
        HttpService httpService = new HttpService(httpproc, reqistry);

        SSLServerSocketFactory sf = null;
        if (port == 8443) {
            // Initialize SSL context
            ClassLoader cl = ElementalHttpServer.class.getClassLoader();
            URL url = cl.getResource("my.keystore");
            if (url == null) {
                System.out.println("Keystore not found");
                System.exit(1);
            }
            KeyStore keystore  = KeyStore.getInstance("jks");
            keystore.load(url.openStream(), "secret".toCharArray());
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, "secret".toCharArray());
            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keymanagers, null, null);
            sf = sslcontext.getServerSocketFactory();
        }
        
        /* * * * * * * * * * * * * * * * * * * *
         * Mise en place de l'executor service
         * * * * * * * * * * * * * * * * * * * */
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        singleThread.execute(new RequestListenerTask(port, httpService, sf));
	}

	/**
	 * Aucune modification par rapport à l'original.
	 */
	static class HttpFileHandler implements HttpRequestHandler {

		private final String docRoot;

		public HttpFileHandler(final String docRoot) {
			super();
			this.docRoot = docRoot;
		}

		@Override
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			String method = request.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}
			String target = request.getRequestLine().getUri();

			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				byte[] entityContent = EntityUtils.toByteArray(entity);
				System.out.println("Incoming entity content (bytes): "
						+ entityContent.length);
			}

			final File file = new File(this.docRoot, URLDecoder.decode(target,
					"UTF-8"));
			if (!file.exists()) {

				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				StringEntity entity = new StringEntity("<html><body><h1>File"
						+ file.getPath() + " not found</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("File " + file.getPath() + " not found");

			} else if (!file.canRead() || file.isDirectory()) {

				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				StringEntity entity = new StringEntity(
						"<html><body><h1>Access denied</h1></body></html>",
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				System.out.println("Cannot read file " + file.getPath());

			} else {

				response.setStatusCode(HttpStatus.SC_OK);
				FileEntity body = new FileEntity(file, ContentType.create(
						"text/html", (Charset) null));
				response.setEntity(body);
				System.out.println("Serving file " + file.getPath());
			}
		}

	}

	/**
	 * La classe implémente Runnable au lieu de Thread.
	 */
	static class RequestListenerTask implements Runnable {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;

		public RequestListenerTask(
				final int port,
				final HttpService httpService, 
				final SSLServerSocketFactory sf) throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = sf != null ? sf.createServerSocket(port) : new ServerSocket(port);
			this.httpService = httpService;
		}

		@Override
		public void run() {
			System.out.println("Listening on port " + this.serversocket.getLocalPort());
			
			/* * * * * * * * * * * * * * * * * * * *
	         * Mise en place de l'executor service
	         * * * * * * * * * * * * * * * * * * * */
			ExecutorService threadExecutor = Executors.newCachedThreadPool();
			
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);

                    /* * * * * * * * * * * * * * * * * * * *
        	         * Ajout d'une nouvelle tâche
        	         * * * * * * * * * * * * * * * * * * * */
                    threadExecutor.execute(new WorkerTask(this.httpService, conn));
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: "
                            + e.getMessage());
                    break;
                }
            }
            
            /* * * * * * * * * * * * * * * * * * * *
	         * Arrêt de toutes les tâches
	         * * * * * * * * * * * * * * * * * * * */
            threadExecutor.shutdown();
		}

	}

	/**
	 * La classe implémente Runnable au lieu de Thread.
	 * => Aucune modifications par rapport à l'original.
	 */
	static class WorkerTask implements Runnable {

		private final HttpService httpservice;
        private final HttpServerConnection conn;
		
		public WorkerTask(
                final HttpService httpservice,
                final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }
		
		@Override
		public void run() {
			System.out.println("New connection task");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
		}
		
	}
}
