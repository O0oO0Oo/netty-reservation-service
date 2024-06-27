package org.server.reservation;

import org.server.reservation.netty.NettyReservationServer;
import org.server.reservation.netty.http.HttpPipelineInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// TODO : 완료되면 모두 임포트
@SpringBootApplication(scanBasePackages = {
        "org.server.reservation"
})
@EnableJpaRepositories(basePackages = {
        "org.server.reservation.business.repository",
        "org.server.reservation.item.repository",
        "org.server.reservation.record.repository",
        "org.server.reservation.user.repository"
})
public class NettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);

        new NettyReservationServer("localhost",8080, new HttpPipelineInitializer()).start();
    }
}
