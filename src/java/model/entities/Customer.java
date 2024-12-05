package model.entities;

import authn.Credentials;
import com.google.gson.annotations.Expose;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ivanm
 */
@Entity
@XmlRootElement
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="Customer_Gen", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Customer_Gen") 
    private Long id;
    @Expose
    private String username;
    
    @OneToMany(mappedBy="author")
    List<Article> articles;

    @OneToOne
    @JsonbTransient // Ignorar para evitar bucles en JSON-B
    private Credentials credential;
    
    // Campo para almacenar los links relacionados (HATEOAS)
    @Expose
    private Map<String, String> links = new HashMap<>();

    public Credentials getCredentials() {
        return credential;
    }

    public void setCredentials(Credentials credential) {
        this.credential = credential;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", username=" + username + ", articles=" + articles + "}";
    }
}
