package cn.aezo.chat_gpt.handler;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * @Version 1.0
 * @Classname ImageHandler
 * @Description
 * @Date 2023/11/17
 * @Created by 陈冰峰
 */
public class ImageHandler {
    // 比较邪乎了，为啥是30，不是20，其实20也可以，就是一个优化参数
    private static final int critical = 30;


    /***
     * 处理图片背景色
     * @param path 原图地址
     * @param targetRgb 目标颜色RGB值 16进制颜色码
     * @param isNetWork 原图是否为网络图片地址
     * @return
     */
    public static MultipartFile handleBufferImageBackgroundRGB(String path, int targetRgb, boolean isNetWork) throws Exception {
        File file;
        if (isNetWork) {
            // 处理网络图片，先将图片下载到本地（上传的头像）
            file = readNetwork(path);
        } else {
            file = new File(path);
        }
        /**
         * 用来处理图片的缓冲流
         */
        BufferedImage bi = null;
        try {
            /**
             * 用ImageIO将图片读入到缓冲中
             */
            bi = ImageIO.read(file);
        } catch (Exception e) {

        }

        /**
         * 得到图片的长宽
         */
        int width = bi.getWidth();
        int height = bi.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        /**
         * 获取左上角颜色，默认左上角像素块颜色为背景色
         */
        int pixel = bi.getRGB(critical, critical);
        /**
         * 这里是遍历图片的像素，因为要处理图片的背色，所以要把指定像素上的颜色换成目标颜色
         * 这里 是一个二层循环，遍历长和宽上的每个像素
         */
        Graphics g = image.getGraphics();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                /**
                 * 得到指定像素（i,j)上的RGB值，
                 */
                int nowPixel = bi.getRGB(x, y);
                /**
                 * 进行换色操作，我这里是要把蓝底换成白底，那么就判断图片中rgb值是否在蓝色范围的像素
                 */
                // 核心代码：但是这样会有误差，还需要优化边缘、人像边框
                int p = pixel == nowPixel ? targetRgb : nowPixel;
                g.setColor(new Color(p));
                g.fillRect(x, y, 1, 1);
            }
        }
        return fileCase(image);
    }

    public static File readNetwork(String pageUrl){
        try (InputStream in = new URL(pageUrl).openConnection().getInputStream()){
            File tempFile = FileUtil.createTempFile(null);
            FileUtils.copyInputStreamToFile(in, tempFile);

            return tempFile;
        }catch (IOException e){

        }
        return null;
    }
    public static MultipartFile fileCase(BufferedImage image){
        //得到BufferedImage对象
        // BufferedImage bufferedImage = JoinTwoImage.testEncode(200, 200, url);
        MultipartFile multipartFile= null;
        try {
            //创建一个ByteArrayOutputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //把BufferedImage写入ByteArrayOutputStream
            ImageIO.write(image, "jpg", os);
            //ByteArrayOutputStream转成InputStream
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            //InputStream转成MultipartFile
            multipartFile =new MockMultipartFile("file", "file.jpg", "text/plain", input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return multipartFile;

    }

}
