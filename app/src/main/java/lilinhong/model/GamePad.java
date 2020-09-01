package lilinhong.model;

public class GamePad {
    public GamePad(int keyCode,int keyMapCode,String desc){
        this.keyCode = keyCode;
        this.keyMapCode = keyMapCode;
        this.desc = desc;
    }
    //key的code
    private int keyCode = -1;
    //key映射值
    private int keyMapCode = -1;
    //描述
    private String desc = "";

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyMapCode() {
        return keyMapCode;
    }

    public void setKeyMapCode(int keyMapCode) {
        this.keyMapCode = keyMapCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
