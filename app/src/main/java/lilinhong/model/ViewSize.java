package lilinhong.model;

public class ViewSize {
    public ViewSize(int width,int height,int marginTop,int marginLeft,int marginRight,int marginButtom){
        this.width = width;
        this.height = height;
        this.marginTop = marginTop;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginButtom = marginButtom;
    }
    private int width;
    private int height;
    private int marginTop = 0;
    private int marginLeft = 0;
    private int marginRight = 0;
    private int marginButtom = 0;
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginButtom() {
        return marginButtom;
    }

    public void setMarginButtom(int marginButtom) {
        this.marginButtom = marginButtom;
    }
}
