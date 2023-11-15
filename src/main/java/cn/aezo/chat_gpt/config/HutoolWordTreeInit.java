package cn.aezo.chat_gpt.config;


import cn.hutool.dfa.WordTree;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.*;

@Configuration
public class HutoolWordTreeInit {

    @Bean
    public WordTree wordTree() {
        WordTree wordTree = new WordTree();
        voidreadSensitiveWordFile(wordTree);
        return wordTree;
    }

    // 读取敏感词库 ,存入HashMap中
    private void voidreadSensitiveWordFile(WordTree wordTree) {
        //敏感词库
        try {
            // 使用ClassLoader加载资源文件
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("censorwords.txt");

//            File file = ResourceUtils.getFile("classpath:censorwords.txt");
            // 读取文件输入流
            InputStreamReader read = new InputStreamReader(inputStream);
            // 文件是否是文件 和 是否存在
            // StringBuffer sb = new StringBuffer();
            // BufferedReader是包装类，先把字符读到缓存里，到缓存满了，再读入内存，提高了读的效率。
            BufferedReader br = new BufferedReader(read);
            String txt = null;
            // 读取文件，将文件内容放入到set中
            while ((txt = br.readLine()) != null) {
                wordTree.addWord(txt);
            }
            br.close();

            // 关闭文件流
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
