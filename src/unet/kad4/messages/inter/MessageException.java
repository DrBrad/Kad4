package unet.kad4.messages.inter;

public class MessageException extends Exception {

    private int code;

    public MessageException(String message){
        super(message);
    }

    public MessageException(String message, Throwable cause){
        super(message, cause);
    }

    public MessageException(String message, int code){
        super(message);
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
