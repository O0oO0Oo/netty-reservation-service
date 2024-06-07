package com.server.reservation;

import com.server.reservation.netty.NettyReservationServer;
import com.server.reservation.netty.http.HttpPipelineInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationApplication.class, args);

		new NettyReservationServer("localhost",8080, new HttpPipelineInitializer()).start();
	}
}