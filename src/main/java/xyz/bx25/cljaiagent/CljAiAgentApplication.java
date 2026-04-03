package xyz.bx25.cljaiagent;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import xyz.bx25.cljaiagent.rag.PgVectorVectorStoreConfig;
import xyz.bx25.cljaiagent.tools.WeatherService;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CljAiAgentApplication {

    public static void main(String[] args) {

        SpringApplication.run(CljAiAgentApplication.class, args);
    }
}
