package game;

public enum WindowSize {
    WIDTH(1024),
    HEIGHT(768);

    private int size;

    public int getSize() {
        return size;
    }

    WindowSize(int size) {
        this.size = size;
    }
}
