package com.light;

import lombok.*;

import java.net.InetAddress;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServerContext {
	private InetAddress ip;
	private int port;
}
