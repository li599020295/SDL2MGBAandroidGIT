package lilinhong.model;

public class GameRom {
    private String name = "";
    private String desc = "";
    private String image = "";
    private String path = "";
    private String md5 = "";
    //游戏封面
    private String cover = "";
    //上次使用时间
    private long preUseTime = 0;
    //玩耍时间
    private long playTime = 0;

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

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
}
