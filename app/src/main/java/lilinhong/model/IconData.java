package lilinhong.model;

import android.graphics.Bitmap;

public class IconData {
    public IconData(Bitmap bitmap,String filePath,int position){
        this.bitmap = bitmap;
        this.filePath = filePath;
        this.position = position;
    }
    private Bitmap bitmap = null;
    private String filePath = null;
    private int position = -1;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
