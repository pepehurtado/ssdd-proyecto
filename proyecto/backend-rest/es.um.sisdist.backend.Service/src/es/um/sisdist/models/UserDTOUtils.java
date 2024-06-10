/**
 *
 */
package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;

/**
 * @author dsevilla
 *
 */
public class UserDTOUtils
{
    public static User fromDTO(UserDTO udto)
    {
        String password_hash = UserUtils.md5pass(udto.getPassword());
        return new User(udto.getId(), udto.getEmail(), password_hash, udto.getName(), udto.getToken(),
                udto.getVisits());
    }

    public static UserDTO toDTO(User u)
    {
        return new UserDTO(u.getId(), u.getEmail(), "", // Password never is returned back
                u.getName(), u.getToken(), u.getVisits());
    }
}
