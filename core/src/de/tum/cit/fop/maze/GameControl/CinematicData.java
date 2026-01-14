package de.tum.cit.fop.maze.GameControl;

import java.util.List;

public class CinematicData {
    private List<CinematicFrame> frames;

    public CinematicData() {}

    public List<CinematicFrame> getFrames() {
        return frames;
    }

    public void setFrames(List<CinematicFrame> frames) {
        this.frames = frames;
    }

    public static class CinematicFrame {
        private String text;
        private String image;
        private boolean showImage;

        public CinematicFrame() {}

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public boolean isShowImage() { return showImage; }
        public void setShowImage(boolean showImage) { this.showImage = showImage; }
    }
}
