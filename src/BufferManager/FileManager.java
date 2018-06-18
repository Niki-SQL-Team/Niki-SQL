package BufferManager;

import Foundation.Exception.NKInternalException;

import java.io.*;

public class FileManager<Type extends Serializable> {

    public void storeObject(Type object, String identifier) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(identifier);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception exception) {
            handleInternalException(exception, "storeObject");
        }
    }

    @SuppressWarnings("unchecked")
    public Type getObject(String identifier) {
        try {
            FileInputStream fileInputStream = new FileInputStream(identifier);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            return (Type)objectInputStream.readObject();
        } catch (Exception exception) {
            handleInternalException(exception, "getObject");
        }
        return null;
    }

    public void renameFile(String oldName, String newName) throws NKInternalException {
        File oldFile = new File(oldName);
        File newFile = new File(newName);
        if (!oldFile.exists() || newFile.exists()) {
            throw new NKInternalException("Rename Corruption.");
        }
        oldFile.renameTo(newFile);
    }

    public void dropFile(String identifier) {
        File file = new File(identifier);
        file.delete();
    }

    private void handleInternalException(Exception exception, String methodName) {
        System.out.println("Error in " + methodName + " method, class FileManager.");
        exception.printStackTrace();
    }

}
