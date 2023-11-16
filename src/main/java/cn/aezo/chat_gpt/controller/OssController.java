package cn.aezo.chat_gpt.controller;
import cn.aezo.chat_gpt.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/oss")
public class OssController {
    @Autowired
    private OssService ossService;
    //上传头像，返回图片的url给
    @PostMapping("/uploadOssFile")
    public String uploadOssFile(MultipartFile file) throws Exception{
        //获取上传文件 MultipartFile
        //返回图片在oss上的路径
        String url = ossService.uploadFileAvatar(file);
        return url;
    }
}