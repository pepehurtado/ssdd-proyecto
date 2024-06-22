package es.um.sisdist.backend.dao.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

public class MongoUserDAO implements IUserDAO
{
    private static final Logger logger = Logger.getLogger(MongoUserDAO.class.getName());
    private Supplier<MongoCollection<User>> collection;

    public MongoUserDAO()
    {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .conventions(asList(Conventions.ANNOTATION_CONVENTION))
                .automatic(true)
                .build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@" 
                + Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        collection = Lazy.lazily(() -> 
        {
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient
                .getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
                .withCodecRegistry(pojoCodecRegistry);
            return database.getCollection("users", User.class);
        });
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("email", id)).first());
        return user;
    }

    @Override
    public boolean addUser(User newUser) {
        Logger logger = Logger.getLogger(UserUtils.class.getName());

        try {
            // Calcula el token MD5
            String token = calculateMD5Token(newUser);
            newUser.setToken(token);

            logger.info("Usuario en addUser: " + newUser.toString());
            InsertOneResult result = collection.get().insertOne(newUser);
            logger.info(result.toString());
            return result.wasAcknowledged();
        } catch (Exception e) {
            logger.info("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    private String calculateMD5Token(User user) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String dataToHash = user.getId() + user.getEmail() + user.getPassword_hash();
        md.update(dataToHash.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @Override
    public boolean deleteUser(String username) {
        try {
            DeleteResult result = collection.get().deleteOne(eq("id", username));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.info("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createDialogue(String userId, Dialogue dialogue) {
        try {
            Optional<User> userOpt = getUserById(userId);
            logger.info("Usuario en crear dialogo: " + userOpt.toString() + " para userId: " + userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("Usuario encontrado: " + user.getId());
                
                if (user.getDialogues() == null) {
                    user.setDialogues(new ArrayList<>());
                }


                for (Dialogue existingDialogue : user.getDialogues()) {
                    if (existingDialogue.getDialogueId().equals(dialogue.getDialogueId())) {
                        logger.info("Diálogo con id: " + dialogue.getDialogueId() + " ya existe para el usuario: " + userId);
                        return false;
                    }
                }
                user.getDialogues().add(dialogue);
                
                
                collection.get().replaceOne(eq("id", userId), user);
                logger.info("Nuevo diálogo añadido. Usuario actualizado: " + user.getDialogues().toString());
                return true;
            } else {
                logger.info("Usuario no encontrado con id: " + userId);
                return false;
            }
        } catch (Exception e) {
            logger.info("Error al crear diálogo para userId: " + userId + " :: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateDialogue(String userId, String dialogueId, Dialogue dialogue) {
        try {
            Optional<User> userOpt = getUserById(userId);
            logger.info("Usuario en actualizar dialogo: " + userOpt.toString() + " para userId: " + userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("Usuario encontrado: " + user.getId());
                
                if (user.getDialogues() == null) {
                    user.setDialogues(new ArrayList<>());
                }

                for (Dialogue existingDialogue : user.getDialogues()) {
                    if (existingDialogue.getDialogueId().equals(dialogueId)) {
                        logger.info("Diálogo con id: " + dialogueId + " encontrado para el usuario: " + userId);
                        user.getDialogues().remove(existingDialogue);
                        user.getDialogues().add(dialogue);
                        logger.info("Diálogo actualizado. Usuario actualizado: " + user);
                        
                        collection.get().replaceOne(eq("id", userId), user);
                        return true;
                    }
                }
                logger.info("Diálogo con id: " + dialogueId + " no encontrado para el usuario: " + userId);
                return false;
            } else {
                logger.info("Usuario no encontrado con id: " + userId);
                return false;
            }
        } catch (Exception e) {
            logger.info("Error al actualizar diálogo para userId: " + userId + " :: " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean deleteDialogue(String userId, String dialogueId) {
        try {
            Optional<User> userOpt = getUserById(userId);
            logger.info("Usuario en eliminar diálogo: " + userOpt.toString() + " para userId: " + userId);
        
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("Usuario encontrado: " + user.getId());

                if (user.getDialogues() == null) {
                    logger.info("No hay diálogos para el usuario: " + userId);
                    return false;
                }

                Dialogue dialogueToRemove = null;
                for (Dialogue existingDialogue : user.getDialogues()) {
                    if (existingDialogue.getDialogueId().equals(dialogueId)) {
                        dialogueToRemove = existingDialogue;
                        break;
                    }
                }

                if (dialogueToRemove != null) {
                    user.getDialogues().remove(dialogueToRemove);
                    logger.info("Diálogo eliminado. Usuario actualizado: " + user);
                
                    collection.get().replaceOne(eq("id", userId), user);
                    return true;
                } else {
                    logger.info("Diálogo con id: " + dialogueId + " no encontrado para el usuario: " + userId);
                    return false;
                }
            } else {
                logger.info("Usuario no encontrado con id: " + userId);
                return false;
            }
        } catch (Exception e) {
            logger.info("Error al eliminar diálogo para userId: " + userId + " :: " + e.getMessage());
            return false;
        }   
    }
    
    @Override
    public boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt) {
        try {
            Optional<User> userOpt = getUserById(userId);
            logger.info("Usuario en añadir prompt: " + userOpt.toString() + " para userId: " + userId);
        
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.info("Usuario encontrado: " + user.getId());

                if (user.getDialogues() == null) {
                    logger.info("No hay diálogos para el usuario: " + userId);
                    return false;
                }

                Dialogue dialogueToUpdate = null;
                for (Dialogue existingDialogue : user.getDialogues()) {
                    if (existingDialogue.getDialogueId().equals(dialogueId)) {
                        dialogueToUpdate = existingDialogue;
                        break;
                    }
                }
                logger.info("Diálogo a actualizar: " + dialogueToUpdate);
                if (dialogueToUpdate != null) {
                    if(dialogueToUpdate.getStatus() == DialogueEstados.BUSY || dialogueToUpdate.getStatus() == DialogueEstados.FINISHED){
                        logger.info("Diálogo con id: " + dialogueId + " ya está finalizado para el usuario: " + userId);
                        return false;
                    }
                    logger.info("Prompt a añadir: " + prompt);
                    logger.info("Diálogo a actualizar: " + dialogueToUpdate.getDialogue());
                    dialogueToUpdate.addPrompt(prompt);
                    logger.info("Prompt añadido: " + prompt);
                    dialogueToUpdate.updateNextUrl();
                    logger.info("Prompt añadido. Diálogo actualizado: " + dialogueToUpdate);
                    dialogueToUpdate.setStatus(DialogueEstados.BUSY);
                    collection.get().replaceOne(eq("id", userId), user);
                    return true;
                } else {
                    logger.info("Diálogo con id: " + dialogueId + " no encontrado para el usuario: " + userId);
                    return false;
                }
            } else {
                logger.info("Usuario no encontrado con id: " + userId);
                return false;
            }
        } catch (Exception e) {
            logger.info("Error al añadir prompt para userId: " + userId + " :: " + e.getMessage());
            return false;
        }   
    }

    @Override
public boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt) {
    try {
        User user = collection.get().find(eq("id", userId)).first();

        if (user == null) {
            logger.info("User not found with ID: " + userId);
            return false;
        }
        List<Dialogue> dialogues = user.getDialogues() == null ? new LinkedList<>() : user.getDialogues();
        for (Dialogue dialogue : dialogues) {
            if (dialogue.getDialogueId().equals(dialogueId)) {
                for (Prompt p : dialogue.getDialogue()) {
                    LocalDateTime promptTimestamp = prompt.getTimestamp();
                    LocalDateTime pTimestamp = p.getTimestamp();
                    
                    // Formateamos los LocalDateTime sin milisegundos
                    String formattedPromptTimestamp = removeMilliseconds(promptTimestamp);
                    String formattedPTimestamp = removeMilliseconds(pTimestamp);
                    
                    logger.info("Los timestamps son iguales? " + formattedPTimestamp + " " + formattedPromptTimestamp);
                    logger.info("Los timestamps son iguales? " + formattedPTimestamp.equals(formattedPromptTimestamp));
                    
                    if (formattedPTimestamp.equals(formattedPromptTimestamp)) {
                        logger.info("Prompt encontrado. DialogueID: " + dialogueId + 
                                " - PromptID: " + formattedPromptTimestamp + " - Respuesta: " + prompt.getAnswer());
                        p.setAnswer(prompt.getAnswer());
                        dialogue.setStatus(DialogueEstados.READY);
                        UpdateResult result = collection.get().updateOne(eq("id", userId),
                            Updates.set("dialogues", dialogues));
                        return result.getModifiedCount() > 0;
                    }
                }
                logger.info("Prompt no encontrado. DialogueID: " + dialogueId + 
                        " - PromptID: " + prompt.getTimestamp());
                return false;
            }
        }
        logger.info("Diálogo no encontrado. DialogueID: " + dialogueId);
        return false;
    } catch (Exception e) {
        logger.info("Error processing dialogues for user ID: " + userId + " :: ");
        return false;
    }
}

private String removeMilliseconds(LocalDateTime timestamp) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    return timestamp.format(formatter);
}
    

    @Override
    public boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status) {
        try {
            User user = collection.get().find(eq("id", userId)).first();
        if (user == null) {
            logger.info("User not found with ID: " + userId);
            return false;
        }
        List<Dialogue> dialogues = user.getDialogues() == null ? new LinkedList<>() : user.getDialogues();
        for (Dialogue dialogue : dialogues) {
            if (dialogue.getDialogueId().equals(dialogueId)) {
                dialogue.setStatus(status);
                UpdateResult result = collection.get().updateOne(eq("id", userId),
                        Updates.set("dialogues", dialogues));
                return result.getModifiedCount() > 0;
            }
        }
        logger.info("Diálogo no encontrado con ID: " + dialogueId);
        return false;
    } catch (Exception e) {
        logger.info("Error processing dialogues for user ID: " + userId + " :: ");
        return false;
    }
}

    @Override
    public Dialogue getDialogue(String userId, String dialogueId) {
        try {
            User u = collection.get().find(eq("id", userId)).first();
            if (u == null) {
                logger.info("User not found with ID: " + userId);
                return null;
            }

            List<Dialogue> dialogues = u.getDialogues();

            if (dialogues == null || dialogues.isEmpty()) {
                logger.info("No dialogues found for user ID: " + userId);
                return null; 
            }

            for (Dialogue dialogue : dialogues) {
                if (dialogue.getDialogueId().equals(dialogueId)) {
                    return dialogue; 
                }
            }

            logger.info("Dialogue not found with ID: " + dialogueId + " for user ID: " + userId);
            return null;
            } catch (Exception e) {
                logger.info("Error retrieving dialogue for user ID: " + userId + " :: " + e.getMessage());
                return null; 
            }

    }

    @Override
    public boolean addVisits(String username) {
        try {
            User user = collection.get().find(eq("id", username)).first();
            if (user == null) {
                logger.info("User not found with ID: " + username);
                return false;
            }
            user.setVisits(user.getVisits() + 1);
            UpdateResult result = collection.get().updateOne(eq("id", username),
                    Updates.set("visits", user.getVisits()));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.info("Error updating visits for user ID: " + username + " :: " + e.getMessage());
            return false;
        }
    }

}