package com.jbhunt.edi.sterlingarchive.utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BlobStorageUtil {
    //02 Test
    private static String connectString = "DefaultEndpointsProtocol=https;AccountName=jsdscdbabloblakesbs02;AccountKey=8sGInYoqei5rP0P2XVoem+W709m3EjlWTak+Y/WIrl+EqYAg+ZDXvgnCXIkSmkmgrhjtF6lB51AP3F4uvuoQng==;EndpointSuffix=core.windows.net";

    public static final String EDI_CONTAINER_NAME = "edi";

    private CloudBlobClient client;
    private boolean initialized = false;

    private void initialize() throws Exception {
        if(!initialized) {
            client = CloudStorageAccount.parse(connectString).createCloudBlobClient();
            initialized = true;
        }
    }

    public String download(String containerName, String reference) throws Exception {
        initialize();
        CloudBlobContainer container = client.getContainerReference(containerName);
        CloudBlockBlob blob = container.getBlockBlobReference(reference);
        return blob.downloadText();
    }

    public void upload(String containerName, String reference, String blobString, String directoryName) throws Exception {
        initialize();
        CloudBlobContainer container = client.getContainerReference(containerName);
        CloudBlobDirectory folder = container.getDirectoryReference(directoryName);
        CloudBlockBlob blob = folder.getBlockBlobReference(reference);
        blob.uploadText(blobString);
    }
}
