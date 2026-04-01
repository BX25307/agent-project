package xyz.bx25.cljaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义Re2 Advisor
 */
@Slf4j

public class ReReadingAdvisor implements BaseAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
			{re2_input_query}
			Read the question again: {re2_input_query}
			""";

	private final String re2AdviseTemplate;
	private int order = 0;

	public ReReadingAdvisor() {
		this.re2AdviseTemplate=DEFAULT_RE2_ADVISE_TEMPLATE;
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

    @Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		String augmentedUserText = PromptTemplate.builder()
			.template(this.re2AdviseTemplate)
			.variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
			.build()
			.render();
//		String key="hello";
//		String value="小金";
//		chatClientRequest.context().put(key,value);
//		log.info("put成功:   key-{},value-{}",key,value);
		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
			.build();
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public int getOrder() {
		return 0;
	}


}