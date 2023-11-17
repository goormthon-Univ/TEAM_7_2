package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.BarcodeType;
import com.mooko.dev.domain.DayPhoto;
import com.mooko.dev.domain.Event;
import com.mooko.dev.domain.User;
import com.mooko.dev.domain.UserBarcode;
import com.mooko.dev.exception.custom.CustomException;
import com.mooko.dev.repository.BarcodeRepository;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mooko.dev.exception.custom.ErrorCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeService {

    //바코드 사이즈 상수화
    private final int NEW_WIDTH = 340;
    private final int NEW_HEIGHT = 160;
    private final int MIN_PHOTO_COUNT_FOR_BARCODE = 30;
    private final int MAX_PHOTO_COUNT_FOR_BARCODE = 130;

    private final BarcodeRepository barcodeRepository;

    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        AffineTransform transform = new AffineTransform();
        transform.scale((double) targetWidth / originalImage.getWidth(),
                (double) targetHeight / originalImage.getHeight());

        AffineTransformOp scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(originalImage, null);
    }



    public BufferedImage combineImagesHorizontally(List<String> imageURLs, int totalWidth, int totalHeight) throws IOException {
        int singleImageWidth = totalWidth / imageURLs.size();
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        try {
            List<BufferedImage> resizedImages = customThreadPool.submit(
                    () -> imageURLs.parallelStream().map(imageURL -> {
                        try {
                            BufferedImage original = ImageIO.read(new URL(imageURL));
                            return resizeImage(original, singleImageWidth, totalHeight);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).collect(Collectors.toList())
            ).get();

            BufferedImage combined = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = combined.createGraphics();

            int x = 0;
            for (BufferedImage image : resizedImages) {
                g.drawImage(image, x, 0, null);
                x += singleImageWidth;
            }
            g.dispose();

            return combined;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            customThreadPool.shutdown();
            try {
                customThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                e.printStackTrace();
            }
        }
    }




    //바코드를 만들때는 이 함수를 사용하기

    /**
     *
     * @param imageFiles    바코드를 만들 이미지 파일들
     *
     *
     */
    public File makeNewBarcode(List<String> imageURLs) throws IOException {
        log.info("imageURLs size = {}", imageURLs.size());

        // 바코드 생성 테스트할때는 주석으로
//        if (imageURLs.size()<MIN_PHOTO_COUNT_FOR_BARCODE){
//            throw new CustomException(ErrorCode.PHOTO_FOR_BARCODE_LESS_THEN);
//        }else if(imageURLs.size()>MAX_PHOTO_COUNT_FOR_BARCODE){
//            throw new CustomException(ErrorCode.PHOTO_FOR_BARCODE_EXCEED);
//        }
        //


        BufferedImage combined = combineImagesHorizontally(imageURLs, NEW_WIDTH, NEW_HEIGHT);
        File barcodeFile = File.createTempFile("barcode", ".jpg");
        ImageIO.write(combined, "jpg", barcodeFile);


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

    public Barcode findBarcode(Long id){
        Barcode barcode = barcodeRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.BARCODE_NOT_FOUND));
        return barcode;
    }

    public Barcode findBarcodeByTitle(List<UserBarcode> userBarcodeList, String title) {
        return userBarcodeList.stream()
                .map(UserBarcode::getBarcode)
                .filter(barcode -> title.equals(barcode.getTitle()))
                .findFirst()
                .orElse(null);
    }

    public void deleteBarcode(Barcode barcode){
        barcodeRepository.delete(barcode);
    }
}
