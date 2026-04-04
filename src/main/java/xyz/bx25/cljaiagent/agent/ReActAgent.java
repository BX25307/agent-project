package xyz.bx25.cljaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *  bx25 小陈
 *  2026/4/3 18:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    /**
     * 思考
     * @return 思考结果（是否要进行act操作）
     */
    public abstract boolean think();

    /**
     * 执行
     * @return 执行结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            //先思考
            boolean shouldAct = think();
            if (!shouldAct) {
                return "think done，no need to act";
            }
            //再执行
            return act();
        } catch (Exception e) {
            log.error("step failed", e);
            return "step failed"+e.getMessage();
        }
    }
}
