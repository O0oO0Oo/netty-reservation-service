package org.server.rsaga.netty;

import org.server.rsaga.netty.http.HttpPipelineInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "org.server.rsaga",
})
@EnableJpaRepositories(basePackages = {
        "org.server.rsaga.business.infra.repository",
        "org.server.rsaga.reservableitem.infra.repository",
        "org.server.rsaga.reservation.infra.repository",
        "org.server.rsaga.user.infra.repository",
        "org.server.rsaga.payment.infra.repository"
})
public class NettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);

        new NettyReservationServer("192.168.35.191",8080, new HttpPipelineInitializer()).start();
    }
}