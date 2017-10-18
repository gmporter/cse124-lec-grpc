package atm;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class ATMClient {
	private static final Logger logger = Logger.getLogger(ATMClient.class.getName());

	private final ManagedChannel channel;
	private final ATMServerGrpc.ATMServerBlockingStub blockingStub;

	private String host;
	private int port;

	/** Construct client connecting to ATM server at {@code host:port}. */
	public ATMClient(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port)
				// Channels are secure by default (via SSL/TLS). For the example we disable TLS
				// to avoid
				// needing certificates.
				.usePlaintext(true).build());
		this.host = host;
		this.port = port;
	}

	/** Construct client for accessing ATM server using the existing channel. */
	ATMClient(ManagedChannel channel) {
		this.channel = channel;
		blockingStub = ATMServerGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/** Test connectivity to the server. */
	public void testConnectivity() {
		logger.info("Testing connectivity to " + host + ":" + port);
		PingRequest request = PingRequest.newBuilder().build();

		try {
			blockingStub.ping(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Server is up!");
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			logger.log(Level.SEVERE, "Usage: atm.ATMClient host port");
			System.exit(1);
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);

		ATMClient client = new ATMClient(host, port);
		try {
			client.testConnectivity();
		} finally {
			client.shutdown();
		}
	}
}