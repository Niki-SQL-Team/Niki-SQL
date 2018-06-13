package BufferManager;

import Foundation.Exception.NKInternalException;

import java.io.*;

public class FileManager<Type extends Serializable> {

    Integer objectSize;
    Integer numberOfObjects;
    String fileIdentifier;
    String fileDirectory;
    String filePath;
    Integer prefixLength = 2 * Integer.SIZE / 8;

    public FileManager(String fileIdentifier, String fileDirectory) {
        this.fileIdentifier = fileIdentifier;
        this.fileDirectory = fileDirectory;
        this.filePath = fileDirectory + fileIdentifier;
        this.objectSize = 0;
        this.numberOfObjects = 0;
    }

    public Integer storeObject(Type object) throws NKInternalException {
        RandomAccessFile randomAccessFile = getFile();
        try {
            assert randomAccessFile != null;
            setFilePointer(randomAccessFile, numberOfObjects);
            ByteArrayOutputStream byteArrayOutputStream = getByteOutputStream(object);
            assert byteArrayOutputStream != null;
            Integer size = writeByteArrayToFile(randomAccessFile, byteArrayOutputStream);
            randomAccessFile.close();
            objectInsertedWith(size);
            return this.numberOfObjects;
        } catch (Exception exception) {
            handleInternalException(exception, "storeObject");
        }
        return -1;
    }

    public boolean isFileExists() {
        File file = new File(this.filePath);
        return file.exists();
    }

    private void setFilePointer(RandomAccessFile randomAccessFile, Integer index) throws IOException {
        assert randomAccessFile != null;
        randomAccessFile.seek(index * this.objectSize + this.prefixLength);
    }

    private ByteArrayOutputStream getByteOutputStream(Type object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        return byteArrayOutputStream;
    }

    private Integer writeByteArrayToFile(RandomAccessFile file, ByteArrayOutputStream outputStream)
            throws IOException {
        byte[] byteArray = outputStream.toByteArray();
        for (byte aByte : byteArray) {
            file.writeByte(aByte);
        }
        return byteArray.length;
    }

    private void objectInsertedWith(Integer size) throws NKInternalException {
        this.numberOfObjects ++;
        if (this.objectSize != 0 && !this.objectSize.equals(size)) {
            throw new NKInternalException("File manager encountered different size objects.");
        }
        this.objectSize = size;
    }

    private RandomAccessFile getFile() {
        File file = new File(this.filePath);
        try {
            file.createNewFile();
            return new RandomAccessFile(file, "rw");
        } catch (Exception exception) {
            handleInternalException(exception, "getFile");
        }
        return null;
    }

    private void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class FileManager.");
        exception.printStackTrace();
    }


}
