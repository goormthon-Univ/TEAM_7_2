package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.BarcodeType;
import com.mooko.dev.domain.Event;
import com.mooko.dev.repository.BarcodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarcodeService {

    //바코드 사이즈 상수화
    private final int NEW_WIDTH = 340;
    private final int NEW_HEIGHT = 160;

    private final BarcodeRepository barcodeRepository;

    public BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth, newHeight;

        if (originalHeight > targetHeight) {
            newHeight = targetHeight;
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            newWidth = targetWidth;
            newHeight = (int) (newWidth / aspectRatio);
        }

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }


    public BufferedImage combineImagesHorizontally(List<File> imageFiles, int totalWidth, int totalHeight) throws IOException {
        int singleImageWidth = totalWidth / imageFiles.size();
        List<BufferedImage> resizedImages = new ArrayList<>();

        for (File imageFile : imageFiles) {
            BufferedImage original = ImageIO.read(imageFile);
            BufferedImage resized = resizeImage(original, singleImageWidth, totalHeight);
            resizedImages.add(resized);
        }

        BufferedImage combined = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combined.createGraphics();

        int x = 0;
        for (BufferedImage image : resizedImages) {
            g.drawImage(image, x, 0, null);
            x += singleImageWidth;
        }
        g.dispose();

        return combined;
    }



    //바코드를 만들때는 이 함수를 사용하기

    /**
     *
     * @param imageFiles    바코드를 만들 이미지 파일들
     * @param outputPath    바코드가 최종 생성돼서 저장되어야 하는 경로
     *
     *                      outputPath는 aggregationFacade에서 가져오기
     *
     */
    public File makeNewBarcode(List<String> imageURLs) throws IOException {
        List<File> imageFiles = new ArrayList<>();

        for (String imageUrl : imageURLs) {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            File tempFile = File.createTempFile("image", ".jpg");
            ImageIO.write(image, "jpg", tempFile);
            imageFiles.add(tempFile);
        }

        BufferedImage combined = combineImagesHorizontally(imageFiles, NEW_WIDTH, NEW_HEIGHT);
        File barcodeFile = File.createTempFile("barcode", ".jpg");
        ImageIO.write(combined, "jpg", barcodeFile);

        // 임시 파일들 삭제
        for (File tempFile : imageFiles) {
            tempFile.delete();
        }

        return barcodeFile;
    }


    @Transactional
    public Barcode saveBarcode(String barcodeFilePath, String title, String startDate, String endDate, BarcodeType barcodeType, Event event) {
         Barcode barcode = Barcode.builder()
                 .barcodeUrl(barcodeFilePath)
                 .title(title)
                 .startDate(startDate)
                 .endDate(endDate)
                 .type(barcodeType)
                 .event(event)
                 .createdAt(LocalDateTime.now())
                 .build();

        return barcodeRepository.save(barcode);
    }
}
