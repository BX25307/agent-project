package xyz.bx25.cljaiagent.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class MyPromptTemplate extends PromptTemplate {

    public static final String DEFAULT_TEMPLATE_PATH = "prompts/conversation-prompt.st";

    public MyPromptTemplate() {
        this(DEFAULT_TEMPLATE_PATH);
    }

    public MyPromptTemplate(String templatePath) {
        this(new ClassPathResource(templatePath));
    }

    public MyPromptTemplate(Resource resource) {
        super(resource);
    }
}
