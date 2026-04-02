package xyz.bx25.cljaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import xyz.bx25.cljaiagent.rag.PgVectorVectorStoreConfig;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CljAiAgentApplication {

    public static void main(String[] args) {

        SpringApplication.run(CljAiAgentApplication.class, args);
    }

}
