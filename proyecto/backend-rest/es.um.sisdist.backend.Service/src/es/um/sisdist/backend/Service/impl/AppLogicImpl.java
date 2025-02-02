package es.um.sisdist.backend.Service.impl;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl {
    IDAOFactory daoFactory;
    IUserDAO dao;

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl() {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));

        if (backend.isPresent() && backend.get().equals("mongo"))
            dao = daoFactory.createMongoUserDAO();
        else
            dao = daoFactory.createSQLUserDAO();

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid needing certificates.
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        asyncStub = GrpcServiceGrpc.newStub(channel);
    }

    public static AppLogicImpl getInstance() {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId) {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId) {
        return dao.getUserById(userId);
    }

    public boolean ping(int v) {
        logger.info("Issuing ping, value: " + v);

        // Test de grpc, puede hacerse con la BD
        var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);

        return response.getV() == v;
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass) {
        Optional<User> u = dao.getUserByEmail(email);
        System.out.println("Usuario dentro del AppLogical" + u.toString());
        if (u.isPresent()) {
            String hashed_pass = UserUtils.md5pass(pass);
            System.out.println("Entra en el if, su pass es " + pass + "-----" + u.get().getPassword_hash() + "-----" + hashed_pass);
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash())){
                dao.addVisits(u.get().getId());
                return u;
            }
        }

        return Optional.empty();
    }

    public boolean addUser(User user) {
            if (dao.addUser(user)){
                return true;
            }
            return false;
        
    }

    public boolean deleteUser(String username) {
        logger.info("Eliminando usuario: " + username);
        return dao.deleteUser(username);
    }

    public boolean createDialogue(String user, Dialogue d) {
        return dao.createDialogue(user, d);
    }

    public boolean updateDialogue(String userId,String dialogueId, Dialogue d) {
        return dao.updateDialogue(userId, dialogueId, d);
    }
    
    public boolean deleteDialogue(String userId, String dialogueId) {
        return dao.deleteDialogue(userId, dialogueId);
    }

    public String addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt) {

        Dialogue dialogo = dao.getDialogue(userId, dialogueId);

        DialogueEstados e = dialogo.getStatus();

        if (e == DialogueEstados.FINISHED){
            return "El dialogo esta finalizado";
        }
        if(e == DialogueEstados.BUSY){
            return "El dialogo esta ocupado";
        }

        if (nextUrl.equals("end")) {
            dao.updateDialogueEstado(userId, dialogueId, DialogueEstados.FINISHED);
        }

        logger.info("Petición de prompt (`" + userId + "` en `" + dialogueId +
        "`): timestamp = " + prompt.getTimestamp() + " prompt = " + prompt.getPrompt());
        
        // Almacenamos el prompt en la base de datos
        dao.addPrompt(userId, dialogueId, nextUrl, prompt);

        // Creamos el mensaje para el servidor gRPC
        var promptRequest = PromptRequest.newBuilder()
            .setPrompt(prompt.getPrompt())
            .setDialogueId(dialogueId)
            .setTimestamp(prompt.getTimestamp().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
            .setUserId(userId)
            .build();

        // Definimos el StreamObserver para manejar la respuesta
        StreamObserver<PromptResponse> promptObserver = new StreamObserver<PromptResponse>() {
            @Override
            public void onNext(PromptResponse value) {
                System.out.println("Respuesta recibida: " + value.getSuccess());                
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Comunicacion completada.");
            }
        };

        // Llamamos al servidor gRPC de manera asíncrona
        asyncStub.sendPrompt(promptRequest, promptObserver);

    return "ok";
}


}
