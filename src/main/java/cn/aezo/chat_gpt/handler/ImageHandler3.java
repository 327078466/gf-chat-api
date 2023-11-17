package cn.aezo.chat_gpt.handler;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Inputparama {
    int thresh = 30;
    int transparency = 255;
    int size = 7;
    Point p = new Point(0, 0);
    Scalar color = new Scalar(255, 255, 255);
}

public class ImageHandler3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat fetchImageFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        // 将BufferedImage转换为Mat对象
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC1);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                Color color = new Color(bufferedImage.getRGB(x, y));
                double[] pixel = {color.getRed(), color.getGreen(), color.getBlue()};
                mat.put(y, x, pixel);
            }
        }
        return mat;
    }

    public static MultipartFile handleBufferImageBackgroundRGB(String path, String targetRgb) throws Exception {
        Mat src = fetchImageFromUrl(path);
        // 创建一个与源图像大小相同的Mat对象，并将所有像素设置为输入的颜色
        String[] colorChannels = targetRgb.split(",");
        int r = Integer.parseInt(colorChannels[0].trim());
        int g = Integer.parseInt(colorChannels[1].trim());
        int b = Integer.parseInt(colorChannels[2].trim());
        Scalar backgroundColor = new Scalar(b, g, r); // 注意OpenCV使用BGR格式

        Inputparama input = new Inputparama();
        input.thresh = 100;
        input.transparency = 255;
        input.size = 6;
        input.color = backgroundColor;
        long s = System.currentTimeMillis();
        Mat result = backgroundSeparation(src, input);
        long e = System.currentTimeMillis();
        double dif = e - s;
        System.out.println("time:" + dif);

        // 将处理后的图片转为BufferedImage
        byte[] byteArray = new byte[0];
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", result, matOfByte); // 可以根据你的需要选择不同的格式，例如".png"
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

    public static Mat backgroundSeparation(Mat src, Inputparama input) {
        Mat bgra = new Mat();
        Mat mask = new Mat();
        Imgproc.cvtColor(src, bgra, Imgproc.COLOR_BGR2BGRA);
        mask = Mat.zeros(bgra.size(), CvType.CV_8UC1);
        int row = src.rows();
        int col = src.cols();

        System.out.println("Mask type: " + mask.type());
        System.out.println("Src type: " + src.type());

        input.p.x = Math.max(0, Math.min(col, (int) input.p.x));
        input.p.y = Math.max(0, Math.min(row, (int) input.p.y));
        input.thresh = Math.max(5, Math.min(200, input.thresh));
        input.transparency = Math.max(0, Math.min(255, input.transparency));
        input.size = Math.max(0, Math.min(30, input.size));

        byte[] ref_b = new byte[3];
        src.get((int) input.p.y, (int) input.p.x, ref_b);
        byte ref_g = ref_b[1];
        byte ref_r = ref_b[2];

        for (int i = 0; i < row; ++i) {
            byte[] m_data = new byte[col];
            mask.get(i, 0, m_data);
            byte[] b_data = new byte[col * 3];
            src.get(i, 0, b_data);
            for (int j = 0; j < col; ++j) {
                double diff = getDiff(b_data[j * 3], b_data[j * 3 + 1], b_data[j * 3 + 2], ref_b[0], ref_g, ref_r);
                if (diff > input.thresh) {
                    m_data[j] = (byte) 255;
                }
            }
            mask.put(i, 0, m_data);
        }


        Mat tmask = new Mat(row + 50, col + 50, CvType.CV_8UC1);
        Mat submat = tmask.submat(new Rect(25, 25, col, row));
        mask.copyTo(submat);

        List<MatOfPoint> contour = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(tmask, contour, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.drawContours(tmask, contour, -1, new Scalar(255), Core.FILLED, 16);

        Mat hat = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(31, 31));
        Imgproc.morphologyEx(tmask, hat, Imgproc.MORPH_BLACKHAT, element);
        Core.compare(hat, new Scalar(0), hat, Core.CMP_GT);
        hat.setTo(new Scalar(255));
        Mat hatd = new Mat();
        hatd = clearMicroConnectedAreas(hat, 450);
        // 调整hatd的尺寸与tmask相同
        Imgproc.resize(hatd, hatd, tmask.size());
// 进行加法操作
        // 进行加法操作
        Mat resizedHatd = new Mat();
        Core.add(tmask, hatd, resizedHatd);
        tmask = resizedHatd.clone(); // 将结果赋值给tmask
        Mat submat2 = tmask.submat(new Rect(25, 25, col, row));
        mask = submat2.clone();
        Imgproc.blur(mask, mask, new Size(2 * input.size + 1, 2 * input.size + 1));
        for (int i = 0; i < row; ++i) {
            byte[] r = new byte[col * 4];
            bgra.get(i, 0, r);
            byte[] m = new byte[col];
            mask.get(i, 0, m);
            for (int j = 0; j < col; ++j) {
                if (m[j] == 0) {
                    r[j * 4] = (byte) input.color.val[0];
                    r[j * 4 + 1] = (byte) input.color.val[1];
                    r[j * 4 + 2] = (byte) input.color.val[2];
                    r[j * 4 + 3] = (byte) input.transparency;
                } else if (m[j] != 255) {
                    int newb = (int) ((r[j * 4] * m[j] * 0.3 + input.color.val[0] * (255 - m[j]) * 0.7) / ((255 - m[j]) * 0.7 + m[j] * 0.3));
                    int newg = (int) ((r[j * 4 + 1] * m[j] * 0.3 + input.color.val[1] * (255 - m[j]) * 0.7) / ((255 - m[j]) * 0.7 + m[j] * 0.3));
                    int newr = (int) ((r[j * 4 + 2] * m[j] * 0.3 + input.color.val[2] * (255 - m[j]) * 0.7) / ((255 - m[j]) * 0.7 + m[j] * 0.3));
                    int newt = (int) ((r[j * 4 + 3] * m[j] * 0.3 + input.transparency * (255 - m[j]) * 0.7) / ((255 - m[j]) * 0.7 + m[j] * 0.3));
                    newb = Math.max(0, Math.min(255, newb));
                    newg = Math.max(0, Math.min(255, newg));
                    newr = Math.max(0, Math.min(255, newr));
                    newt = Math.max(0, Math.min(255, newt));
                    r[j * 4] = (byte) newb;
                    r[j * 4 + 1] = (byte) newg;
                    r[j * 4 + 2] = (byte) newr;
                    r[j * 4 + 3] = (byte) newt;
                }
            }
        }

        return bgra;
    }

    public static Mat clearMicroConnectedAreas(Mat src, double min_area) {
        Mat dst = new Mat();
        src.copyTo(dst); // 使用copyTo()方法创建dst的副本

        // 进行图像处理操作，不影响原始图像的尺寸
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        if (!contours.isEmpty()) {
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area < min_area) {
                    Imgproc.drawContours(dst, Arrays.asList(contour), -1, new Scalar(0), Core.FILLED);
                }
            }
        }
        return dst;
    }


    public static double getDiff(double b, double g, double r, double ref_b, double ref_g, double ref_r) {
        double diff = Math.pow((b - ref_b), 2) + Math.pow((g - ref_g), 2) + Math.pow((r - ref_r), 2);
        return Math.sqrt(diff);
    }
}

