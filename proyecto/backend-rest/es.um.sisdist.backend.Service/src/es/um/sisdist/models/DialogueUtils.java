package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.Prompt;
import java.util.List;


public class DialogueUtils {

        public static Dialogue fromDTO(DialogueDTO ddto) {
        // Comprobamos qué atributos tiene el DTO
        List<Prompt> prompts = PromptUtils.fromDTO(ddto.getDialogue());
        return new Dialogue(ddto.getDialogueId(), ddto.getStatus(), prompts, ddto.getNextUrl(),
                ddto.getEndUrl());
    }

    public static DialogueDTO toDTO(Dialogue d) {
        // Comprobamos qué atributos tiene el Dialogue
        List<PromptDTO> prompts = PromptUtils.toDTO(d.getDialogue());
        return new DialogueDTO(d.getDialogueId(), d.getStatus(), prompts, d.getNextUrl(),
                d.getEndUrl());
    }
}
