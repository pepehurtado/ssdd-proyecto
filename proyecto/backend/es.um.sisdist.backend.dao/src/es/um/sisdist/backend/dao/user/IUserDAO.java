package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO {
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String email);

    boolean addUser(User user);

    boolean deleteUser(String username);

    boolean addVisits(String username);

    boolean createDialogue(String userId, Dialogue dialogue);

    boolean updateDialogue(String userId, String dialogueId, Dialogue dialogue);

    boolean deleteDialogue(String userId, String dialogueId);

    boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt);

    boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt);
    
    boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status);

    Dialogue getDialogue(String userId, String dialogueId);
}
