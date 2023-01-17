package com.main19.server.myplants.gallery.controller;

import com.main19.server.dto.SingleResponseDto;
import com.main19.server.myplants.entity.MyPlants;
import com.main19.server.myplants.gallery.dto.GalleryDto;
import com.main19.server.myplants.gallery.entity.Gallery;
import com.main19.server.myplants.gallery.mapper.GalleryMapper;
import com.main19.server.myplants.gallery.service.GalleryService;
import com.main19.server.myplants.service.MyPlantsService;
import com.main19.server.s3service.S3StorageService;
import javax.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/myplants")
public class GalleryController {

    private final S3StorageService storageService;
    private final GalleryMapper galleryMapper;
    private final GalleryService galleryService;
    private final MyPlantsService myPlantsService;

    @PostMapping(value = "/{myplants-id}/gallery" , consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity postGallery(@RequestHeader(name = "Authorization") String token, @PathVariable("myplants-id") @Positive long myPlantsId,
        @RequestPart("post") GalleryDto.Post requestBody, @RequestPart MultipartFile galleryImage) {

        String imagePath = storageService.uploadGalleryImage(galleryImage);
        Gallery gallery = galleryMapper.galleryDtoPostToGallery(requestBody);
        MyPlants myPlants = myPlantsService.findMyPlants(myPlantsId);

        Gallery createdGallery = galleryService.createGallery(gallery,myPlants,imagePath,token);

        GalleryDto.Response response = galleryMapper.galleryToGalleryDtoResponse(createdGallery);

        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @GetMapping("/gallery/{gallery-id}")
    public ResponseEntity getGallery(@PathVariable("gallery-id") @Positive long galleryId) {

        Gallery gallery = galleryService.findGallery(galleryId);
        GalleryDto.Response response = galleryMapper.galleryToGalleryDtoResponse(gallery);

        return new ResponseEntity(new SingleResponseDto<>(response),HttpStatus.OK);
    }

    @DeleteMapping(value = "/gallery/{gallery-id}")
    public ResponseEntity deleteGallery(@RequestHeader(name = "Authorization") String token, @PathVariable("gallery-id") @Positive long galleryId) {

        storageService.removeGalleryImage(galleryId,token);
        galleryService.deleteGallery(galleryId,token);

        return ResponseEntity.noContent().build();
    }
}
