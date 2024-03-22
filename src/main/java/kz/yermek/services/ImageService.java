package kz.yermek.services;

import kz.yermek.models.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    Image saveImage(MultipartFile file);
    Image saveUserImage(MultipartFile file);

}
