package com.light;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class LightServer {
	private ServerContext serverContext;
	private Selector selector;

	private static final Logger logger = LogManager.getLogger();

	public void start(String[] args, String... controllerPacakgePaths) throws IOException {
		init(args, controllerPacakgePaths);
		run();
	}

	private void init(String[] args, String... controllerPacakgePaths) throws UnknownHostException {
		long start = System.currentTimeMillis();
		initContext(args);
		initController(controllerPacakgePaths);
		initServer();
		long end = System.currentTimeMillis();
		logger.info("服务器启动! {}: {} 花费时间 {} ms", serverContext.getIp().getAddress(), serverContext.getPort(), end - start);
	}

	private void initContext(String[] args) throws UnknownHostException {
		if (args.length < 1 || args[0] != "start") {
			logger.error("{}错误", args);
			System.exit(1);
		}
		InetAddress ip;
		int port;
		if (args.length == 1 && args[1].matches(".+:\\d+")) {
			String[] addressAndPort = args[1].split(":");
			ip = InetAddress.getByName(addressAndPort[0]);
			port = Integer.valueOf(addressAndPort[1]);
		} else {
			ip = InetAddress.getByName("127.0.0.1");
			port = 8080;
		}
		serverContext.setIp(ip);
		serverContext.setPort(port);
	}

	private void initController(String... controllerPacakgePaths) {

	}

	private void initServer() {
		ServerSocketChannel serverSocketChannel;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(this.serverContext.getIp(), this.serverContext.getPort()));
			serverSocketChannel.configureBlocking(false);
			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() throws IOException {
		while (true) {
			try {
				if (selector.select(500) == 0)
					continue;
			} catch (Exception e) {
				e.printStackTrace();
			}

			Set<SelectionKey> readykeys = selector.keys();
			Iterator<SelectionKey> iterator = readykeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				if (selectionKey.isAcceptable()) {
					accept(selectionKey);
				} else if (selectionKey.isReadable()) {
					read(selectionKey);
				} else if (selectionKey.isWritable()) {
					write(selectionKey);
				}
			}
		}
	}

	private void write(SelectionKey selectionKey) throws IOException {
		logger.info("write");
		SocketChannel client = (SocketChannel) selectionKey.channel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		client.write(byteBuffer);
		//如果数据写完了
		selectionKey.cancel();
		selectionKey.interestOps(~SelectionKey.OP_WRITE & selectionKey.interestOps());
		client.close();

	}

	private void read(SelectionKey selectionKey) throws IOException {
		logger.info("read");
		SocketChannel client = (SocketChannel) selectionKey.channel();
		// TODO: 2019-03-27 读数据
		System.out.println("read data");
		client.register(selector, SelectionKey.OP_WRITE);
	}

	private void accept(SelectionKey selectionKey) throws IOException {
		logger.info("accept");
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
		SocketChannel client = serverSocketChannel.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
	}
}
