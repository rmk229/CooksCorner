package kz.yermek.services.impl;

import jakarta.transaction.Transactional;
import kz.yermek.models.Image;
import kz.yermek.repositories.ImageRepository;
import kz.yermek.services.CloudinaryService;
import kz.yermek.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;


    @Override
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @Override
    @Transactional
    public Image saveImage(MultipartFile file) {
        Image image = new Image();
        try {
            image.setUrl(cloudinaryService.uploadFile(file, "Recipes picture"));
            imageRepository.save(image);
        }catch (Exception exception){
            throw new RuntimeException("Image upload failed: " + exception.getMessage());
        }

        return image;

    }

    @Override
    public void deleteUserImage(Long id) {
        imageRepository.deleteById(id);
    }
}
