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
import java.util.stream.IntStream;

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

    public BufferedImage combineImagesHorizontally(List<String> imageURLs, int totalWidth, int totalHeight) throws IOException {
        int imageCount = imageURLs.size();
        double exactImageWidth = (double) totalWidth / imageCount;
        int baseImageWidth = (int) exactImageWidth;
        // 남은 폭을 각 이미지에 분배하기 위한 오프셋을 계산합니다.
        int extraWidth = totalWidth - baseImageWidth * imageCount;

        // 병렬 처리를 위한 스레드 풀을 생성합니다.
        ForkJoinPool pool = new ForkJoinPool();
        try {
            // 모든 이미지를 병렬로 리사이징합니다.
            List<BufferedImage> resizedImages = pool.submit(() ->
                    IntStream.range(0, imageCount).parallel().mapToObj(i -> {
                        try {
                            URL imageUrl = new URL(imageURLs.get(i));
                            BufferedImage original = ImageIO.read(imageUrl);
                            // 각 이미지에 추가 폭을 할당합니다.
                            int width = baseImageWidth + ((i < extraWidth) ? 1 : 0);
                            return resizeImage(original, width, totalHeight);
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
                if (image != null) {
                    g.drawImage(image, x, 0, null);
                    x += image.getWidth();
                }
            }
            g.dispose();

            return combined;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("An error occurred while combining images.", e);
        } finally {
            pool.shutdown();
        }
    }

    // 이미지 리사이징 메소드
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }



    //바코드를 만들때는 이 함수를 사용하기

    /**
     *
     * @param imageFiles    바코드를 만들 이미지 파일들
     *
     *
     */
    public File makeNewBarcode(List<String> imageURLs) throws IOException, InterruptedException {
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
        return barcodeRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.BARCODE_NOT_FOUND));
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
