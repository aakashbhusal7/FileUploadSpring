package com.example.filedemo.service;

import com.example.filedemo.exceptions.FileStorageException;
import com.example.filedemo.exceptions.MyFileNotFoundException;
import com.example.filedemo.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new FileStorageException("could not create directory", e);
        }
    }

    public String storeFile(MultipartFile file){
        String fileName= StringUtils.cleanPath(file.getOriginalFilename());
        try{
            if(fileName.contains("..")){
                throw new FileStorageException("Sorry, filename contains invalid character"+fileName);
            }
            Path targetLocation=this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        }catch (IOException e){
            throw new FileStorageException("Could not store "+fileName + ". Please try again! ",e);
        }
    }

    public Resource loadFileAsResources(String fileName){
        try{
            Path filePath=this.fileStorageLocation.resolve(fileName).normalize();
            org.springframework.core.io.Resource resource=new UrlResource(filePath.toUri());
            if(resource.exists()){
                return resource;
            }else{
                throw new MyFileNotFoundException("File not found "+fileName);
            }
        }catch (MalformedURLException e){
            throw new MyFileNotFoundException("File not found "+fileName,e);
        }
    }
}

