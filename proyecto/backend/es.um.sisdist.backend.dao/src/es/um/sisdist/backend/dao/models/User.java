/**
 *
 */
package es.um.sisdist.backend.dao.models;

import java.util.List;

import es.um.sisdist.backend.dao.models.utils.UserUtils;

public class User
{
    private String id;
    private String email;
    private String password_hash;
    private String name;

    private String token;

    private int visits;

    private List<Dialogue> dialogues;

    public String getId()
    {
        return id;
    }
    public void setId(final String uid)
    {
        this.id = uid;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(final String email)
    {
        this.email = email;
    }

    public String getPassword_hash()
    {
        return password_hash;
    }

    public void setPassword_hash(final String password_hash)
    {
        this.password_hash = password_hash;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(final String token)
    {
        this.token = token;
    }


    public List<Dialogue> getDialogues()
    {
        return dialogues;
    }


    public void setDialogues(final List<Dialogue> dialogues) {

        this.dialogues = dialogues;
        
    }

    public int getVisits()
    {
        return visits;
    }
    public void setVisits(final int visits)
    {
        this.visits = visits;
    }

    public User(String email, String password_hash, String name, String token, int visits)
    {
        this(email, email, password_hash, name, token, visits);
        this.id = UserUtils.md5pass(email);
    }

    public User(String id, String email, String password_hash, String name, String token, int visits)
    {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.name = name;
        this.token = token;
        this.visits = visits;
    }

    @Override
    public String toString()
    {
        return "User [id=" + id + ", email=" + email + ", password_hash=" + password_hash + ", name=" + name
                + ", token=" + token + ", visits=" + visits + "]";
    }

    public User()
    {
    }
}