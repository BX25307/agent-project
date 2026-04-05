package xyz.bx25.cljaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * bx25 小陈
 * 2026/4/3 21:20
 */
@SpringBootTest
class EternityManusTest {
   @Resource
   private EternityManus manus;


   @Test
   void run() {
      String userPrompt = """
                我的另一半居住在上海静安区,请帮我找到 5 公里内合适的约会地点,
                并结合一些网络图片,制定一份详细的约会计划,
                并以 PDF 格式输出,
                要求用中文,
                同时图片也要显示出来""";
      String chatId = UUID.randomUUID().toString();
      String answer = manus.run(userPrompt);
      Assertions.assertNotNull(answer);
   }
}