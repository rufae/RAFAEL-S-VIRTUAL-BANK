package Banco.BBDD;

public class DataControl {

    public boolean datacontrol(String data, String control){
        boolean verification = data.matches(control);
        return verification;
    }
}
