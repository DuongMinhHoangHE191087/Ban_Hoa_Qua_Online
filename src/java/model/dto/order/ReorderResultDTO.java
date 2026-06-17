package model.dto.order;

/**
 * Ket qua reorder tu don hang cu vao gio hang hien tai.
 */
public class ReorderResultDTO {

    private int addedCount;
    private int skippedCount;

    public int getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }
}
