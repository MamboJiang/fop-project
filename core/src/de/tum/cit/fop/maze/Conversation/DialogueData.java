package de.tum.cit.fop.maze.Conversation;

import java.util.List;

/**
 * Data model representing a dialogue conversation.
 * Contains character configurations and a list of dialogue lines.
 */
public class DialogueData {
    private String leftCharacterImage;
    private String rightCharacterImage;
    private List<DialogueLine> lines;

    /**
     * Default constructor for DialogueData.
     */
    public DialogueData() {
    }

    /**
     * Gets the left character image path.
     * @return Image path.
     */
    public String getLeftCharacterImage() {
        return leftCharacterImage;
    }

    /**
     * Sets the left character image path.
     * @param leftCharacterImage Image path.
     */
    public void setLeftCharacterImage(String leftCharacterImage) {
        this.leftCharacterImage = leftCharacterImage;
    }

    /**
     * Gets the right character image path.
     * @return Image path.
     */
    public String getRightCharacterImage() {
        return rightCharacterImage;
    }

    /**
     * Sets the right character image path.
     * @param rightCharacterImage Image path.
     */
    public void setRightCharacterImage(String rightCharacterImage) {
        this.rightCharacterImage = rightCharacterImage;
    }

    /**
     * Gets the list of dialogue lines.
     * @return List of lines.
     */
    public List<DialogueLine> getLines() {
        return lines;
    }

    /**
     * Sets the list of dialogue lines.
     * @param lines List of lines.
     */
    public void setLines(List<DialogueLine> lines) {
        this.lines = lines;
    }

    private float leftScale = 1.0f;
    private float leftOffsetX = 0f;
    private float leftOffsetY = 0f;
    
    private float rightScale = 1.0f;
    private float rightOffsetX = 0f;
    private float rightOffsetY = 0f;

    /** @return Scale for the left character. */
    public float getLeftScale() { return leftScale; }
    /** @param leftScale Scale for the left character. */
    public void setLeftScale(float leftScale) { this.leftScale = leftScale; }

    /** @return X offset for the left character. */
    public float getLeftOffsetX() { return leftOffsetX; }
    /** @param leftOffsetX X offset for the left character. */
    public void setLeftOffsetX(float leftOffsetX) { this.leftOffsetX = leftOffsetX; }

    /** @return Y offset for the left character. */
    public float getLeftOffsetY() { return leftOffsetY; }
    /** @param leftOffsetY Y offset for the left character. */
    public void setLeftOffsetY(float leftOffsetY) { this.leftOffsetY = leftOffsetY; }

    /** @return Scale for the right character. */
    public float getRightScale() { return rightScale; }
    /** @param rightScale Scale for the right character. */
    public void setRightScale(float rightScale) { this.rightScale = rightScale; }

    /** @return X offset for the right character. */
    public float getRightOffsetX() { return rightOffsetX; }
    /** @param rightOffsetX X offset for the right character. */
    public void setRightOffsetX(float rightOffsetX) { this.rightOffsetX = rightOffsetX; }

    /** @return Y offset for the right character. */
    public float getRightOffsetY() { return rightOffsetY; }
    /** @param rightOffsetY Y offset for the right character. */
    public void setRightOffsetY(float rightOffsetY) { this.rightOffsetY = rightOffsetY; }

    private float leftPortraitScale = 1.0f;
    private float leftPortraitOffsetX = 0f;
    private float leftPortraitOffsetY = 0f;

    private float rightPortraitScale = 1.0f;
    private float rightPortraitOffsetX = 0f;
    private float rightPortraitOffsetY = 0f;

    /** @return Portrait scale for the left character. */
    public float getLeftPortraitScale() { return leftPortraitScale; }
    /** @param scale Portrait scale for the left character. */
    public void setLeftPortraitScale(float scale) { this.leftPortraitScale = scale; }
    /** @return Portrait X offset for the left character. */
    public float getLeftPortraitOffsetX() { return leftPortraitOffsetX; }
    /** @param offset Portrait X offset for the left character. */
    public void setLeftPortraitOffsetX(float offset) { this.leftPortraitOffsetX = offset; }
    /** @return Portrait Y offset for the left character. */
    public float getLeftPortraitOffsetY() { return leftPortraitOffsetY; }
    /** @param offset Portrait Y offset for the left character. */
    public void setLeftPortraitOffsetY(float offset) { this.leftPortraitOffsetY = offset; }

    /** @return Portrait scale for the right character. */
    public float getRightPortraitScale() { return rightPortraitScale; }
    /** @param scale Portrait scale for the right character. */
    public void setRightPortraitScale(float scale) { this.rightPortraitScale = scale; }
    /** @return Portrait X offset for the right character. */
    public float getRightPortraitOffsetX() { return rightPortraitOffsetX; }
    /** @param offset Portrait X offset for the right character. */
    public void setRightPortraitOffsetX(float offset) { this.rightPortraitOffsetX = offset; }
    /** @return Portrait Y offset for the right character. */
    public float getRightPortraitOffsetY() { return rightPortraitOffsetY; }
    /** @param offset Portrait Y offset for the right character. */
    public void setRightPortraitOffsetY(float offset) { this.rightPortraitOffsetY = offset; }

    /**
     * Represents a single line of dialogue.
     */
    public static class DialogueLine {
        private String text;
        private String speaker;
        private boolean isLeft;

        /**
         * Default constructor for DialogueLine.
         */
        public DialogueLine() {
        }

        /** @return dialogue text. */
        public String getText() {
            return text;
        }

        /** @param text dialogue text. */
        public void setText(String text) {
            this.text = text;
        }

        /** @return speaker name. */
        public String getSpeaker() {
            return speaker;
        }

        /** @param speaker speaker name. */
        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }

        private String portrait;
        private String effect;

        /** @return portrait image path. */
        public String getPortrait() {
            return portrait;
        }

        /** @param portrait portrait image path. */
        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        /** @return effect name. */
        public String getEffect() {
            return effect;
        }

        /** @param effect effect name. */
        public void setEffect(String effect) {
            this.effect = effect;
        }

        /** @return true if left character is speaking. */
        public boolean isLeft() {
            return isLeft;
        }

        /** @param left set true if left character is speaking. */
        public void setLeft(boolean left) {
            isLeft = left;
        }
        
        private boolean hideLeft = false;
        private boolean hideRight = false;
        
        /** @return true if left character should be hidden. */
        public boolean isHideLeft() { return hideLeft; }
        /** @param hideLeft set true to hide left character. */
        public void setHideLeft(boolean hideLeft) { this.hideLeft = hideLeft; }
        
        /** @return true if right character should be hidden. */
        public boolean isHideRight() { return hideRight; }
        /** @param hideRight set true to hide right character. */
        public void setHideRight(boolean hideRight) { this.hideRight = hideRight; }
    }
}
