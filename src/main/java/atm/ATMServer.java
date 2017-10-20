package atm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import atm.LoginToken.Builder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

public class ATMServer {
	private static final Logger logger = Logger.getLogger(ATMServer.class.getName());
	private Server server;
	private int port;

	public ATMServer(int port) {
		this.port = port;
	}

	private void start() throws IOException {
		server = ServerBuilder.forPort(port).addService(new ATMServerImpl()).build().start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown
				// hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				ATMServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 1) {
			logger.log(Level.SEVERE, "Usage: atm.ATMServer <port>");
			System.exit(1);
		}
		int port = Integer.parseInt(args[0]);
		final ATMServer server = new ATMServer(port);
		server.start();
		server.blockUntilShutdown();
	}

	static class ATMServerImpl extends ATMServerGrpc.ATMServerImplBase {
		@Override
		public void ping(atm.PingRequest request, io.grpc.stub.StreamObserver<atm.PingResponse> responseObserver) {
			PingResponse reply = PingResponse.newBuilder().build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
		
		@Override
		public void login(atm.LoginRequest request, io.grpc.stub.StreamObserver<atm.LoginToken> responseObserver) {
			int account_num = request.getAccountNum();
			int pin = request.getPin();
			
			long loginToken = 0;
			
			// Valid accounts:
			//    1234 (pin 5555)
			//    9876 (pin 1122)
			
			if (account_num == 1234 && pin == 5555) {
				loginToken = 1;
				LoginToken reply = LoginToken.newBuilder().setToken(loginToken).build();
				responseObserver.onNext(reply);
				responseObserver.onCompleted();
				
			} else if (account_num == 9876 && pin == 1122) {
				loginToken = 2;
				LoginToken reply = LoginToken.newBuilder().setToken(loginToken).build();
				responseObserver.onNext(reply);
				responseObserver.onCompleted();
				
			} else {
				StatusRuntimeException e = new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("No such account number"));
				responseObserver.onError(e);
			}
		}
	}

}
