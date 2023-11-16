package cn.aezo.chat_gpt.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String uploadFileAvatar(MultipartFile file)  throws Exception;
}
