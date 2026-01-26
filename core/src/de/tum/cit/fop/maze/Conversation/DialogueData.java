package de.tum.cit.fop.maze.Conversation;

import java.util.List;

public class DialogueData {
    private String leftCharacterImage;
    private String rightCharacterImage;
    private List<DialogueLine> lines;

    public DialogueData() {
    }

    public String getLeftCharacterImage() {
        return leftCharacterImage;
    }

    public void setLeftCharacterImage(String leftCharacterImage) {
        this.leftCharacterImage = leftCharacterImage;
    }

    public String getRightCharacterImage() {
        return rightCharacterImage;
    }

    public void setRightCharacterImage(String rightCharacterImage) {
        this.rightCharacterImage = rightCharacterImage;
    }

    public List<DialogueLine> getLines() {
        return lines;
    }

    public void setLines(List<DialogueLine> lines) {
        this.lines = lines;
    }

    private float leftScale = 1.0f;
    private float leftOffsetX = 0f;
    private float leftOffsetY = 0f;
    
    private float rightScale = 1.0f;
    private float rightOffsetX = 0f;
    private float rightOffsetY = 0f;

    public float getLeftScale() { return leftScale; }
    public void setLeftScale(float leftScale) { this.leftScale = leftScale; }

    public float getLeftOffsetX() { return leftOffsetX; }
    public void setLeftOffsetX(float leftOffsetX) { this.leftOffsetX = leftOffsetX; }

    public float getLeftOffsetY() { return leftOffsetY; }
    public void setLeftOffsetY(float leftOffsetY) { this.leftOffsetY = leftOffsetY; }

    public float getRightScale() { return rightScale; }
    public void setRightScale(float rightScale) { this.rightScale = rightScale; }

    public float getRightOffsetX() { return rightOffsetX; }
    public void setRightOffsetX(float rightOffsetX) { this.rightOffsetX = rightOffsetX; }

    public float getRightOffsetY() { return rightOffsetY; }
    public void setRightOffsetY(float rightOffsetY) { this.rightOffsetY = rightOffsetY; }

    private float leftPortraitScale = 1.0f;
    private float leftPortraitOffsetX = 0f;
    private float leftPortraitOffsetY = 0f;

    private float rightPortraitScale = 1.0f;
    private float rightPortraitOffsetX = 0f;
    private float rightPortraitOffsetY = 0f;

    public float getLeftPortraitScale() { return leftPortraitScale; }
    public void setLeftPortraitScale(float scale) { this.leftPortraitScale = scale; }
    public float getLeftPortraitOffsetX() { return leftPortraitOffsetX; }
    public void setLeftPortraitOffsetX(float offset) { this.leftPortraitOffsetX = offset; }
    public float getLeftPortraitOffsetY() { return leftPortraitOffsetY; }
    public void setLeftPortraitOffsetY(float offset) { this.leftPortraitOffsetY = offset; }

    public float getRightPortraitScale() { return rightPortraitScale; }
    public void setRightPortraitScale(float scale) { this.rightPortraitScale = scale; }
    public float getRightPortraitOffsetX() { return rightPortraitOffsetX; }
    public void setRightPortraitOffsetX(float offset) { this.rightPortraitOffsetX = offset; }
    public float getRightPortraitOffsetY() { return rightPortraitOffsetY; }
    public void setRightPortraitOffsetY(float offset) { this.rightPortraitOffsetY = offset; }

    public static class DialogueLine {
        private String text;
        private String speaker;
        private boolean isLeft;

        public DialogueLine() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSpeaker() {
            return speaker;
        }

        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }

        private String portrait;
        private String effect;

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

        public boolean isLeft() {
            return isLeft;
        }

        public void setLeft(boolean left) {
            isLeft = left;
        }
        
        private boolean hideLeft = false;
        private boolean hideRight = false;
        
        public boolean isHideLeft() { return hideLeft; }
        public void setHideLeft(boolean hideLeft) { this.hideLeft = hideLeft; }
        
        public boolean isHideRight() { return hideRight; }
        public void setHideRight(boolean hideRight) { this.hideRight = hideRight; }
    }
}
