package chatclient;

import org.kohsuke.args4j.Option;

public class CommandLineValues {
		@Option(required = true, name = "-h", aliases ={"--host"}, usage="Host Address")
		private String host;
		
		@Option(required = true, name = "-p", aliases = {"--port"}, usage="Port Address")
		private int port = 4444;

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}
	}


