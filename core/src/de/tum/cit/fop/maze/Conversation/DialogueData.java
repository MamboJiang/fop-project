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

        public boolean isLeft() {
            return isLeft;
        }

        public void setLeft(boolean left) {
            isLeft = left;
        }
    }
}
