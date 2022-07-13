package de.volkswagen.fakultaet.backend.service;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AzureBlobService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobService.class);

    private CloudBlobClient cloudBlobClient;

    public AzureBlobService(CloudBlobClient cloudBlobClient) {
        this.cloudBlobClient = cloudBlobClient;
    }

    public boolean createContainer(String containerName) throws URISyntaxException, StorageException {
        CloudBlobContainer container = this.cloudBlobClient.getContainerReference(containerName);
        return container
                .createIfNotExists(BlobContainerPublicAccessType.CONTAINER,
                        new BlobRequestOptions(),
                        new OperationContext());
    }

    public URI uploadFile(String containerName, MultipartFile file) throws URISyntaxException, StorageException, IOException {
        String blobFileName = String.join(".",
                UUID.randomUUID().toString(),
                FilenameUtils.getExtension(file.getOriginalFilename()));
        CloudBlockBlob cloudBlockBlob = this.cloudBlobClient
                .getContainerReference(containerName)
                .getBlockBlobReference(blobFileName);
        cloudBlockBlob.upload(file.getInputStream(), file.getSize());
        return cloudBlockBlob.getUri();
    }

    public List<URI> getBlobFileUriList(String containerName) throws URISyntaxException, StorageException {
        List<URI> blobFileUriList = new ArrayList<>();
        for (ListBlobItem item : this.cloudBlobClient.getContainerReference(containerName).listBlobs()) {
            blobFileUriList.add(item.getUri());
        }
        return blobFileUriList;
    }

    public void deleteBlobFile(String containerName, String blobFileName) throws URISyntaxException, StorageException {
        this.cloudBlobClient
                .getContainerReference(containerName)
                .getBlockBlobReference(blobFileName)
                .deleteIfExists();
    }
}
