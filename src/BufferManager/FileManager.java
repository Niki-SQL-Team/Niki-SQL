package BufferManager;

import Foundation.Exception.NKInternalException;

import java.io.*;

public class FileManager<Type extends Serializable> {

    public void storeObject(Type object, String path) {
        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(path);
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
//            objectOutputStream.writeObject(object);
//            objectOutputStream.close();
//            fileOutputStream.close();
        } catch (Exception exception) {
            handleInternalException(exception, "storeObject");
        }
    }

    @SuppressWarnings("unchecked")
    public Type getObject(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            return (Type)objectInputStream.readObject();
        } catch (Exception exception) {
            handleInternalException(exception, "getObject");
        }
        return null;
    }

    @SuppressWarnings("all")
    public void renameFile(String oldName, String newName) throws NKInternalException {
        File oldFile = new File(oldName);
        File newFile = new File(newName);
        if (!oldFile.exists() || newFile.exists()) {
            throw new NKInternalException("Rename Corruption.");
        }
        oldFile.renameTo(newFile);
    }

    @SuppressWarnings("all")
    public void dropFile(String path) {
        File file = new File(path);
        file.delete();
    }

    public Boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    private void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class FileManager.");
        exception.printStackTrace();
    }

}
