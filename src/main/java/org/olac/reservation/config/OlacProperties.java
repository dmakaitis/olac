package org.olac.reservation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("olac")
@Data
public class OlacProperties {

    private int maxTickets;

}
