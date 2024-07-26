package org.server.rsaga.netty;

import org.server.rsaga.netty.http.HttpPipelineInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// TODO : 완료되면 모두 임포트
@SpringBootApplication(scanBasePackages = {
        "org.server.rsaga"
})
@EnableJpaRepositories(basePackages = {
        "org.server.rsaga.business.repository",
        "org.server.rsaga.reservableitem.repository",
        "org.server.rsaga.reservation.repository",
        "org.server.rsaga.user.repository"
})
public class NettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);

        new NettyReservationServer("localhost",8080, new HttpPipelineInitializer()).start();
    }
}
