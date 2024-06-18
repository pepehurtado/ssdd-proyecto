package es.um.sisdist.backend.dao.user;

import java.util.ArrayList;
import static java.util.Arrays.asList;
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
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.User;
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
        try {
            logger.info("Usuario en addUser: ");
            InsertOneResult result = collection.get().insertOne(newUser);
            logger.info(result.toString());
            return result.wasAcknowledged();
        } catch (Exception e) {
            logger.info("Error al crear usuario: " + e.getMessage());
            return false;
        }
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
                logger.info("Nuevo diálogo añadido. Usuario actualizado: " + user);
                
                collection.get().replaceOne(eq("id", userId), user);
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

                if (dialogueToUpdate != null) {
                    dialogueToUpdate.getDialogue().add(prompt);
                    dialogueToUpdate.setNextUrl();
                    logger.info("Prompt añadido. Diálogo actualizado: " + dialogueToUpdate);
                
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

}
