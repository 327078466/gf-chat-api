package cn.aezo.chat_gpt.handler;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /***
     * 处理图片背景色
     * @param path 原图地址
     * @param targetRgb 目标颜色RGB值 16进制颜色码
     * @param isNetWork 原图是否为网络图片地址
     * @return
     */
    public static MultipartFile handleBufferImageBackgroundRGB(String path, String targetRgb) throws Exception {
        // 从URL加载图片
        Mat sourceImage = fetchImageFromUrl(path);
        if (sourceImage.empty()) {
            throw new IllegalArgumentException("Cannot read image from the provided URL.");
        }

        // 创建一个与源图像大小相同的Mat对象，并将所有像素设置为输入的颜色
        String[] colorChannels = targetRgb.split(",");
        int r = Integer.parseInt(colorChannels[0]);
        int g = Integer.parseInt(colorChannels[1]);
        int b = Integer.parseInt(colorChannels[2]);
        Scalar backgroundColor = new Scalar(b, g, r); // 注意OpenCV使用BGR格式
        Mat destinationImage = new Mat(sourceImage.size(), sourceImage.type());
        destinationImage.setTo(backgroundColor);
        // 使用addWeighted方法混合源图像与新的颜色背景
        Core.addWeighted(sourceImage, 1.0, destinationImage, 0.5, 0, destinationImage);

        // 将处理后的图片转为BufferedImage
        byte[] byteArray = new byte[0];
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", destinationImage, matOfByte); // 可以根据你的需要选择不同的格式，例如".png"
        if (matOfByte.total() > 0) {
            byteArray = matOfByte.toArray();
        }
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArray));
        // 将BufferedImage转为MultipartFile
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos); // 可以根据你的需要选择不同的格式，例如"png"
        byte[] bytes = baos.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        MultipartFile multipartFile = new MockMultipartFile("file", "file.jpg", "image/jpeg", inputStream); // 可以将"file.jpg"和"image/jpeg"更改为你需要的文件名和MIME类型

        return multipartFile;
    }
    public static Mat fetchImageFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        // 将BufferedImage转换为Mat对象
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                Color color = new Color(bufferedImage.getRGB(x, y));
                double[] pixel = {color.getRed(), color.getGreen(), color.getBlue()};
                mat.put(y, x, pixel);
            }
        }
        return mat;
    }
    public static BufferedImage matToBufferedImage(Mat matrix) {
        MatOfByte mb = new MatOfByte();
        Imgcodecs.imencode(".bmp", matrix, mb);
        try {
            InputStream in = new ByteArrayInputStream(mb.toArray());
            BufferedImage image = ImageIO.read(in);
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Error converting Mat to BufferedImage", e);
        }
    }


    public static File readNetwork(String pageUrl) {
        try (InputStream in = new URL(pageUrl).openConnection().getInputStream()) {
            File tempFile = FileUtil.createTempFile(null);
            FileUtils.copyInputStreamToFile(in, tempFile);
            return tempFile;
        } catch (IOException e) {

        }
        return null;
    }

    public static MultipartFile fileCase(BufferedImage image) {
        //得到BufferedImage对象
        // BufferedImage bufferedImage = JoinTwoImage.testEncode(200, 200, url);
        MultipartFile multipartFile = null;
        try {
            //创建一个ByteArrayOutputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //把BufferedImage写入ByteArrayOutputStream
            ImageIO.write(image, "jpg", os);
            //ByteArrayOutputStream转成InputStream
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            //InputStream转成MultipartFile
            multipartFile = new MockMultipartFile("file", "file.jpg", "text/plain", input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return multipartFile;

    }

}
