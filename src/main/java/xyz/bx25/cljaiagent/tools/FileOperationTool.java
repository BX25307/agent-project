package xyz.bx25.cljaiagent.tools;

import cn.hutool.core.io.FileUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import xyz.bx25.cljaiagent.constant.FileConstant;

/**
 * 文件操作工具类(提供文件读写功能)
 * @Author bx25 小陈
 * @Date 2026/4/2 10:55
 */

public class FileOperationTool {

    private final String FILE_DIR= FileConstant.FILE_SAVE_DIR+"/file";
    @Tool(description = "read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try{
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "Error reading file"+e.getMessage();
        }
    }

    @Tool(description = "write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to read") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        FileUtil.mkdir(FILE_DIR);
        try{
         FileUtil.writeUtf8String(content, filePath);
         return "File written successfully"+filePath;
        }catch (Exception e){
            return  "Error writing file"+e.getMessage();
        }
    }
}
