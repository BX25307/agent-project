//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package xyz.bx25.cljaiagent.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

public final class FileBasedChatMemoryRepository implements ChatMemoryRepository {

    private final String Base_DIR;
    private static final Kryo kryo=new Kryo();
    static {
        //不需要手动注册--》动态注册
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemoryRepository(String baseDir) {
        this.Base_DIR = baseDir;
        File file = new File(baseDir);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    @Override
    public List<String> findConversationIds() {
        File directory = new File(Base_DIR);
        // 目录不存在，返回空列表
        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }

        List<String> conversationIds = new ArrayList<>();
        // 列出所有以 .kryo 结尾的文件
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".kryo"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // 去掉 .kryo 后缀，得到 conversationId
                String conversationId = fileName.substring(0, fileName.lastIndexOf(".kryo"));
                conversationIds.add(conversationId);
            }
        }

        return conversationIds;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return getOrCreateConversation(conversationId);

    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId,messageList);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
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
