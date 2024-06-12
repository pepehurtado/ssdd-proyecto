package es.um.sisdist.backend.dao.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Dialogue {
    private String dialogueId;
    private String status;
    private List<Prompt> dialogue;
    private String nextUrl;
    private String endUrl;

    // Constructor
    public Dialogue(String dialogueId, String status, List<Prompt> dialogue, String nextUrl, String endUrl) {
        this.dialogueId = dialogueId;
        this.status = "READY";
        this.dialogue = dialogue;
        // nextUrl y endUrl deben ser configuradas apropiadamente
        this.nextUrl = "/u/ID/dialogue/" + dialogueId + "/next";
        this.endUrl = "/u/ID/dialogue/" + dialogueId + "/end";
    }
    public Dialogue()
    {
    }

    // Getters y Setters
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

    public List<Prompt> getDialogue() {
        return dialogue;
    }

    public void setDialogue(List<Prompt> dialogue) {
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

    // Métodos para añadir mensajes a la conversación
    public void addMessage(String prompt, String answer, LocalDate timestamp) {
        this.dialogue.add(new Prompt(prompt, answer, timestamp));
    }

    public void addMessage(Prompt prompt) {
        this.dialogue.add(prompt);
    }

    // Método para finalizar la conversación
    public void endDialogue() {
        this.status = "FINISHED";
    }

    // Clase interna para representar los mensajes
    public static class Message {
        private String prompt;
        private String answer;
        private long timestamp;

        public Message(String prompt, String answer, long timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;
        }

        // Getters y Setters
        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
