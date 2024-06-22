package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDTO
{
    private String id;
    private String email;
    private String password;
    private String name;

    private String token;

    private int visits;

    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
    public int getVisits()
    {
        return visits;
    }

    /**
     * @param visits the visits to set
     */
    public void setVisits(int visits)
    {
        this.visits = visits;
    }

    public UserDTO(String id, String email, String password, String name, String token, int visits)
    {
        super();
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.token = token;
        this.visits = visits;
    }

    public UserDTO()
    {
    }
}
