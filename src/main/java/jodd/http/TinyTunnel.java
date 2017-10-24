package jodd.http;

import jodd.props.Props;
import jodd.typeconverter.Convert;
import jodd.util.ClassLoaderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TinyTunnel extends HttpTunnel {

	public static void main(String[] args) throws IOException {
		File propsFile = new File("tinytunnel.props");

		final InputStream in;
		if (propsFile.exists()) {
			in = new FileInputStream(propsFile);
		}
		else {
			in = ClassLoaderUtil.getResourceAsStream("tinytunnel.props");
		}

		Props props = new Props();
		props.load(in);
		in.close();

		TinyTunnel tunnel = new TinyTunnel();

		tunnel.socketBacklog =
			Convert.toIntValue(props.getValue("server.socket.backlog"), 50);

		tunnel.threadPoolSize =
			Convert.toIntValue(props.getValue("server.threadPool.size"), 10);

		tunnel.listenPort =
			Convert.toIntValue(props.getValue("server.listen.port"), 8888);

		tunnel.targetHost = props.getValue("target.host");

		tunnel.targetPort =
			Convert.toIntValue(props.getValue("target.port"), 8080);

		System.out.print("TinyTunnel started on port " + tunnel.listenPort);
		System.out.print("(" + tunnel.socketBacklog + "/");
		System.out.print(tunnel.threadPoolSize + ") to ");
		System.out.println(tunnel.targetHost + ':' + tunnel.targetPort);

		tunnel.start();
	}

	@Override
	protected Runnable onSocketConnection(Socket socket) {
		return new HttpTunnelConnection(socket) {
			@Override
			protected void onRequest(HttpRequest request) {

				// remove gzip
				request.removeHeader("Accept-Encoding");

				System.out.println("\n\n\n---------------------------------------->>>");

				System.out.println(request);

				super.onRequest(request);
			}

			@Override
			protected void onResponse(HttpResponse response) {
				System.out.println("----------------------------------------<<<");

				System.out.println(response);

				super.onResponse(response);

				System.out.println("========================================");
			}
		};
	}
}