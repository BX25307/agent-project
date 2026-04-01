package xyz.bx25.cljaiagent.memory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author bx25 小陈
 * @Date 2026/3/30 17:01
 */
public class FileBasedChatMemory implements ChatMemory {
    private final String Base_DIR;
    private static final Kryo kryo=new Kryo();
    static {
        //不需要手动注册--》动态注册
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String baseDir) {
        this.Base_DIR = baseDir;
        File dir = new File(baseDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId,List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId,messageList);
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 获取或创建会话消息列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId){
        File file = getConversationFile(conversationId);
        List<Message> messages=new ArrayList<>();
        if(file.exists()){
            try(Input input=new Input(new FileInputStream(file))){
                messages=kryo.readObject(input,ArrayList.class);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存会话消息
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 每个会话文件单独保存
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId){
        return new File(Base_DIR,conversationId+".kryo");
    }
}
