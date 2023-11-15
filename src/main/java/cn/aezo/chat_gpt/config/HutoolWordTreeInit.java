package cn.aezo.chat_gpt.config;


import cn.hutool.dfa.WordTree;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Configuration
public class HutoolWordTreeInit {

    @Bean
    public WordTree wordTree(){
        WordTree wordTree = new WordTree();
        voidreadSensitiveWordFile(wordTree);
        return wordTree;
    }

    // 读取敏感词库 ,存入HashMap中
    private void voidreadSensitiveWordFile(WordTree wordTree) {
        //敏感词库
        File file = new File(
                "src/main/resources/censorwords.txt");
        try {
            // 读取文件输入流
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    file), "utf-8");
            // 文件是否是文件 和 是否存在
            if (file.isFile() && file.exists()) {
                // StringBuffer sb = new StringBuffer();
                // BufferedReader是包装类，先把字符读到缓存里，到缓存满了，再读入内存，提高了读的效率。
                BufferedReader br = new BufferedReader(read);
                String txt = null;
                // 读取文件，将文件内容放入到set中
                while ((txt = br.readLine()) != null) {
                    wordTree.addWord(txt);
                }
                br.close();
            }
            // 关闭文件流
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
