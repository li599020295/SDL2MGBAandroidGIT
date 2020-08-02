package lilinhong.model;

public class GameRom {
    private String name = "";
    private String desc = "";
    private String image = "";
    private String path = "";
    private String md5 = "";
    private long preUseTime = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getPreUseTime() {
        return preUseTime;
    }

    public void setPreUseTime(long preUseTime) {
        this.preUseTime = preUseTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
