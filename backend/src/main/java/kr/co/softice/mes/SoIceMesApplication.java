package kr.co.softice.mes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * SoIce MES - Main Application Class
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * Base MES Platform - Manufacturing Execution System
 *
 * @author Moon Myung-seop (msmoon@softice.co.kr)
 * @company SoftIce Co., Ltd.
 * @since 2026-01-17
 * @version 0.1.0-SNAPSHOT
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class SoIceMesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoIceMesApplication.class, args);
        log.info("\n" +
                "========================================================\n" +
                "                                                        \n" +
                "  SOFTICE - Manufacturing Execution System             \n" +
                "           Base MES Platform v0.1.0                    \n" +
                "                                                        \n" +
                "  Developer: Moon Myung-seop (msmoon@softice.co.kr)   \n" +
                "  Company: SoftIce Co., Ltd.                           \n" +
                "                                                        \n" +
                "========================================================");
    }
}
