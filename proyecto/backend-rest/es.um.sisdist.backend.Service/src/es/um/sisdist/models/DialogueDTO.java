package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collections;
import java.util.List;

import es.um.sisdist.backend.dao.models.DialogueEstados;

@XmlRootElement
public class DialogueDTO {
    private String dialogueId;
    private DialogueEstados status;
    private List<PromptDTO> dialogue;
    private String nextUrl;
    private String endUrl;

    public DialogueDTO() {
    }

    public DialogueDTO(String dialogueId, DialogueEstados status, List<PromptDTO> dialogue, String nextUrl, String endUrl) {
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

    public DialogueEstados getStatus() {
        return status;
    }

    public void setStatus(DialogueEstados status) {
        this.status = status;
    }

    public List<PromptDTO> getDialogue() {
        if (dialogue == null) {
            return List.of();
        }
        return Collections.unmodifiableList(dialogue);
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
