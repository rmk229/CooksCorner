package kz.yermek.services;

import kz.yermek.models.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    Image saveImage(MultipartFile file);

    void deleteUserImage(Long id);

    boolean isImageFile(MultipartFile file);
}
