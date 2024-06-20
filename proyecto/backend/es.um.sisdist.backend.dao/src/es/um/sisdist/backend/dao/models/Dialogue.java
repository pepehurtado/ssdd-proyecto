package es.um.sisdist.backend.dao.models;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Dialogue {
    // Atributos
    private String dialogueId;
    private DialogueEstados status;
    private List<Prompt> dialogue;
    private String nextUrl;
    private String endUrl;

    // Getters & Setters
    /**
     * @return the id
     */
    public String getDialogueId() {
        return dialogueId;
    }

    /**
     * @param id the id to set
     */
    public void setDialogueId(String id) {
        this.dialogueId = id;
    }

    public DialogueEstados getStatus() {
        return status;
    }

    public void setStatus(DialogueEstados status) {
        this.status = status;
    }

    /**
     * @return the prompts
     */
    public List<Prompt> getDialogue() {
        if (dialogue == null) {
            return List.of();
        }
        return Collections.unmodifiableList(dialogue);
    }

    /**
     * @param prompts the prompts to set
     */
    public void setDialogue(List<Prompt> prompts) {
        this.dialogue = prompts;
    }

    /**
     * @return the nextUrl
     */
    public String getNextUrl() {
        return nextUrl;
    }

    /**
     * @param nextUrl the nextUrl to set
     */
    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    /**
     * @return the endUrl
     */
    public String getEndUrl() {
        return endUrl;
    }

    /**
     * @param endUrl the endUrl to set
     */
    public void setEndUrl(String endUrl) {
        this.endUrl = endUrl;
    }


    // Funcionalidad
    @Override
    public String toString() {
        // Devuelve el diálogo en formato de cadena (todos los atributos)
        return "Dialogue{" +
                "id='" + dialogueId + "\', " +
                "status='" + status + "\', " +
                "dialogue='" + dialogue + "\', " +
                "nextUrl='" + nextUrl + "\', " +
                "endUrl='" + endUrl + "\'" +
                '}';
    }

    public void updateDialogueName(String newName){
        this.setDialogueId(newName);
        updateNextUrl();
        updateEndUrl();

    }


    public void addPrompt(Prompt prompt) {
        // Comprobamos que el prompt no exista
        this.dialogue.stream().filter(p -> p.getTimestamp().equals(prompt.getTimestamp()))
                .findFirst()
                .ifPresent(p -> {
                    // Susitituimos el prompt existente por el nuevo
                    this.dialogue.set(this.dialogue.indexOf(p), prompt);
                });

        // Añadimos el prompt a la lista
        this.dialogue.add(prompt);

        // Generamos un nuevo `next`
        updateNextUrl();
    }

    /**
     * Función para actualizar el `next` del diálogo
     */
    public void updateNextUrl() {
        // Utilizamos el timestamp actual directamente en lugar de su hashCode
        String timestamp = Long.toString(System.currentTimeMillis());
        // Usamos el timestamp directamente en la URL
        this.nextUrl = "/dialogue/" + this.dialogueId + "/" + timestamp;
    }
    

    /**
     * Función para actualizar el `end` del diálogo
     */
    public void updateEndUrl() {
        this.endUrl = "/dialogue/" + this.dialogueId + "/end";
    }

    /**
     * Función para inicializar un diálogo
     */
    public void initialiseDialogue() {
        // Inicializamos las URLs y el estado
        this.status = DialogueEstados.READY; //TODO "READY"

        // Actualizamos el `next`
        updateNextUrl();

        updateEndUrl();

        // Inicializamos la lista de prompts
        this.dialogue = new LinkedList<>();
    }

    // Constructores
    public Dialogue(String id, DialogueEstados status, List<Prompt> prompts, String nextUrl,
            String endUrl) {
        this.dialogueId = id;
        this.status = status;
        this.dialogue = prompts;
        this.nextUrl = nextUrl;
        this.endUrl = endUrl;

        // Inicializamos si el diálogo es nuevo
        if (status == null) {
            initialiseDialogue();
        }
    }

    public Dialogue() {
    }
}
