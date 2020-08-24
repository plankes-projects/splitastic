package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.repository.ImageDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class ImageDataDao {

    final int MAX_IMAGE_WIDTH_AND_HEIGHT = 120;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ImageDataRepository imageDataRepository;

    public ImageData getOrCreateImageDataWithUrl(String url) {
        String sha2 = createSha2(url);

        ImageData data;
        try {
            data = getImageDataWithHash(sha2);
        } catch (NoResultException e) {
            try {
                data = new ImageData();
                data.setUrl(url);
                data.setSha2(sha2);
                imageDataRepository.save(data);
            } catch (Exception ex) {
                data = getImageDataWithHash(sha2);
            }
        }
        return data;
    }

    private ImageData getImageDataWithHash(String sha2) {
        return (ImageData) entityManager
                .createNativeQuery("SELECT * FROM image_data where sha2 = :sha2", ImageData.class)
                .setParameter("sha2", sha2)
                .getSingleResult();
    }

    private String createSha2(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return String.format("%064x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageData getImageDataForFile(BufferedImage image) {
        String url = convertImageToBase64Url(image);
        ImageData imageData = this.getOrCreateImageDataWithUrl(url);
        imageData.setUrl(url);
        return imageData;
    }

    public ImageData getImageDataFromUpload(String base64File) {
        String url = processBase64Url(base64File);
        ImageData image = this.getOrCreateImageDataWithUrl(url);
        image.setUrl(url);
        return image;
    }

    private String processBase64Url(String base64Image) {
        try {
            base64Image = base64Image.split(",")[1];
            byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            Dimension newDimension = getNewDimension(img);

            Image tmp = img.getScaledInstance((int) newDimension.getWidth(), (int) newDimension.getHeight(), Image.SCALE_SMOOTH);
            BufferedImage dimg = new BufferedImage((int) newDimension.getWidth(), (int) newDimension.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = dimg.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            return convertImageToBase64Url(dimg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Dimension getNewDimension(BufferedImage img) {
        Dimension d = new Dimension();
        float factor = img.getHeight() > img.getWidth() ? getFactor(img.getHeight()) : getFactor(img.getWidth());
        d.setSize(img.getWidth() * factor, img.getHeight() * factor);
        return d;
    }

    private float getFactor(int size) {
        if (size > MAX_IMAGE_WIDTH_AND_HEIGHT) {
            return (float) MAX_IMAGE_WIDTH_AND_HEIGHT / (float) size;
        } else {
            return (float) size / (float) MAX_IMAGE_WIDTH_AND_HEIGHT;
        }
    }

    private String convertImageToBase64Url(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] fileContent = baos.toByteArray();
            baos.close();
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            return "data:image/png;base64," + encodedString;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
