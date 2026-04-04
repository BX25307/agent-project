package xyz.bx25.cljaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import xyz.bx25.cljaiagent.agent.model.AgentState;

/**
 * Base ReAct-style agent.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();

    public abstract String act();

    protected String onThinkCompletedBeforeAct() {
        return null;
    }

    protected String onThinkCompletedWithoutAct() {
        return "think done, no need to act";
    }

    protected String onActCompleted() {
        return act();
    }

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                setState(AgentState.FINISHED);
                return onThinkCompletedWithoutAct();
            }
            String thought = onThinkCompletedBeforeAct();
            onActCompleted();
            return thought;
        } catch (Exception e) {
            log.error("step failed", e);
            return "step failed" + e.getMessage();
        }
    }
}
