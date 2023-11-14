package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.BarcodeType;
import com.mooko.dev.domain.Event;
import com.mooko.dev.repository.BarcodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
     *
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

        deleteTemporaryFilesWithRetry(imageFiles);

        return barcodeFile;
    }

    public void deleteTemporaryFilesWithRetry(List<File> tempFiles) {
        final int maxRetries = 3;
        final long retryDelayMillis = 1000;

        for (File file : tempFiles) {
            int retryCount = 0;

            while (retryCount < maxRetries) {
                try {
                    if (file.delete()) {
                        log.info("파일 삭제 성공: " + file.getAbsolutePath());
                        break;
                    } else {
                        retryCount++;
                        log.warn("파일 삭제 실패, 재시도 중 (" + retryCount + "/" + maxRetries + "): " + file.getAbsolutePath());
                        Thread.sleep(retryDelayMillis);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("파일 삭제 중 인터럽트 발생: " + file.getAbsolutePath(), e);
                    break;
                } catch (SecurityException e) {
                    log.error("파일 삭제 중 보안 예외 발생: " + file.getAbsolutePath(), e);
                    break;
                }
            }

            if (retryCount == maxRetries) {
                log.error("파일 삭제 실패, 최대 재시도 횟수 도달: " + file.getAbsolutePath());
            }
        }
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
