package org.wglin.imagepicker;

public class ImageFolder {
    /**
     * image's dir
     */
    private String dir;

    /**
     * the first image's path
     */
    private String firstImagePath;

    /**
     * folders name
     */
    private String name;

    /**
     * number of image
     */
    private int count;

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}