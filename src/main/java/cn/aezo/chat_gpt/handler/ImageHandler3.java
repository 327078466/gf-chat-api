package cn.aezo.chat_gpt.handler;

import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import static org.opencv.core.CvType.CV_8UC3;

public class ImageHandler3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static class InputParams {
        int thresh = 10;
        int transparency = 255;
        int size = 7;
        Point p = new Point(10, 10);
        Scalar color = new Scalar(255, 255, 255);
    }

    public static Mat fetchImageFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            // 将BufferedImage转换为Mat对象
            Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CV_8UC3); // 注意这里是CV_8UC3而不是CV_8UC1
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    Color color = new Color(bufferedImage.getRGB(x, y));
                    double[] pixel = {color.getBlue(),color.getGreen(),color.getRed()};
                    mat.put(y, x, pixel);
                }
            }
            return mat;
        }
    }


    public static MultipartFile handleBufferImageBackgroundRGB(String pathUrl, String targetRgb) throws Exception {
        Mat src = fetchImageFromUrl(pathUrl);
        InputParams input = new InputParams();
        input.thresh = 30;
        input.transparency = 255;
        input.size = 6;
        // 创建一个与源图像大小相同的Mat对象，并将所有像素设置为输入的颜色
        String[] colorChannels = targetRgb.split(",");
        int r = Integer.parseInt(colorChannels[0].trim());
        int g = Integer.parseInt(colorChannels[1].trim());
        int b = Integer.parseInt(colorChannels[2].trim());
        input.color = new Scalar(b, g, r); // 注意OpenCV使用BGR格式

        long startTime = System.currentTimeMillis();
        Mat result = backgroundSeparation(src, input);
        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        System.out.println("Time: " + elapsedTime + " seconds");
        MultipartFile multipartFile = convertToMultipartFile(result,"result.jpg");
        return multipartFile;
    }

    public static MultipartFile convertToMultipartFile(Mat mat,String fileName) {
        // 将 Mat 转换为字节数组
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        // 构造 MultipartFile 对象
        MultipartFile multipartFile = null;
        byte[] byteArray = matOfByte.toArray();
        multipartFile = new MockMultipartFile(fileName, fileName, "image/jpeg", byteArray);
        return multipartFile;
    }

    public static Mat backgroundSeparation(Mat src, InputParams input) {
        Mat bgra = new Mat();
        Mat mask = new Mat();
        // 创建一个4通道背板 带透明度属性
        Imgproc.cvtColor(src, bgra, Imgproc.COLOR_BGR2BGRA);
        mask = Mat.zeros(bgra.size(), CvType.CV_8UC1); // 蒙版是单通道
        saveFileByDirectory(convertToMultipartFile(mask,"mask.jpg"));
        int row = src.rows();
        int col = src.cols();
        // Exceptional value correction
        input.p.x = Math.max(0, Math.min(col, input.p.x));
        input.p.y = Math.max(0, Math.min(row, input.p.y));
        input.thresh = Math.max(5, Math.min(200, input.thresh));
        input.transparency = Math.max(0, Math.min(255, input.transparency));
        input.size = Math.max(0, Math.min(30, input.size));
        // 确认当前背景颜色
        Scalar refColorScalar = Core.mean(src.row((int) input.p.y).col((int) input.p.x));
        double[] refColor = refColorScalar.val;
        double ref_b = refColor[0];
        double ref_g = refColor[1];
        double ref_r = refColor[2];

        // 计算蒙版区域
        for (int i = 0; i < row; ++i) { // 行
            for (int j = 0; j < col ; ++j) { // 列
                double[] m = mask.get(i, j); // 单通道像素点颜色
                double[] b = src.get(i, j); // 原图像素点颜色
                if ((getDiff(b[0], b[1], b[2], ref_b, ref_g, ref_r)) > input.thresh) {
                    m[0] = 255;
                    mask.put(i,j,m);
                }
            }
        }
        // 第一次处理 获得蒙版
        saveFileByDirectory(convertToMultipartFile(mask,"mask2.jpg"));
        // 创建一个大小为 (row + 50, col + 50) 的空白图像
        Mat tmask = Mat.zeros(mask.rows() + 50, mask.cols() + 50, CvType.CV_8UC1);
        // 截取 mask 的子区域，然后进行复制
        mask.copyTo(new Mat(tmask, new Range(25, 25 + mask.rows()), new Range(25, 25 + mask.cols())));
        saveFileByDirectory(convertToMultipartFile(tmask,"tmask.jpg"));
        // 寻找轮廓，作用是填充轮廓内黑洞
        List<MatOfPoint> contour = new ArrayList<>();
        MatOfInt4  hierarchy = new MatOfInt4();
        Imgproc.findContours(tmask, contour, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.drawContours(tmask, contour, -1, new Scalar(255), Core.FILLED, 16);

        // 黑帽运算获取同背景色类似的区域，识别后填充
        Mat hat = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(31, 31));
        Imgproc.morphologyEx(tmask, hat, Imgproc.MORPH_BLACKHAT, element);
        // 将 hat 中大于 0 的像素设置为 255
        Core.compare(hat, new Scalar(0), hat, Core.CMP_GT);
        hat.setTo(new Scalar(255), hat);
        saveFileByDirectory(convertToMultipartFile(hat,"hat.jpg"));
        Mat hatd = new Mat();
        hatd =  clearMicroConnectedAreas(hat, hatd, 450);
        saveFileByDirectory(convertToMultipartFile(mask,"hatd.jpg"));
        // 将 tmask 和 hatd 相加
        Core.add(tmask, hatd, tmask);
        // 截取 tmask 的子区域，然后进行克隆
         mask = new Mat(tmask, new Range(25, 25 + src.rows()), new Range(25, 25 + src.cols())).clone();

        // 掩膜滤波，是为了边缘虚化
        Imgproc.blur(mask, mask, new Size(2 * input.size + 1, 2 * input.size + 1));
        saveFileByDirectory(convertToMultipartFile(mask,"mask3.jpg"));
        // Change color
        for (int i = 0; i < row; ++i) { // 行
            for (int j = 0; j < col; ++j) { // 列
                double[] r = bgra.get(i, j);
                double[] m = mask.get(i, j);
                if (m[0] == 0.0) {
                    r[0] = input.color.val[0];
                    r[1] = input.color.val[1];
                    r[2] = input.color.val[2];
                    r[3] = input.transparency;
                    bgra.put(i,j,r);
                }
                else if (m[0] != 255.0) {
                    int newb = (int) ((r[0] * m[0] * 0.3 + input.color.val[0] * (255 - m[0]) * 0.7) / ((255 - m[0]) * 0.7 + m[0] * 0.3));
                    int newg = (int) ((r[1] * m[0] * 0.3 + input.color.val[1] * (255 - m[0]) * 0.7) / ((255 - m[0]) * 0.7 + m[0] * 0.3));
                    int newr = (int) ((r[2] * m[0] * 0.3 + input.color.val[2] * (255 - m[0]) * 0.7) / ((255 - m[0]) * 0.7 + m[0] * 0.3));
                    int newt = (int) ((r[3] * m[0] * 0.3 + input.transparency * (255 - m[0]) * 0.7) / ((255 - m[0]) * 0.7 + m[0] * 0.3));
                    newb = Math.max(0, Math.min(255, newb));
                    newg = Math.max(0, Math.min(255, newg));
                    newr = Math.max(0, Math.min(255, newr));
                    newt = Math.max(0, Math.min(255, newt));
                    r[0] = newb;
                    r[1] = newg;
                    r[2] = newr;
                    r[3] = newt;
                    bgra.put(i,j,r);
                }else {
                    bgra.put(i,j,r);
                }
            }
        }
        saveFileByDirectory(convertToMultipartFile(bgra,"bgra.jpg"));
        return bgra;
    }

    public static Mat clearMicroConnectedAreas(Mat src, Mat dst, double minArea) {
        dst = src.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        MatOfInt4  hierarchy = new MatOfInt4 ();
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        if (!contours.isEmpty() && !hierarchy.empty()) {
            for (MatOfPoint contour : contours) {
                Rect rect = Imgproc.boundingRect(contour);
                double area = Imgproc.contourArea(contour);
                if (area < minArea) {
                    for (int i = rect.y; i < rect.y + rect.height; i++) { // 行
                        for (int j = rect.x; j < rect.x + rect.width; j++) { // 列
                            double[] outputData = dst.get(i, j);
                            if (outputData[0] == 255) {
                                outputData[0] = 0;
                                dst.put(i,j,outputData);
                            }
                        }
                    }
                }
            }
        }
        return dst;
    }

    public static double getDiff(double b1, double g1, double r1, double b2, double g2, double r2) {
        return Math.abs(b1 - b2) + Math.abs(g1 - g2) + Math.abs(r1 - r2);
    }

    public static void saveFileByDirectory(MultipartFile file) {
        try {
            // 将文件保存在服务器目录中
            // 文件名称
//            String uuid = UUID.randomUUID().toString();
            // 得到上传文件后缀
            String originalName = file.getOriginalFilename();
//            String ext = "." + FilenameUtils.getExtension(originalName);
            // 新生成的文件名称
//            String fileName = uuid + ext;
            // 复制文件
            File targetFile = new File("D:\\chenbingfeng\\source\\goldfish_chatgpt\\image", originalName);
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
        } catch (IOException e) {

        }
    }

}
