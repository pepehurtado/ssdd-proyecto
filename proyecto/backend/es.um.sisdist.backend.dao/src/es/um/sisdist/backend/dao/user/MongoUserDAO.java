package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.utils.Lazy;
import com.mongodb.client.result.InsertOneResult;

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
    public boolean createDialogue(String userId, Dialogue dialogue) {
        try {
            // Encontrar al usuario por su id
            Optional<User> userOpt = getUserById(userId);
    
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Agregar el nuevo diálogo a la lista de diálogos del usuario
                user.getDialogues().add(dialogue);
    
                // Actualizar el usuario en la base de datos
                collection.get().replaceOne(eq("id", userId), user);
                return true;
            } else {
                logger.info("Usuario no encontrado con id: " + userId);
                return false;
            }
        } catch (Exception e) {
            logger.info("Error al crear diálogo: " + e.getMessage());
            return false;
        
    }}

}
