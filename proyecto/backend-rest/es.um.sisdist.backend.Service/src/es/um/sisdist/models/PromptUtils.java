package es.um.sisdist.models;

import java.util.ArrayList;
import java.util.List;

import es.um.sisdist.backend.dao.models.Prompt;

public class PromptUtils {

    public static Prompt fromDTO(PromptDTO pdto) {
        if(pdto.getAnswer() != null) {
            return new Prompt(pdto.getPrompt(), pdto.getAnswer(),pdto.getTimestamp());
        }
        return new Prompt(pdto.getPrompt(), pdto.getTimestamp());
    }

    public static PromptDTO toDTO(Prompt p) {
        if(p.getAnswer() != null) {
            return new PromptDTO(p.getPrompt(), p.getAnswer(), p.getTimestamp());
        }
        return new PromptDTO(p.getPrompt(), p.getTimestamp());
    }

    public static List<Prompt> fromDTO(List<PromptDTO> promptDTOs) {
        List<Prompt> prompts = new ArrayList<>();
        for(PromptDTO pdto : promptDTOs) {
            prompts.add(fromDTO(pdto));
        }
        return prompts;
    }

    public static List<PromptDTO> toDTO(List<Prompt> prompts) {
        List<PromptDTO> promptDTOs = new ArrayList<>();
        for(Prompt p : prompts) {
            promptDTOs.add(toDTO(p));
        }
        return promptDTOs;
    }
    
}
