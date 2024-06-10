package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DialogueDTO {
    private String dialogueId;
    private String status;
    private List<PromptDTO> dialogue;
    private String nextUrl;
    private String endUrl;

    public DialogueDTO() {
    }

    public DialogueDTO(String dialogueId, String status, List<PromptDTO> dialogue, String nextUrl, String endUrl) {
        this.dialogueId = dialogueId;
        this.status = status;
        this.dialogue = dialogue;
        this.nextUrl = nextUrl;
        this.endUrl = endUrl;
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PromptDTO> getDialogue() {
        return dialogue;
    }

    public void setDialogue(List<PromptDTO> dialogue) {
        this.dialogue = dialogue;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public String getEndUrl() {
        return endUrl;
    }

    public void setEndUrl(String endUrl) {
        this.endUrl = endUrl;
    }


}
