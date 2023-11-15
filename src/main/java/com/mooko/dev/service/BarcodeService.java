package com.mooko.dev.service;

import com.mooko.dev.domain.Barcode;
import com.mooko.dev.domain.BarcodeType;
import com.mooko.dev.domain.Event;
import com.mooko.dev.repository.BarcodeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeService {

    //바코드 사이즈 상수화
    private final int NEW_WIDTH = 340;
    private final int NEW_HEIGHT = 160;

    private final BarcodeRepository barcodeRepository;

    public BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }



    public BufferedImage combineImagesHorizontally(List<String> imageURLs, int totalWidth, int totalHeight) throws IOException {
        int singleImageWidth = totalWidth / imageURLs.size();
        List<BufferedImage> resizedImages = new ArrayList<>();

        for (String imageURL : imageURLs) {
            BufferedImage original = ImageIO.read(new URL(imageURL));
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
        log.info("imageURLs size = {}", imageURLs.size());

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
        Optional<Barcode> barcode = barcodeRepository.findById(id);
        return barcode.get();
    }

    public Barcode findBarcodeByTitle(String title){
        Optional<Barcode> barcode = barcodeRepository.findByTitle(title);
        if (barcode.isPresent()){
            return barcode.get();
        } else {
            return null;
        }
    }

    public void deleteBarcode(Barcode barcode){
        barcodeRepository.delete(barcode);
    }
}
