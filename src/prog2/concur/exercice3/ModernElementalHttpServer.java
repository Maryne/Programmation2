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

/**
 * Classe ModernElementalHttpServer
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
public class ModernElementalHttpServer {

	public static void main(String[] args) throws Exception {
		ModernElementalHttpServer srv = new ModernElementalHttpServer();
		srv.start();
	}

	private String _docRoot;
	private int _port;
	private int _maxConnections;

	public final String CONFIG_FILE_PATH = "resources/http_server.ini";

	/**
	 * Instancie un nouveau serveur.
	 */
	public ModernElementalHttpServer() {
		readConfig();
	}

	/**
	 * Lit le fichier de configuration et initialise les attributs.
	 */
	private void readConfig() {
		try {
			SimpleIniReader sir = new SimpleIniReader(CONFIG_FILE_PATH);

			_docRoot = sir.get("wwwroot");
			_port = Integer.parseInt(sir.get("port"));
			_maxConnections = Integer.parseInt(sir
					.get("max_simultaneous_connections"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Démarre l'écoute des requêtes.
	 * 
	 * @throws Exception
	 */
	private void start() throws Exception {
		// Création la réponse HTTP
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate())
				.add(new ResponseServer("EduJavaServer/1.0"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Définition du gestionnaire de requêtes
		UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new HttpFileHandler(_docRoot));

		// Définition du service HTTP
		HttpService httpService = new HttpService(httpproc, reqistry);

		SSLServerSocketFactory sf = null;
		if (_port == 8443) {
			// Initialize SSL context
			ClassLoader cl = ModernElementalHttpServer.class.getClassLoader();
			URL url = cl.getResource("my.keystore");
			if (url == null) {
				System.out.println("Keystore not found");
				System.exit(1);
			}
			KeyStore keystore = KeyStore.getInstance("jks");
			keystore.load(url.openStream(), "secret".toCharArray());
			KeyManagerFactory kmfactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, "secret".toCharArray());
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(keymanagers, null, null);
			sf = sslcontext.getServerSocketFactory();
		}

		/**
		 * Mise en place de l'executor service
		 **/
		ExecutorService singleThread = Executors.newSingleThreadExecutor();
		singleThread.execute(new RequestListenerTask(_port, httpService, sf, _maxConnections));
	}

	/**
	 * Classe HttpFileHandler Aucune modification par rapport à l'original.
	 * 
	 * @author Papillon Maxence & Maryne Teissier
	 */
	static class HttpFileHandler implements HttpRequestHandler {

		private final String docRoot;

		/**
		 * Constructeur
		 * 
		 * @param docroot
		 *            docroot
		 */
		public HttpFileHandler(final String docRoot) {
			super();
			this.docRoot = docRoot;
		}

		/**
		 * Méthode handle
		 * 
		 * @param request
		 *            requete http
		 * @param response
		 *            reponse http
		 * @param context
		 *            contexte http
		 */
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
	 * Classe RequestListenerTask La classe implémente Runnable au lieu de
	 * Thread.
	 * 
	 * @author Papillon Maxence & Maryne Teissier
	 */
	static class RequestListenerTask implements Runnable {

		private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
		private final ServerSocket serversocket;
		private final HttpService httpService;
		private int _maxConnections;

		/**
		 * Constructeur
		 * 
		 * @param port
		 *            port
		 * @param httpService
		 *            httpService
		 * @param sf
		 *            SSLServerSocketFactory
		 */
		public RequestListenerTask(final int port,
				final HttpService httpService, final SSLServerSocketFactory sf,
				int maxConnections) throws IOException {
			this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
			this.serversocket = sf != null ? sf.createServerSocket(port)
					: new ServerSocket(port);
			this.httpService = httpService;
			this._maxConnections = maxConnections;
		}

		/**
		 * Méthode run qui définit ce que le thread doit éxécuter
		 **/
		@Override
		public void run() {
			System.out.println("Listening on port "
					+ this.serversocket.getLocalPort());

			/**
			 * Mise en place de l'executor service
			 **/
			ExecutorService threadExecutor = Executors
					.newFixedThreadPool(_maxConnections);

			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					System.out.println("Incoming connection from "
							+ socket.getInetAddress());
					HttpServerConnection conn = this.connFactory
							.createConnection(socket);

					/**
					 * Ajout d'une nouvelle tâche
					 **/
					threadExecutor.execute(new WorkerTask(this.httpService,
							conn));
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					break;
				}
			}
		}
	}

	/**
	 * Classe WorkerTask La classe implémente Runnable au lieu de Thread. =>
	 * Aucune modifications par rapport à l'original.
	 * 
	 * @author Papillon Maxence & Maryne Teissier
	 */
	static class WorkerTask implements Runnable {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		/**
		 * Constructeur
		 * 
		 * @param Httpservice
		 *            servicehttp
		 * @param conn
		 *            connection htpp
		 * 
		 */
		public WorkerTask(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		/**
		 * Méthode run qui définit ce que le thread doit éxécuter
		 */
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
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
				}
			}
		}

	}
}
