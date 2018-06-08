package Foundation.Exception;

public class NKInterfaceException extends Exception {

    private String description;

    public NKInterfaceException(String description) {
        this.description = description;
    }

    public void describe() {
        System.out.println(this.description);
    }

}
