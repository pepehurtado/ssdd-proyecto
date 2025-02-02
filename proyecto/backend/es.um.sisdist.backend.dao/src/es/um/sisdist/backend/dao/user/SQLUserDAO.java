/**
 *
 */
package es.um.sisdist.backend.dao.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class SQLUserDAO implements IUserDAO
{
    Supplier<Connection> conn;

    public SQLUserDAO()
    {
    	conn = Lazy.lazily(() -> 
    	{
    		try
    		{
    			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

    			// Si el nombre del host se pasa por environment, se usa aquí.
    			// Si no, se usa localhost. Esto permite configurarlo de forma
    			// sencilla para cuando se ejecute en el contenedor, y a la vez
    			// se pueden hacer pruebas locales
    			String sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER")).orElse("localhost");
    			String dbName = Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd");
    			return DriverManager.getConnection(
                    "jdbc:mysql://" + sqlServerName + "/" + dbName + "?user=root&password=root");
    		} catch (Exception e)
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
            
    			return null;
    		}
    	});
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<User> getUserByEmail(String id)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("SELECT * from users WHERE email = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e)
        {
            // Fallthrough
        }
        return Optional.empty();
    }

    
    private Optional<User> createUser(ResultSet result)
    {
        return Optional.empty();
        //try
        //{
        //    return Optional.of(new User(result.getString(1), // id
        //            result.getString(2), // email
        //            result.getString(3), // pwhash
         //           result.getString(4), // name
         //           result.getString(5), // token
         //           result.getInt(6))); // visits
        //} catch (SQLException e)
        //{
         //   return Optional.empty();
        //}
    }

    @Override
    public boolean addUser(User newUser)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createDialogue(String userId, Dialogue dialogue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteUser(String user) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateDialogue(String userId, String dialogueId, Dialogue dialogue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteDialogue(String userId, String dialogueId) {
        return false;
    }

    @Override
    public boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status) {
        // TODO Auto-generated method stub
       return false;
    }

    @Override
    public Dialogue getDialogue(String userId, String dialogueId) {
        // TODO Auto- method stub
        return null;
    }

    @Override
    public boolean addVisits(String username) {
        // TODO Auto-generated method stub
        return false;
    }
    
    
}
