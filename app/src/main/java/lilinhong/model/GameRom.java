package lilinhong.model;

import java.io.Serializable;

public class GameRom implements Serializable {
    private String name = "";
    private String desc = "";
    private String image = "";
    private String path = "";
    private String md5 = "";
    //游戏封面
    private String cover = "";
    //上次使用时间
    private long lastPlayTime = 0;
    //收藏
    private boolean collect = false;
    //玩耍时间
    private long playTime = 0;
    //开始玩耍的时间这个是临时使用的
    private long startPlayTime = 0;

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

    public boolean isCollect() {
        return collect;
    }

    public GameRom setCollect(boolean collect) {
        this.collect = collect;
        return this;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public long getStartPlayTime() {
        return startPlayTime;
    }

    public void setStartPlayTime(long startPlayTime) {
        this.startPlayTime = startPlayTime;
    }
}
