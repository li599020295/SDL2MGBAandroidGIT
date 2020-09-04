package lilinhong.model;

public class CheatData {
    private String name;
    private String code;
    private boolean enable;
    public CheatData(String name,String code){
        this.name = name;
        this.code = code;
        this.enable = true;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
