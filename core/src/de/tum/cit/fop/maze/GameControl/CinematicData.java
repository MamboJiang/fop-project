package de.tum.cit.fop.maze.GameControl;

import java.util.List;

/**
 * Data model for a cinematic sequence, loaded from JSON.
 */
public class CinematicData {
    private List<CinematicFrame> frames;

    /**
     * Default constructor.
     */
    public CinematicData() {
    }

    /**
     * @return The list of frames in this cinematic.
     */
    public List<CinematicFrame> getFrames() {
        return frames;
    }

    /**
     * Sets the list of frames.
     * 
     * @param frames The new list of frames.
     */
    public void setFrames(List<CinematicFrame> frames) {
        this.frames = frames;
    }

    /**
     * Represents a single frame in the cinematic.
     */
    public static class CinematicFrame {
        private String text;
        private String image;
        private boolean showImage;

        /**
         * Default constructor.
         */
        public CinematicFrame() {
        }

        /**
         * @return The text to display.
         */
        public String getText() {
            return text;
        }

        /**
         * @param text The text to set.
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * @return The image path.
         */
        public String getImage() {
            return image;
        }

        /**
         * @param image The image path to set.
         */
        public void setImage(String image) {
            this.image = image;
        }

        /**
         * @return True if the image should be shown.
         */
        public boolean isShowImage() {
            return showImage;
        }

        /**
         * @param showImage Whether to show the image.
         */
        public void setShowImage(boolean showImage) {
            this.showImage = showImage;
        }
    }
}
